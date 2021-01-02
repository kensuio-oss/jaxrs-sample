package io.kensu.jdbc.parser;

import io.kensu.collector.model.DamSchemaUtils;
import io.kensu.dim.client.model.FieldDef;
import io.kensu.utils.ConcurrentHashMultimap;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Database;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.kensu.utils.ListUtils.orEmptyList;

public class DamJdbcQueryParser {
    private final Logger logger = Logger.getLogger(DamJdbcQueryParser.class.getName());

    public final String dbInstance;
    public final String dbType;
    public final String sqlStatement;
    public final Statement statement;

    public DamJdbcQueryParser(String dbInstance,
                              String dbType,
                              String sqlStatement) throws JSQLParserException {
        this.dbInstance = dbInstance;
        this.dbType = dbType;
        this.sqlStatement = sqlStatement;

        this.statement = CCJSqlParserUtil.parse(sqlStatement);
        tryToAddDefaultDatabase();
    }

    // Attention: this is mutable
    protected void addSchemaNameToTable(Table table){
        if (table.getSchemaName() == null) {
            table.setSchemaName(dbInstance);
        }
    }

    private String getTableName(Table table) {
        return table.getSchemaName()+"."+table.getName();
    }

    protected void tryToAddDefaultDatabase() throws JSQLParserException {
        // select based on: https://github.com/JSQLParser/JSqlParser/issues/812
        try {
            StringBuilder buffer = new StringBuilder();
            ExpressionDeParser expressionDeParser = new ExpressionDeParser();
            SelectDeParser selectDeParser = new SelectDeParser(expressionDeParser, buffer) {
                @Override
                public void visit(Table tableName) {
                    addSchemaNameToTable(tableName);
                    super.visit(tableName);
                }
            };
            StatementDeParser deparser = new StatementDeParser(expressionDeParser, selectDeParser, buffer);
            if (statement instanceof Insert) {
                Insert stmt = (Insert) statement;
                deparser.visit(stmt);
                addSchemaNameToTable(stmt.getTable());
            } else if (statement instanceof Update) {
                Update stmt = (Update) statement;
                deparser.visit(stmt);
                addSchemaNameToTable(stmt.getTable());
            } else if (statement instanceof Delete) {
                Delete stmt = (Delete) statement;
                deparser.visit(stmt);
                addSchemaNameToTable(stmt.getTable());
            } else if (statement instanceof Select) {
                deparser.visit((Select) statement);
            }
        } catch (RuntimeException e){
            logger.log(Level.INFO, "Unable to set default db", e);
            // ??? this.statement = CCJSqlParserUtil.parse(sqlStatement);
        }
    }

    protected Table orDefaultTable(Table t, Table defaultT){
        return (t == null) ? defaultT : t;
    }

    public ReferencedSchemaFieldsInfo guessReferencedOutputTableSchemas() {
        ConcurrentHashMultimap<FieldDef> dataFieldsByTable = new ConcurrentHashMultimap<>();
        ConcurrentHashMultimap<FieldDef> controlFieldsByTable = new ConcurrentHashMultimap<>();
        final String lineageOperation;
        if (statement instanceof Insert) {
            lineageOperation = "insert into";
            Insert insertStatement = (Insert) statement;
            Table mainTable = insertStatement.getTable();
            String mainTableName = getTableName(mainTable);

            Consumer<Column> columnConsumer = column -> {
                dataFieldsByTable.addEntry(mainTableName, DamSchemaUtils.fieldWithMissingInfo(column.getColumnName()));
            };

            orEmptyList(insertStatement.getSetColumns()).forEach(columnConsumer);
            orEmptyList(insertStatement.getColumns()).forEach(columnConsumer);

            // Get the values (as VALUES (...) or SELECT)
            ItemsList insertItems = insertStatement.getItemsList();
            if (insertItems != null) {
                insertStatement.getItemsList().accept(new ItemsListVisitorAdapter() {
                    @Override
                    public void visit(NamedExpressionList namedExpressionList) {
                        namedExpressionList.getNames().forEach(column -> {
                            dataFieldsByTable.addEntry(mainTableName, DamSchemaUtils.fieldWithMissingInfo(column));
                        });
                    }
                });
            }
            // only if columns were not provided explicitly, guess them from aliases in subselect, if any
            if (dataFieldsByTable.size() == 0 && insertStatement.getSelect() != null) {
                insertStatement.getSelect().getSelectBody().accept(new SelectVisitorAdapter() {
                    @Override
                    public void visit(PlainSelect plainSelect) {
                        plainSelect.getSelectItems().forEach(item -> {
                            item.accept(new SelectItemVisitorAdapter() {
                                @Override
                                public void visit(SelectExpressionItem item) {
                                    final List<String> maybeExprColumnList = new ArrayList<>();
                                    item.getExpression().accept(new ExpressionVisitorAdapter() {
                                        @Override
                                        public void visit(Column column) {
                                            maybeExprColumnList.add(column.getColumnName());
                                        }
                                    });
                                    String maybeExprColumnName = (maybeExprColumnList.size() == 1) ? maybeExprColumnList.get(0) : null;
                                    final String columnName = (item.getAlias() != null) ? item.getAlias().getName() : maybeExprColumnName;
                                    if (columnName != null) {
                                        dataFieldsByTable.addEntry(mainTableName, DamSchemaUtils.fieldWithMissingInfo(columnName));
                                    }
                                }
                            });
                        });
                    }
                });
            }
        } else if (statement instanceof Update) {
            lineageOperation = "update";
            Update updStatement = (Update) statement;
            Table mainTable = updStatement.getTable();
            updStatement.getColumns().forEach(column -> {
                dataFieldsByTable.addEntry(
                        getTableName(mainTable),
                        DamSchemaUtils.fieldWithMissingInfo(column.getColumnName())
                );
            });
            updStatement.getWhere().accept(new ExpressionVisitorAdapter() {
                @Override
                public void visit(Column column) {
                    controlFieldsByTable.addEntry(
                            getTableName(orDefaultTable(column.getTable(), mainTable)),
                            DamSchemaUtils.fieldWithMissingInfo(column.getColumnName())
                    );
                }
            });
        } else if (statement instanceof Delete) {
            lineageOperation = "delete from";
            Delete delStatement = (Delete) statement;
            Table mainTable = delStatement.getTable();
            delStatement.getWhere().accept(new ExpressionVisitorAdapter() {
                @Override
                public void visit(Column column) {
                    controlFieldsByTable.addEntry(
                            getTableName(orDefaultTable(column.getTable(), mainTable)),
                            DamSchemaUtils.fieldWithMissingInfo(column.getColumnName())
                    );
                }
            });
        } else {
            lineageOperation = "unknown";
            logger.warning("parsing OK, but unsupported query type for: " + sqlStatement);
        }
        ConcurrentHashMultimap<Map.Entry<FieldDef, String>> fieldsMap = new ConcurrentHashMultimap<>();
        // fill in the values of data with nulls as there is no alias... I guess oO
        dataFieldsByTable.entrySet().stream().forEach(e -> e.getValue().forEach(c -> fieldsMap.addEntry(e.getKey(), Map.entry(c, null))));
        return new ReferencedSchemaFieldsInfo(lineageOperation, fieldsMap, controlFieldsByTable);
    }

    // FIXME: Assumes that all columns are explicitly mentioned, so table.* is unsupported !
    public ReferencedSchemaFieldsInfo guessReferencedInputTableSchemas() {
        ConcurrentHashMultimap<Map.Entry<FieldDef, String>> dataFieldsByTable = new ConcurrentHashMultimap<>();
        ConcurrentHashMultimap<FieldDef> controlFieldsByTable = new ConcurrentHashMultimap<>();
        if (statement instanceof Select) {
            Select selectStatement = (Select) statement;
            // initialize alias -> original table name cache
            TableNamesAndAliasesFinder tableNamesAndAliasesFinder = new TableNamesAndAliasesFinder();
            List<String> referencedTablesList = tableNamesAndAliasesFinder.getTableList(selectStatement);
            // data lineage
            for (SelectItem selectItem : ((PlainSelect)selectStatement.getSelectBody()).getSelectItems()) {
                selectItem.accept(new SelectItemVisitorAdapter() {
                    @Override
                    public void visit(SelectExpressionItem item) {
                        Expression expr = item.getExpression();
                        logger.fine("expr:" + expr.getClass().getName());
                        if (expr instanceof Column){
                            Column column = (Column) expr;
                            Table resolvedColumnTable = resolveColumnTableOrFail(column, referencedTablesList);
                            Table resolvedColumnTableWithoutAlias = tableNamesAndAliasesFinder.getTableWithoutAlias(resolvedColumnTable);
                            dataFieldsByTable.addEntry(
                                    getTableName(resolvedColumnTableWithoutAlias),
                                    Map.entry(DamSchemaUtils.fieldWithMissingInfo(column.getColumnName()), item.getAlias().getName())
                            );
                        }

                    }
                });
            }
            // control lineage
            Optional.ofNullable(((PlainSelect) selectStatement.getSelectBody()).getWhere()).ifPresent(w -> w.accept(new ExpressionVisitorAdapter() {
                @Override
                public void visit(Column column) {
                    Table resolvedColumnTable = resolveColumnTableOrFail(column, referencedTablesList);
                    Table resolvedColumnTableWithoutAlias = tableNamesAndAliasesFinder.getTableWithoutAlias(resolvedColumnTable);
                    controlFieldsByTable.addEntry(
                            getTableName(resolvedColumnTableWithoutAlias),
                            DamSchemaUtils.fieldWithMissingInfo(column.getColumnName())
                    );
                }
            }));
            logger.fine("schema: (table -> fieldDef)" + dataFieldsByTable);
        }
        return new ReferencedSchemaFieldsInfo("select", dataFieldsByTable, controlFieldsByTable);
    }

    protected Table resolveColumnTableOrFail(Column column, List<String> tableList){
        Table defaultColumnTable = column.getTable();
        if (defaultColumnTable == null ) {
            if (tableList.size() == 1){
                defaultColumnTable = new Table(tableList.get(0)); // FIXME: database name! FIXME: qualified name twice?
            } else {
                throw new RuntimeException("ambigous column reference (at least without access to schemas)");
            }
        }
        return defaultColumnTable;
    }
}

class TableNamesAndAliasesFinder extends TablesNamesFinder {

    private final ConcurrentHashMap<String, Table> aliasToTable = new ConcurrentHashMap<>();

    public Table getTableWithoutAlias(Table table) {
        String tableWholeName = extractTableName(table);
        return aliasToTable.getOrDefault(tableWholeName, table);
    }

    @Override
    public void visit(Table tableName) {
        Alias tableAlias = tableName.getAlias();
        if (tableAlias != null){
            String aliasName = tableAlias.getName();
            aliasToTable.put(aliasName, tableName);
        }
        super.visit(tableName);
    }
}
