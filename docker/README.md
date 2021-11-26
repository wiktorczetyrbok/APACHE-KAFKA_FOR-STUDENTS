# Optional Docker Instructions

Optionally docker environment can be used to run the project.

To init the whole environment:

`docker-compose up`

This will bring up all nodes needed for testing (kafka, kafka-connect, ksql, etc). For details check docker-compose.yml. Containers which are not used (like schema registry) can be commented out). All usual ports are exposed outside of docker network so kafka streams application running in IDE can also access them.

 Kafka commands can be run from outside using docker exec.

`docker exec -it kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic github-commits`



