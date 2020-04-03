1. Run the first worker:
`connect-distributed.sh kafka-connect/worker1.properties`

2. Run the second worker:
`connect-distributed.sh kafka-connect/worker2.properties`

3. Create a FileStream Source Connector reading from `/tmp/github-accounts.txt` file:
```bash
curl -H 'Content-Type: application/json' \
-X POST -d '{
    "name": "filestream-source-kafka-connector",
    "config": {
        "connector.class": "FileStreamSource",
        "tasks.max": 1,
        "file": "/tmp/github-accounts.txt",
        "topic": "github-accounts"
    }
}' \
http://localhost:8083/connectors
```

4. Create a FileStream Sink Connector writing to `/tmp/github-metrics.txt` file:
```bash
curl -H 'Content-Type: application/json' \
-X POST -d '{
    "name": "filestream-sink-kafka-connector",
    "config": {
        "connector.class": "FileStreamSink",
        "tasks.max": 1,
        "topics": "github-metrics-total-commits,github-metrics-total-committers,github-metrics-top-committers",
        "file": "/tmp/github-metrics.txt"
    }
}' \
http://localhost:8083/connectors
```
