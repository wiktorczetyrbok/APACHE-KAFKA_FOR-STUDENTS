# Github Metrics

## Usage

## Components
The project consists from the following high-level components.

### FileStream Source Kafka Connector
Reads the list of Github accounts from the file and writes them in Kafka.

### Github Commits Polling Stream
 - Receives topics with the specified accounts from Kafka,
 - gets the commits data using Github REST API,
 - writes the commits back into Kafka.

### Commits Metrics Stream
Analyzes commits from Kafka and writes back the metrics based on them.
Works with exactly-once semantics.

### FileStream Sink Kafka Connector
Reads the metrics from Kafka and writes it into the file.

## Project's structure
```

```
