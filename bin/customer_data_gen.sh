curl --request POST \
  --url http://localhost:8082/topics/Customer \
  --header 'accept: application/vnd.kafka.v2+json' \
  --header 'content-type: application/vnd.kafka.avro.v2+json' \
  --data '{
    "key_schema": "{\"name\":\"key\",\"type\": \"string\"}",
    "value_schema_id": "input the correct id here",
    "records": [
        {
            "key" : "678",
            "value": {
                "customerId": "678",
                "name": "11",
                "phoneNumber": "888-888-8888",
                "accountId": "0123"
            }
        }
    ]
}'