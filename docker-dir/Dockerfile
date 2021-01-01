FROM jboss/wildfly

EXPOSE 9990

COPY --chown=jboss:jboss customization /opt/jboss/wildfly/customization/

#COPY ./deployments/sample-service-1.3.0.war /opt/jboss/wildfly/standalone/deployments/

RUN ["/opt/jboss/wildfly/bin/add-user.sh", "admin", "admin", "--silent"]

# RUN ["chmod", "+x", "/opt/jboss/wildfly/customization/execute.sh"]

# UseSSE=2 because docker on M1 seems to not support SSE3
ENV JAVA_OPTS="-XX:UseSSE=2 -Xmx2g ${JAVA_OPTS}"

#CMD /opt/jboss/wildfly/customization/execute.sh && /bin/bash
CMD /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 -c standalone.xml #> /dev/null &
