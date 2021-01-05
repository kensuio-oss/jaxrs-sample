# JAX-RS Sample application for Wildfly
A very simple JAX-RS sample application that implements a few services.  The
services are meant purely as a demonstration and are not "real" services.

Deployment
----

To build, simply run

```mvn clean package```

This will compile, run the unit tests, and create a war file that can be deployed into an JEE app server.  I've only
tested with Wildfly at this point.

The war file can be deployed to Wildfly and possibly other JEE application servers.
For Wildlfy, there are many options to deploy listed on
[the application deployment page](https://docs.jboss.org/author/display/WFLY10/Application+deployment)
that can help deploy.

