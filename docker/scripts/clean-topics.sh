#!/bin/bash

set -e

TOPICS=(
  github-accounts
  github-commits
  github-metrics-total-commits
  github-metrics-total-committers
  github-metrics-top-committers
  github-metrics-languages
)

echo "Deleting existing topics (if any)..."
for topic in "${TOPICS[@]}"; do
  echo "🗑️  Deleting topic: $topic"
  docker exec -it kafka1 kafka-topics \
    --bootstrap-server kafka1:29092 \
    --delete \
    --topic "$topic" || true
done

echo "Waiting for deletion to propagate..."
sleep 5

echo "Re-creating topics..."
docker exec -it kafka1 kafka-topics --create \
  --bootstrap-server kafka1:29092 \
  --topic github-accounts \
  --partitions 3 --replication-factor 3

docker exec -it kafka1 kafka-topics --create \
  --bootstrap-server kafka1:29092 \
  --topic github-commits \
  --partitions 3 --replication-factor 3

docker exec -it kafka1 kafka-topics --create \
  --bootstrap-server kafka1:29092 \
  --topic github-metrics-total-commits \
  --partitions 2 --replication-factor 3

docker exec -it kafka1 kafka-topics --create \
  --bootstrap-server kafka1:29092 \
  --topic github-metrics-total-committers \
  --partitions 2 --replication-factor 3

docker exec -it kafka1 kafka-topics --create \
  --bootstrap-server kafka1:29092 \
  --topic github-metrics-top-committers \
  --partitions 2 --replication-factor 3

docker exec -it kafka1 kafka-topics --create \
  --bootstrap-server kafka1:29092 \
  --topic github-metrics-languages \
  --partitions 2 --replication-factor 3

echo "All topics reset successfully!"
