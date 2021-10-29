cd ..

git clone https://github.com/kensuio-oss/java-jdbc.git
(cd java-jdbc && git checkout ft/clikhouse-and-stats && mvn clean & mvn compile && mvn install)

git clone https://github.com/vidma/jaeger-client-java.git
(cd jaeger-client-java && git checkout gson-v1.6.0 && ./gradlew clean install publishToMavenLocal )

(cd jaxrs-sample && ./build-and-run-app.sh )
