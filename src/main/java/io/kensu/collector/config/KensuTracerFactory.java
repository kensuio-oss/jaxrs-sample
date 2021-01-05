package io.kensu.collector.config;

import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerFactory;
import io.opentracing.noop.NoopTracerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.jaegertracing.Configuration;
import io.opentracing.contrib.reporter.TracerR;

@Priority(0)
public class KensuTracerFactory implements TracerFactory {
    static final Logger logger = Logger.getLogger(KensuTracerFactory.class.getName());

    @Produces 
    @ApplicationScoped 
    @Override
    public Tracer getTracer() {
        Tracer backendTracer = null;
        try {
            // TODO Deal with existing reporter via Widlfy default-tracer for example
            System.setProperty("JAEGER_SERVICE_NAME", "jaxr-sample"); // properties are used first
            backendTracer = Configuration.fromEnv().getTracer();
        } catch (Exception e) {
            logger.warning("Can't create Jaeger as var env are not set, and we don't support default-tracer from Wildly yet");
            backendTracer = NoopTracerFactory.create();
        }

        Properties properties = null;
        try {
            properties = getProperties();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


        //properties.getProperty("git.build.time")
        // ---
        //git.branch=master
        //git.build.host=Andys-Air
        //git.build.time=2021-01-03T21\:40\:23+01\:00
        //git.build.user.email=andy.petrella@kensu.io
        //git.build.user.name=Andy Petrella
        //git.build.version=1.3.0
        //git.closest.tag.commit.count=
        //git.closest.tag.name=
        //git.commit.id=148c8b67ff8c06c675428b165e1ba5c1fac2fdec
        //git.commit.id.abbrev=148c8b6
        //git.commit.id.describe=148c8b6-dirty
        //git.commit.id.describe-short=148c8b6-dirty
        //git.commit.message.full=added join and grouping query + avoid client code to crash if collector stuff crashes in the TracerReporter
        //git.commit.message.short=added join and grouping query + avoid client code to crash if collector stuff crashes in the TracerReporter
        //git.commit.time=2021-01-02T21\:59\:08+01\:00
        //git.commit.user.email=andy.petrella@kensu.io
        //git.commit.user.name=Andy Petrella
        //git.dirty=true
        //git.local.branch.ahead=0
        //git.local.branch.behind=0
        //git.remote.origin.url=https\://github.com/kensuio-oss/jaxrs-sample.git
        //git.tags=
        //git.total.commit.count=21
        //app.artifactId=jaxrs-sample
        //app.version=1.0.0

        // FIXME only for testing...

        //System.setProperty("DAM_OFFLINE", "true");
        //System.setProperty("DAM_OFFLINE_FILE_NAME", "dim.jsonl");

        System.setProperty("DAM_OFFLINE", "false");
        String kensuToken = "eyJhbGciOiJIUzI1NiJ9.eyIkaW50X3Blcm1zIjpbXSwic3ViIjoib3JnLnBhYzRqLmNvcmUucHJvZmlsZS5Db21tb25Qcm9maWxlI2FuZHkiLCJ0b2tlbl9pZCI6ImZhNGI4NzBlLTk4YjYtNDBlYi05OWFiLTJjMmQ4M2FmMWJjMyIsImFwcGdyb3VwX2lkIjoiM2I0NjE1MTktZjkxMy00OTgyLTllMWUtODM4NWRkNjhkZmFiIiwiJGludF9yb2xlcyI6WyJhcHAiXSwiZXhwIjoxOTI1MTIzNjAwLCJpYXQiOjE2MDk3NjM2MDB9.9s15uqcmL9ZY67qi4TzKz9_fMHkbgjBDHBqPQ8QbecQ";
        String DAM_INGESTION_URL = "https://api.qa3.464n.com";

        //String kensuToken = "eyJhbGciOiJIUzI1NiJ9.eyIkaW50X3Blcm1zIjpbXSwic3ViIjoib3JnLnBhYzRqLmNvcmUucHJvZmlsZS5Db21tb25Qcm9maWxlI2FuZHkiLCJ0b2tlbl9pZCI6ImJjYTAwMTU4LTNiNjgtNDAzZi05OTk2LWNjNjExOTg1MDYxZCIsImFwcGdyb3VwX2lkIjoiNTRiNmY2NTctMTljZC00MDAzLTk5ZTQtZDc5ZTMyZjVkM2ZhIiwiJGludF9yb2xlcyI6WyJhcHAiXSwiZXhwIjoxOTI1MTM0MjgwLCJpYXQiOjE2MDk3NzQyODB9.vQ280k1C4GYclSHFUa4kDtv-d922SS1M88dPQQVzOEY";
        //String DAM_INGESTION_URL = "https://api-demo102.usnek.com";

        //String kensuToken = "eyJhbGciOiJIUzI1NiJ9.eyIkaW50X3Blcm1zIjpbXSwic3ViIjoib3JnLnBhYzRqLmNvcmUucHJvZmlsZS5Db21tb25Qcm9maWxlI2pvZWwubmdhdG91byIsInRva2VuX2lkIjoiMTZjODQ2ODUtMzQzZS00MGViLWIzNDktNDg3ZDA1OTc0MjY2IiwiYXBwZ3JvdXBfaWQiOiJkMWVlYmIyNS04YTE0LTRkNWQtODc4Zi1jYTczYTE2NDU1OWYiLCIkaW50X3JvbGVzIjpbImFwcCJdLCJleHAiOjE5MjMzMjYxNjUsImlhdCI6MTYwNzk2NjE2NX0.iiRqYsG8wPX8ROtDwtZYNGspAcmJ7e2tQ8qn_JGGods";
        //String DAM_INGESTION_URL = "https://api.qa7.464n.com/";

        System.setProperty("DAM_INGESTION_URL", DAM_INGESTION_URL);
        String DAM_AUTH_TOKEN = kensuToken;
        System.setProperty("DAM_AUTH_TOKEN", DAM_AUTH_TOKEN);
        String DAM_PROCESS_NAME = properties.getProperty("app.artifactId");
        System.setProperty("DAM_PROCESS_NAME", DAM_PROCESS_NAME);
        String DAM_USER_NAME = System.getenv("USER");
        System.setProperty("DAM_USER_NAME", DAM_USER_NAME);
        String DAM_RUN_ENVIRONMENT = "TEST";
        System.setProperty("DAM_RUN_ENVIRONMENT", DAM_RUN_ENVIRONMENT);
        String DAM_PROJECTS = "blink";
        System.setProperty("DAM_PROJECTS", DAM_PROJECTS);
        String DAM_CODEBASE_LOCATION = properties.getProperty("git.remote.origin.url");
        System.setProperty("DAM_CODEBASE_LOCATION", DAM_CODEBASE_LOCATION);
        String DAM_CODE_VERSION = properties.getProperty("app.version")+"_"+properties.getProperty("git.commit.id.describe-short");
        System.setProperty("DAM_CODE_VERSION", DAM_CODE_VERSION);

        io.kensu.collector.TracerReporter reporter = new io.kensu.collector.TracerReporter();

        // JDBC
        io.opentracing.contrib.jdbc.TracingDriver.setInterceptorMode(true);
        io.opentracing.contrib.jdbc.TracingDriver.setTraceEnabled(true);
        io.opentracing.contrib.jdbc.TracingDriver.setInterceptorProperty(false);

        Tracer javaTracer = new TracerR(backendTracer, reporter, backendTracer.scopeManager());        
        return javaTracer;
    }
    private Properties getProperties() throws IOException {

        Properties properties = new Properties();

        try (InputStream inputStream = this.getClass().getResourceAsStream("/git.properties")) {
            if (inputStream == null)
                throw new IOException("Can't locate properties file to generate version info");

            properties.load(inputStream);

        }
        Properties appProperties = new Properties();

        try (InputStream inputStream = this.getClass().getResourceAsStream("/app.properties")) {
            if (inputStream == null)
                throw new IOException("Can't locate app.properties file to generate app info");

            appProperties.load(inputStream);
        }

        properties.putAll(appProperties);
        return properties;
    }
}
