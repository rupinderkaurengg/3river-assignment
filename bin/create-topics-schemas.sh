docker exec -it broker kafka-topics --create --bootstrap-server localhost:9092 --topic Customer --partitions 2 --replication-factor 1
docker exec -it broker kafka-topics --create --bootstrap-server localhost:9092 --topic Balance --partitions 2 --replication-factor 1
docker exec -it broker kafka-topics --create --bootstrap-server localhost:9092 --topic CustomerBalance --partitions 2 --replication-factor 1


jq '. | {schema: tojson}' /Users/pannu/3river-assignment/src/main/resources/schema/balance.avsc  | curl -X POST http://localhost:8081/subjects/balance-value/versions -H "Content-Type:application/json" -d @-
jq '. | {schema: tojson}' /Users/pannu/3river-assignment/src/main/resources/schema/customer.avsc  | curl -X POST http://localhost:8081/subjects/customer-value/versions -H "Content-Type:application/json" -d @-
jq '. | {schema: tojson}' /Users/pannu/3river-assignment/src/main/resources/schema/customerBalance.avsc  | curl -X POST http://localhost:8081/subjects/customerBalance-value/versions -H "Content-Type:application/json" -d @-

