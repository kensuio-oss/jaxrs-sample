package com.hotjoe.services.listener;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.mail.search.ReceivedDateTerm;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.InterceptorContext;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptorContext;

import io.kensu.utils.GenericTag;
import org.jboss.resteasy.core.ResourceInvoker;
import org.jboss.resteasy.core.ResourceMethodRegistry;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.core.interception.PostMatchContainerRequestContext;
import org.jboss.resteasy.plugins.server.servlet.HttpServletInputMessage;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.util.GlobalTracer;
import io.opentracing.contrib.jaxrs2.serialization.InterceptorSpanDecorator;
import io.opentracing.contrib.jaxrs2.server.ServerSpanDecorator;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.contrib.jaxrs2.server.ServerTracingInterceptor;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature.Builder;

@Provider
public class KensuTracingInterceptorFeature implements DynamicFeature {
    private static final Logger logger = Logger.getLogger(KensuTracingInterceptorFeature.class.getName());

    private final ServerTracingDynamicFeature tracingFeature;

    public KensuTracingInterceptorFeature() {
        Builder builder = new ServerTracingDynamicFeature.Builder(GlobalTracer.get())
                .withDecorators(List.of(ServerSpanDecorator.STANDARD_TAGS, decorator)).withTraceSerialization(true)
                .withSerializationDecorators(List.of(InterceptorSpanDecorator.STANDARD_TAGS, serDecorator));
        this.tracingFeature = builder.build();
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        tracingFeature.configure(resourceInfo, context);
    }

    private ServerSpanDecorator decorator = new ServerSpanDecorator() {

        @Override
        public void decorateRequest(ContainerRequestContext requestContext, Span span) {
            if (requestContext instanceof PostMatchContainerRequestContext) {
                PostMatchContainerRequestContext post = (PostMatchContainerRequestContext) requestContext;
                ResourceInvoker resourceInvoker = post.getResourceMethod();
                try {
                    Registry registry = (Registry) ResteasyProviderFactory.getContextDataMap().get(Registry.class);
                    String urlPattern = null;
                    if (registry instanceof ResourceMethodRegistry) {
                        ResourceMethodRegistry r = (ResourceMethodRegistry) registry;
                        Map<String, List<ResourceInvoker>> bounded = r.getBounded();
                        for (Map.Entry<String, List<ResourceInvoker>> bs : bounded.entrySet()) {
                            if (bs.getValue().contains(resourceInvoker)) {
                                urlPattern = bs.getKey();
                                break;
                            }
                        }
                    }
                    MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo().getPathParameters();
                    MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo().getQueryParameters();
                    //TODO what about the BODY... ?  => requestContext.getEntityStream()
                    Optional<String> queryParamsPattern = queryParameters.keySet().stream().map(a->a+"={"+a+"}").reduce((a,b)->a+"&"+b);
                    span.setTag("http.request.url.path.pattern", urlPattern+queryParamsPattern.map(a->"?"+a).orElse(""));
                    span.setTag(new GenericTag<Set<String>>("http.request.url.path.parameters"), pathParameters.keySet());
                    span.setTag(new GenericTag<Set<String>>("http.request.url.query.parameters"), queryParameters.keySet());
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Can't find the URL Pattern for current request. Msg: " + e.getMessage(), e);
                }
            }

        }

        @Override
        public void decorateResponse(ContainerResponseContext responseContext, Span span) {
        }

    };

    private InterceptorSpanDecorator serDecorator = new InterceptorSpanDecorator() {
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
                // can check type of entity with getType => java.util.ArrayList (e.g.)
                logger.info("Entity:" + c.getEntity());
                // TODO extract schema from entity...
                // TODO extract stats from entity
            }
        }
    };

}
