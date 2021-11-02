package io.kensu.collector.config;

import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.spi.Reporter;
import io.jaegertracing.zipkin.ZipkinSender;
import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerFactory;
import io.opentracing.noop.NoopTracerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.jaegertracing.Configuration;
import zipkin2.codec.Encoding;
import zipkin2.reporter.urlconnection.URLConnectionSender;

class KensuJaegerSpanAnnotatingReporter implements Reporter {

    Reporter delegate;
    Map<String, String> kensuAnnotations;

    public KensuJaegerSpanAnnotatingReporter(Map<String, String> kensuAnnotations, Reporter delegate){
        this.delegate = delegate;
        this.kensuAnnotations = kensuAnnotations;
    }

    @Override
    public void report(JaegerSpan span) {
        for (Map.Entry<String, String> ann: this.kensuAnnotations.entrySet()){
            span.setTag(ann.getKey(), ann.getValue());
        }
        delegate.report(span);
    }

    @Override
    public void close() {
        delegate.close();
    }
}


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

        DamProcessEnvironment kensuEnv = new DamProcessEnvironment(properties);
        Map<String, String> kensuAnnotations = kensuEnv.getKensuAnnotations();
        Tracer backendTracer;
        try {
            // TODO Deal with existing reporter via Widlfy default-tracer for example
            System.setProperty("JAEGER_SERVICE_NAME", properties.getProperty("app.artifactId")); // properties are used first
            String kensuZipkinEndpoint =  kensuEnv.getKensuIngestionUrl();
            String zipkinDebuggingEndpoint = kensuEnv.getOptEnvOrProp("DEBUGGING_ZIPKIN_ENDPOINT", "zipkin.collector.url", null);

            Reporter kensuReporter =  buildZipkinReporter(
                    kensuZipkinEndpoint,
                    kensuEnv.getKensuIngestionToken(),
                    kensuAnnotations);

            Reporter debuggingReporter = null;
            if (zipkinDebuggingEndpoint != null) {
                debuggingReporter = buildZipkinReporter(zipkinDebuggingEndpoint, null, kensuAnnotations)
            }
            // FIXME: check, maybe jaeger is not needed at all anymore, and we could use Zipkin Tracer directly?
            Reporter zipkinReporter = combineReporters(kensuReporter, debuggingReporter);
            // doesn't work well like that - doesn't pick up the settings (like sampling) from env it seams
            //  backendTracer = new JaegerTracer.Builder(serviceName).withReporter(zipkinReporter).build();
            backendTracer = Configuration.fromEnv().getTracerBuilder().withReporter(zipkinReporter).build();
            // or we can also use original Jaeger tracer to report to Jaeger server
            // backendTracer = Configuration.fromEnv().getTracer();

        } catch (Exception e) {
            logger.warning("Can't create Jaeger as var env are not set, and we don't support default-tracer from Wildly yet");
            backendTracer = NoopTracerFactory.create();
        }

        // Configure JDBC Open Tracer
        io.opentracing.contrib.jdbc.TracingDriver.setInterceptorMode(true);
        io.opentracing.contrib.jdbc.TracingDriver.setTraceEnabled(true);
        io.opentracing.contrib.jdbc.TracingDriver.setInterceptorProperty(false);
        return backendTracer;
        // it seems TracerR is not needed anymore (for now at least, as zipkin/jaeger reporter has a composite one)
        //Tracer javaTracer = new TracerR(backendTracer, kensuReporter, backendTracer.scopeManager());
    }

    private Reporter combineReporters(Reporter main, Reporter optional) {
        if (optional == null) {
            return main;
        } else {
            return new io.jaegertracing.internal.reporters.CompositeReporter(
                    main,
                    optional
            );
        }
    }

    private KensuJaegerSpanAnnotatingReporter buildZipkinReporter(String zipkinEndpoint,
                                               String kensuAuthToken,
                                               Map<String, String> kensuAnnotations) throws MalformedURLException {
        // Add X-Auth-Token header
        // URLConnectionSender is final so cannot add a header outside of URLStreamHandler
        URL serverUrl;
        if (kensuAuthToken == null){
            serverUrl = new URL(zipkinEndpoint);
        } else {
            serverUrl = new URL(null, zipkinEndpoint, new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL u) throws IOException {
                    URLConnection conn = new URL(zipkinEndpoint).openConnection();
                    conn.addRequestProperty("X-Auth-Token", kensuAuthToken);
                    return conn;
                }
            });
        }
        // P.S. there's no non-thrift span converter. originally it uses String.valueOf for tag values...!
        RemoteReporter reporter =  new RemoteReporter.Builder()
                .withSender(ZipkinSender.create(
                        URLConnectionSender.newBuilder().encoding(Encoding.THRIFT).endpoint(serverUrl).build()
                ))
                .withFlushInterval(1) // FIXME
                .build();
        return new KensuJaegerSpanAnnotatingReporter(kensuAnnotations, reporter);
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
