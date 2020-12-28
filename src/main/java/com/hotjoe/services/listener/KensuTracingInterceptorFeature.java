package com.hotjoe.services.listener;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Priorities;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.InterceptorContext;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptorContext;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.util.GlobalTracer;
import io.opentracing.contrib.jaxrs2.serialization.InterceptorSpanDecorator;
import io.opentracing.contrib.jaxrs2.server.ServerTracingInterceptor;

@Provider
public class KensuTracingInterceptorFeature implements DynamicFeature {
    private static final Logger logger = Logger.getLogger(KensuTracingInterceptorFeature.class.getName());

    private InterceptorSpanDecorator decorator = new InterceptorSpanDecorator() {
        /**
         * Decorate spans by outgoing object.
         *
         * @param context
         * @param span
         */
        public void decorateRead(InterceptorContext context, Span span) {
            // nothing
        }

        /**
         * Decorate spans by outgoing object.
         *
         * @param context
         * @param span
         */
        public void decorateWrite(InterceptorContext context, Span span) {
            if (context instanceof WriterInterceptorContext) {
                WriterInterceptorContext c = (WriterInterceptorContext) context;
                // can check format with c.getMediaType() == "application/json"
                // can check type of entity xith getType => java.util.ArrayList (e.g.)
                logger.info("Entity:" + c.getEntity());
                // TODO extract schema from entity...
                // TODO extract stats from entity
            }
        }
    };

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        context.register(new ServerTracingInterceptor(GlobalTracer.get(), List.of(decorator)), Priorities.ENTITY_CODER);
    }
}
