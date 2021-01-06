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
        Properties properties = null;
        try {
            properties = getProperties();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        Tracer backendTracer = null;
        try {
            // TODO Deal with existing reporter via Widlfy default-tracer for example
            System.setProperty("JAEGER_SERVICE_NAME", properties.getProperty("app.artifactId")); // properties are used first
            backendTracer = Configuration.fromEnv().getTracer();
        } catch (Exception e) {
            logger.warning("Can't create Jaeger as var env are not set, and we don't support default-tracer from Wildly yet");
            backendTracer = NoopTracerFactory.create();
        }

        // Configure Kensu Tracer
        System.setProperty("DAM_OFFLINE", properties.getProperty("kensu.collector.api.offline.switch"));
        System.setProperty("DAM_OFFLINE_FILE_NAME", properties.getProperty("kensu.collector.api.offline.file"));
        System.setProperty("DAM_INGESTION_URL", properties.getProperty("kensu.collector.api.url"));
        System.setProperty("DAM_AUTH_TOKEN", properties.getProperty("kensu.collector.api.token"));
        System.setProperty("DAM_USER_NAME", properties.getProperty("kensu.collector.run.user", System.getenv("USER")));
        System.setProperty("DAM_RUN_ENVIRONMENT", properties.getProperty("kensu.collector.run.env"));
        System.setProperty("DAM_PROJECTS", String.join(";", properties.getProperty("kensu.collector.run.projects","")));
        System.setProperty("DAM_PROCESS_NAME", properties.getProperty("app.artifactId"));
        System.setProperty("DAM_CODEBASE_LOCATION", properties.getProperty("git.remote.origin.url"));
        System.setProperty("DAM_CODE_VERSION", properties.getProperty("app.version")+"_"+properties.getProperty("git.commit.id.describe-short"));
        // Instantiate Kensu Tracer
        io.kensu.collector.TracerReporter reporter = new io.kensu.collector.TracerReporter();

        // Configure JDBC Open Tracer
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

        Properties kensuTracerProperties = new Properties();
        try (InputStream inputStream = this.getClass().getResourceAsStream("/kensu-tracer.properties")) {
            if (inputStream == null)
                throw new IOException("Can't locate kensu-tracer.properties file to generate app info");

            kensuTracerProperties.load(inputStream);
        }

        properties.putAll(appProperties);
        properties.putAll(kensuTracerProperties);
        return properties;
    }
}
