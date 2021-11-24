WAR_FILE="./docker-dir/deployments/sample-service-1.3.1.war"
if [ ! -f "$WAR_FILE" ]; then
    echo "File $WAR_FILE not found!"
    echo "Downloading a prebuilt version from github releases ( https://github.com/kensuio-oss/jaxrs-sample/releases )..."
	# follow redirects
	curl --location --output $WAR_FILE https://github.com/kensuio-oss/jaxrs-sample/releases/download/1.3.1/sample-service-1.3.1.war
else
	echo "$WAR_FILE already exists, not downloading again."
fi

cd docker-dir
./run-docker.sh
