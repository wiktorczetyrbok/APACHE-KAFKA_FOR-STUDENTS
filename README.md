# Github Metrics

- [Components](#components)
  * [FileStream Source Kafka Connector](#filestream-source-kafka-connector)
  * [Github Accounts Analyzer](#github-accounts-analyzer)
  * [Commits Metrics Kafka Stream](#commits-metrics-kafka-stream)
  * [FileStream Sink Kafka Connector](#filestream-sink-kafka-connector)
- [Usage](#usage)
- [Project's Structure](#project-s-structure)

## Components

The project consists from the following high-level components.

### FileStream Source Kafka Connector
Reads the list of Github accounts from the `/tmp/github-accounts.txt` file (JSON format) and writes them in Kafka in topic `github-accounts`.

### Github Accounts Analyzer

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.litmos.gridu.ilyavy%3Agithub-accounts-analyzer&metric=coverage)](https://sonarcloud.io/dashboard?id=com.litmos.gridu.ilyavy%3Agithub-accounts-analyzer)

Consumes github accounts and produces commits in topic `github-commits`:
 - Receives topics with the specified accounts from Kafka,
 - gets accounts' commits data using Github REST API,
 - writes the commits back into Kafka.

### Commits Metrics Kafka Stream

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.litmos.gridu.ilyavy%3Acommits-metrics-kafka-stream&metric=coverage)](https://sonarcloud.io/dashboard?id=com.litmos.gridu.ilyavy%3Acommits-metrics-kafka-stream)

Analyzes commits from Kafka and writes back the metrics based on them.
Works with exactly-once semantics.

Writes the corresponding metrics into the following topics:
- `github-metrics-total-commits`;
- `github-metrics-total-committers`;
- `github-metrics-top-committers`;
- `github-metrics-languages`.

### FileStream Sink Kafka Connector
Reads the metrics from Kafka and writes it into the `/tmp/github-metrics.txt` file (CSV format with `:` as a separator).

## Usage

It is assumed that all the commands are called from the root directory of the project.

1. At first, it's needed to run Kafka cluster and create the needed topics.
It can be done using the commands provided in `kafka-cluster/commands.md`,
the required properties files are provided there as well.

2. Then it's required to run Kafka Connect workers, and to create necessary connectors.
It can be done using the commands provided in `kafka-connect/commands.md`.

3. To build Java components of the system Maven is required:
```
mvn clean package
```

4. To run Github Accounts Analyzer use the following command:
```
java -jar github-accounts-analyzer/target/github-accounts-analyzer-1.0-jar-with-dependencies.jar
```

5. To run Commits Metrics:
```
java -jar commits-metrics-kafka-stream/target/commits-metrics-kafka-stream-1.0-jar-with-dependencies.jar
```

6. Now by writing into file `/tmp/github-accounts.txt` we can see the corresponding metrics in the file
`/tmp/github-metrics.txt`.
Example of the input for accounts file (it uses JSON syntax, each record should be at one line):
```
{"account": "ilyavy", "interval": "1d"}
```
All the metrics will be written in the file with metrics (it uses CSV format with `:` as a separator). Example:
```
total_commits: 1
Java: 1
top5_committers: ilyavy (1)
total_committers: 1
```

## Project's Structure
```
├── kafka-cluster   # properties files and CLI commands needed to run Kafka cluster
├── kafka-connect   # properties files and CLI commands needed to run Kafka Connect and create connectors
├── github-accounts-analyzer     # Github Accounts Analyzer
├── commits-metrics-kafka-stream # Commits Metrics Kafka Stream
├── modules-integration-test # End-2-End integration tests for modules of the project
```
