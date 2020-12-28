package com.hotjoe.services.application;

import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerFactory;
import io.opentracing.noop.NoopTracerFactory;

import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
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
        io.kensu.collector.TracerReporter reporter = new io.kensu.collector.TracerReporter();
        Tracer javaTracer = new TracerR(backendTracer, reporter, backendTracer.scopeManager());        
        return javaTracer;
    }
   
}
