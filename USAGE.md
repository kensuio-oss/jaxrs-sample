
ZIPKIN UI (not used in source) [http://127.0.0.1:9411/zipkin/](http://127.0.0.1:9411/zipkin/)


# where the app runs
```bash
docker exec -it ksu-wildfly bash
cat dim.jsonl
```

some endpoints working only my MySQL:

- http://127.0.0.1:8080/rest/v1/product-line/Motorcycles
- http://127.0.0.1:8080/rest/v1/customers/big-ones?greaterThanAmount=600


JAEGER UI [http://localhost:16686/search](http://localhost:16686/search)
