# https://zipkin.io/pages/quickstart.html
if [ ! -f ./zipkin.jar ]; then
    echo "File zipkin.jar not found! Downloading & extracting it..."
    curl -sSL https://zipkin.io/quickstart.sh | bash -s
fi

# uncomment to use cassandra storage
# export STORAGE_TYPE=cassandra3
java -jar zipkin.jar
