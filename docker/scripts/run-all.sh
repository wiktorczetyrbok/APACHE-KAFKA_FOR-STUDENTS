#!/bin/bash

set -e
pwd

echo "🧹 STEP 1: Cleaning topics..."
./clean-topics.sh

echo "🔌 STEP 2: Creating FileStream Source Connector..."
curl -s -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "filestream-source-kafka-connector",
    "config": {
      "connector.class": "FileStreamSource",
      "tasks.max": 1,
      "file": "/tmp/github-accounts.txt",
      "topic": "github-accounts"
    }
  }'
echo -e "\n✅ Source connector created."

echo "🔌 STEP 3: Creating FileStream Sink Connector..."
curl -s -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "filestream-sink-kafka-connector",
    "config": {
      "connector.class": "FileStreamSink",
      "tasks.max": 1,
      "topics": "github-metrics-total-commits,github-metrics-total-committers,github-metrics-top-committers,github-metrics-languages",
      "file": "/tmp/github-metrics.txt"
    }
  }'
echo -e "\n✅ Sink connector created."

echo "📊 STEP 4: Running KSQL setup statements..."
docker exec -i ksqldb-cli ksql http://ksqldb-server:8088 < setup-ksql-statements2.ksql

echo "✅ ALL DONE!"
