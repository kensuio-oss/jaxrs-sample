if [ ! -f ./docker-dir/deployments/sample-service-1.3.0.war ]; then
    echo "File ./docker-dir/deployments/sample-service-1.3.0.war not found!"
    echo "Downloading a prebuilt version from github releases ( https://github.com/kensuio-oss/jaxrs-sample/releases )..."

	# donwload prebuild sample app jar from releases, see https://github.com/kensuio-oss/jaxrs-sample/releases
	# follow redirects
	curl --location --output ./docker-dir/deployments/sample-service-1.3.0.war https://github.com/kensuio-oss/jaxrs-sample/releases/download/1.3.0/sample-service-1.3.0.war
else
	echo "./docker-dir/deployments/sample-service-1.3.0.war already exists, not downloading again."
fi

cd docker-dir
./run-docker.sh
