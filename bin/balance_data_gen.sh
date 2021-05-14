curl --request POST \
  --url http://localhost:8082/topics/Balance \
  --header 'accept: application/vnd.kafka.v2+json' \
  --header 'content-type: application/vnd.kafka.avro.v2+json' \
  --data '{
    "key_schema": "{\"name\":\"key\",\"type\": \"string\"}",
    "value_schema_id": "input the correct id here",
    "records": [
        {
            "key" : "0123",
            "value": {
                "balanceId": "0123",
                "accountId" : "11",
                "balance" : 2.34
            }
        }
    ]
}'