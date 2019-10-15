#linux version
docker run --name=roach1 --net=host -p 26257:26257 -p 8080:8080  -v "${PWD}/cockroach-data/roach1:/cockroach/cockroach-data"  cockroachdb/cockroach:v19.1.5 start --insecure
