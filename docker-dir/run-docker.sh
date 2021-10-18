# if [ ! -f ./hits_v1.tsv ]; then
#     echo "warning this will create two files taking 10GB!"
#     echo "File hits_v1.tsv not found! Downloading & extracting it..."
#     curl -L https://datasets.clickhouse.tech/hits/tsv/hits_v1.tsv.xz | unxz --threads=`nproc` > hits_v1.tsv
# fi

# if [ ! -f ./visits_v1.tsv ]; then
#     echo "warning this will create two files taking 10GB!"
#     echo "File visits_v1.tsv not found! Downloading & extracting it..."
#     curl -L https://datasets.clickhouse.tech/visits/tsv/visits_v1.tsv.xz | unxz --threads=`nproc` > visits_v1.tsv
# fi



docker compose -f infra.yml stop
docker compose -f infra.yml rm --force
docker compose -f infra.yml up
