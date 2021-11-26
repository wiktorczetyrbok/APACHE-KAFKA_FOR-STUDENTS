# Optional Docker Instructions

## installing kafka connect plugin

dat source is github api consumed using rest connector

https://docs.github.com/en/rest/reference/repos#commits

building the connector and placing in the connectors dir

`mvn clean install && \
cp kafka-connect-rest-plugin/target/kafka-connect-rest-plugin-*.jar jars`

Jars need to be placed in /usr/share/java/ of kafka-connect docker container

## creating kafka connect connector using the plugin with kafka connect api

list available plugins that can be used as connectors:

`curl -s localhost:8083/connector-plugins|jq '.'`

add new connector:

`curl -i -X PUT -H "Content-Type:application/json" http://localhost:8083/connectors/my-github-rest-connector/config -d '{"connector.class":"com.tm.kafka.connect.rest.RestSourceConnector", "rest.source.url":"https://api.github.com/repos/homebrew/homebrew-core/commits?page=1&per_page=1", "value.converter":"org.apache.kafka.connect.json.JsonConverter", "rest.source.destination.topics":"github-commits", "rest.source.poll.interval.ms": "30000", "rest.source.method": "GET"}'`

check the connector's configuration
```
curl -i -X GET -H "Content-Type:application/json" http://localhost:8083/connectors/my-github-rest-connector/
curl -i -X GET -H "Content-Type:application/json" http://localhost:8083/connectors/my-github-rest-connector/pause
```

