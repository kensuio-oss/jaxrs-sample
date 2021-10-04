#!/bin/bash

set -e


clickhouse client -n < /app/home/clickhouse-schema.sql

# --max_memory_usage=5000000000 
# max_insert_block_size - The size of blocks (in a count of rows) to form for insertion into a table.
clickhouse-client --query "INSERT INTO tutorial.visits_v1 FORMAT TSV" --max_insert_block_size=10000 < /app/home/visits_v1.tsv

clickhouse-client --query "INSERT INTO tutorial.hits_v1 FORMAT TSV" --max_insert_block_size=10000  < /app/home/hits_v1.tsv

