JAEGER UI [http://localhost:16686/search](http://localhost:16686/search)

ZIPKIN UI (not used in source) http://127.0.0.1:9411/zipkin/


# where the app runs
```bash
docker exec -it ksu-wildfly bash
cat dim.jsonl
```

for now only http://127.0.0.1:8080/rest/v1/customers/big-ones?greaterThanAmount=600 is working.
comm issue elsewhere. …
