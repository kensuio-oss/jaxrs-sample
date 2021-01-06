package io.kensu.collector.config;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

// import io.jaegertracing.*;
// import io.jaegertracing.crossdock.thrift.TracedService.AsyncProcessor.joinTrace;
// import io.jaegertracing.internal.samplers.ProbabilisticSampler;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

/********
 * NOT SURE THIS ONE IS REALLY NEEDED
 */

@WebListener
public class TracingContextListener implements ServletContextListener {
  static final Logger logger = Logger.getLogger(TracingContextListener.class.getName());

  @Inject Tracer tracer;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    //io.opentracing.Tracer tracer = javax.enterprise.inject.spi.CDI.current().select(io.opentracing.Tracer.class).get();
    logger.info("Tracer CDI injected is:" + tracer);
    if (!GlobalTracer.isRegistered()) {
      // trying to get the tracer set by Wildfly / JBOSS via CDI (beans.xml)
      logger.info("Global tracer not registered yet, registering the CDI injected one");
      GlobalTracer.register(tracer);
    } else {
      logger.info("Global tracer already registered:" + GlobalTracer.get());
      if (!GlobalTracer.get().equals(tracer)) {
        if (tracer == null) {
          logger.info("Global tracer is registered but CDI injected Tracer is NULL !!!...");
          // Ugly but need to return early here
          return;
        } else {
          logger.info("Global tracer registered is not the CDI injected one, so we're replacing it");
          GlobalTracer.register(tracer);
        }
      } else {
        logger.info("Global tracer registered is the CDI injected one, so we're good");
      }
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {}
}