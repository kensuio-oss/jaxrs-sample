package io.jaegertracing.internal;

import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.spi.Reporter;

public class JaegerProxy {
    public static JaegerProxy INSTANCE = new JaegerProxy();

    public Reporter getJaegerReporter(JaegerTracer jagerTracer){
        return jagerTracer.getReporter();
    }
}
