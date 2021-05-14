package com.ibm.gbs;


import com.ibm.gbs.schema.Customer;
import com.ibm.gbs.schema.CustomerBalance;
import com.ibm.gbs.schema.Transaction;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

public class CustomerBalanceKStreamJoinsDemo {

    public static void main(String[] args) {
        CustomerBalanceKStreamJoinsDemo customerBalanceKStreamJoinsDemo = new CustomerBalanceKStreamJoinsDemo();
        customerBalanceKStreamJoinsDemo.join();
    }

    public void join() {
        Properties properties = loadProperties();
        Topology topology = runStreamOpJoin();

        KafkaStreams streams = new KafkaStreams(topology, properties);

        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    public Properties loadProperties() {
        Properties props = new Properties();
        props.put(KEY_DESERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG,
                io.confluent.kafka.serializers.KafkaAvroDeserializer.class);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "StreamJoinApp");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        return props;
    }

    public Topology runStreamOpJoin() {
        final Map<String, String> serdeConfig = Collections.singletonMap(SCHEMA_REGISTRY_URL_CONFIG,
                "http://localhost:8081");

        Serde<CustomerBalance> customerBalanceSerde = new SpecificAvroSerde<>();
        customerBalanceSerde.configure(serdeConfig, false);

        Serdes.StringSerde stringSerde = new Serdes.StringSerde();

        final Serde<Customer> customerSpecificAvroSerde = new SpecificAvroSerde<>();
        customerSpecificAvroSerde.configure(serdeConfig, false);

        final Serde<Transaction> transactionSpecificAvroSerde = new SpecificAvroSerde<>();
        transactionSpecificAvroSerde.configure(serdeConfig, false);

        StreamsBuilder builder = new StreamsBuilder();


        KStream<String, Customer> customerStream =
                builder.stream("Customer", Consumed.with(stringSerde, customerSpecificAvroSerde));
        KStream<String, Customer> keyCustomerStream = customerStream.map((key, customer) ->
                new KeyValue<>(String.valueOf(customer.getAccountId()), customer));

        KStream<String, Transaction> transactionStream =
                builder.stream("Balance", Consumed.with(stringSerde, transactionSpecificAvroSerde));
        KStream<String, Transaction> keyTransactionStream = transactionStream.map((key, transaction) ->
                new KeyValue<>(String.valueOf(transaction.getAccountId()), transaction));

        KStream<String, CustomerBalance> joined = keyCustomerStream.join(keyTransactionStream,
                (customer, balance) ->
                        CustomerBalance.newBuilder().
                                setAccountId(customer.getAccountId())
                                .setBalance(balance.getBalance())
                                .setPhoneNumber(customer.getPhoneNumber())
                                .setCustomerId(customer.getCustomerId())
                                .build(),
                JoinWindows.of(Duration.ofMinutes(5)),
                Joined.with(
                        Serdes.String(),
                        customerSpecificAvroSerde,
                        transactionSpecificAvroSerde)
        );

        joined.print(Printed.toSysOut());
        joined.to("CustomerBalance", Produced.with(stringSerde, customerBalanceSerde));

        final Topology topology = builder.build();
        return topology;
    }
}
