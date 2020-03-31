1. Run Zookeeper:
`zookeeper-server-start.sh kafka-cluster/zookeeper.properties`

2. Run the first broker:
`kafka-server-start.sh kafka-cluster/server1.properties`

3. Run the second broker:
`kafka-server-start.sh kafka-cluster/server2.properties`

4. Run the third broker:
`kafka-server-start.sh kafka-cluster/server3.properties`

5. Create topics:  
- `kafka-topics.sh --bootstrap-server localhost:9092 --create --topic github-accounts --partitions 3 --replication-factor 3`

