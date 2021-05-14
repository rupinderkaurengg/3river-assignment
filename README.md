- Start Confluent Kafka using docker compose

      docker-compose up
     
 - Create Topics and Avro Schemas
 
      `./bin/create-topics-schemas.sh`
     
 - Run Streaming App
     - using Intellij
     - using maven 
        
       `mvn exec:java -Dexec.mainClass="com.ibm.gbs.CustomerBalanceKStreamJoinsDemo"` 
     
 - Run Data Gen
 
      `./bin/balance_data_gen.sh`
      `./bin/customer_data_gen.sh`