# JAX-RS Sample application for Wildfly
A very simple JAX-RS sample application that implements a few services.  The
services are meant purely as a demonstration and are not "real" services.

Development
----
In IDEA: https://www.jetbrains.com/help/idea/deploying-a-web-app-into-wildfly-container.html#Deploying_a_web_app_into_Wildfly_container-5-procedure


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

## Dependencies
Updated Java Client for Wildfly which uses RestEasy instead of Glassfish for the client part, therefore the java client 
has been regenerated (see https://www.baeldung.com/spring-boot-rest-client-swagger-codegen):
```
openapi-generator-cli generate -i dam-ingestion-api.json --api-package io.kensu.dim.client.api --model-package io.kensu.dim.client.model --invoker-package io.kensu.dim.client.invoker --group-id io.kensu.dim.client --artifact-id java-resteasy-jackson --artifact-version 1.0.0-SNAPSHOT -g java -p java8=true --library resteasy -o client-java-resteasy-jackson
```
