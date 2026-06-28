package com.abhaypanday.provisioning;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.util.backoff.FixedBackOff;


@Configuration
public class KafkaConfig {

    public static final String REQUESTED = "provisioning.requested";
    public static final String NUMBER_RESERVED = "provisioning.number-reserved";
    public static final String RELEASE_NUMBER = "provisioning.release-number";

    @Bean
    NewTopic requestedTopic() {
        return TopicBuilder.name(REQUESTED).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic numberReservedTopic() {
        return TopicBuilder.name(NUMBER_RESERVED).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic releaseNumberTopic() {
        return TopicBuilder.name(RELEASE_NUMBER).partitions(3).replicas(1).build();
    }

    @Bean
    DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (ConsumerRecord<?, ?> record, Exception ex) ->
                        new TopicPartition(record.topic() + ".DLT", record.partition()));
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
    }
}
