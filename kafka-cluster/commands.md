1. Run Zookeeper:
`zookeeper-server-start.sh kafka-cluster/zookeeper.properties`

2. Run the first broker:
`kafka-server-start.sh kafka-cluster/server1.properties`

3. Run the second broker:
`kafka-server-start.sh kafka-cluster/server2.properties`

4. Run the third broker:
`kafka-server-start.sh kafka-cluster/server3.properties`

5. Create topics:  
- github-accounts - accounts read from the file
```
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic github-accounts \
 --partitions 3 --replication-factor 3
```
- github-commits - commits which are polled from Github using github-accounts
```
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic github-commits \
--partitions 3 --replication-factor 3
 ```
- metrics topics
```
kafka-topics.sh --bootstrap-server localhost:9092 --create \
--topic github-metrics-total-commits --partitions 2 --replication-factor 3
```
```
kafka-topics.sh --bootstrap-server localhost:9092 --create \
--topic github-metrics-total-committers --partitions 2 --replication-factor 3
```
```
kafka-topics.sh --bootstrap-server localhost:9092 --create \
--topic github-metrics-top-committers --partitions 2 --replication-factor 3
```
```
kafka-topics.sh --bootstrap-server localhost:9092 --create \
--topic github-metrics-languages --partitions 2 --replication-factor 3
```