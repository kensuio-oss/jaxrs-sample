package io.kensu.collector.interceptors;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import io.kensu.dim.client.model.FieldDef;
import io.kensu.collector.utils.GenericTag;
import io.kensu.collector.utils.json.KensuJsonSchemaInferrer;
import org.apache.commons.io.output.TeeOutputStream;

import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;


/**
 * In JAX-RS, this method is needed to log responses, <b>after</b> they have been committed.  It
 * isn't possible to use the ContainerResponseFilter to do what we want because that filter is called
 * <b>before</b> the response is actually written.  That's great for things like additional headers, but
 * it won't do us any good for logging.
 *
 * This class is marked with @Logged so that it pays attention to the annotation.
 * 
 */
@Provider
public class ResponseInterceptor implements WriterInterceptor {
    private static final Logger logger = Logger.getLogger(ResponseInterceptor.class.getName());

    // TODO that would be cool
    public static class KensuLiveOutputStreamInterceptor extends OutputStream {
        PipedInputStream in;
        PipedOutputStream out;
        public KensuLiveOutputStreamInterceptor() throws IOException {
            in = new PipedInputStream();
            out = new PipedOutputStream(in);
            new Thread(() -> {
                try {
                    // TODO schema and stats could be built in streaming mode
                    //  => so no need to reprocess the whole stream again
                    JsonFactory fac = new JsonFactory();
                    final JsonParser jsonParser = fac.createParser(in);
                    JsonToken token;
                    while((token = jsonParser.nextToken()) != null) {
                        if (token == JsonToken.START_OBJECT) {
                        }
                    }
                    jsonParser.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }

        @Override
        public void close() throws IOException {
            out.close();
            in.close();
        }
    }

    public static class KensuEagerOutputStreamInterceptor extends OutputStream {
        ByteArrayOutputStream out;
        private final String encoding;
        private final Span span;

        public KensuEagerOutputStreamInterceptor(String encoding, Span span) throws IOException {
            this.encoding = encoding;
            this.span = span;
            out = new ByteArrayOutputStream();
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }

        @Override
        public void close() throws IOException {
            out.close();
            String content = out.toString(this.encoding);
            Entry<Set<FieldDef>, Map<String, Map<String, Double>>> inferredSchemaAndStats = KensuJsonSchemaInferrer.inferSchemaAndStats(content);
            this.span.setTag(new GenericTag<Set<FieldDef>>("response.schema"), inferredSchemaAndStats.getKey());
            this.span.setTag(new GenericTag<Map<String, Map<String, Double>>>("response.stats"), inferredSchemaAndStats.getValue());
        }
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        MediaType mediaType = MediaType.WILDCARD_TYPE;
        Object contentType = context.getHeaders().getFirst("Content-Type");
        if( contentType != null ) {
            if( contentType instanceof MediaType)
                mediaType = (MediaType) contentType;
            else if( contentType instanceof String )
                mediaType = MediaType.valueOf((String)(contentType));
        }
        TeeOutputStream tee = null;
        if( mediaType.getType().startsWith("text") || mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
            Span span = GlobalTracer.get().activeSpan();
            if (span != null) {
                try {
                    OutputStream originalStream = context.getOutputStream();
                    String contentEncoding = Optional.ofNullable(context.getHeaders().getFirst("Content-Encoding"))
                                                        .map(e -> e.toString()).orElse("UTF-8");

                    OutputStream interceptStream = new KensuEagerOutputStreamInterceptor(contentEncoding, span);
                    tee = new TeeOutputStream(originalStream, interceptStream);
                    context.setOutputStream(tee);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error while processing response stream for Kensu", e);
                }
            } else {
                logger.warning("Can't log response schema as there is no active span...");
            }
        } else {
            logger.fine("response body is of type " + mediaType.toString());
        }
        try {
            context.proceed();
        } finally {
            if (tee != null) tee.close();
        }
    }
}
