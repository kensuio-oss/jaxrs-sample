# https://zipkin.io/pages/quickstart.html
if [ ! -f ./zipkin.jar ]; then
    echo "File hits_v1.tsv not found! Downloading & extracting it..."
    curl -sSL https://zipkin.io/quickstart.sh | bash -s
fi


# export STORAGE_TYPE=cassandra3
java -jar zipkin.jar
