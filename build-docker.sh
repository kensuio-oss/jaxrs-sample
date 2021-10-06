git clone git@github.com:kensuio/dim-client-java-resteasy-jackson.git
# for simple run: 
(cd cd dim-client-java-resteasy-jackson && git checkout main &&  ./gradlew clean install )
# for including jboss EAP magic BOMs need maven
#(cd dim-client-java-resteasy-jackson && git checkout f-fix-jboss7.3 &&   mvn clean & mvn compile && mvn install )

git clone https://github.com/kensuio-oss/java-jdbc.git
(cd java-jdbc && git checkout ft/clikhouse-and-stats && mvn clean & mvn compile && mvn install)

(cd jaxrs-sample && mvn clean package && cp target/sample-service-1.3.0.war docker-dir/deployments/)

# FIXME: setup mysql...