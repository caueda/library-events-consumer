package com.devueda.libraryeventsconsumer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
@EnableKafka
public class LibraryEventConsumerConfig {
    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
                                                                                ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
//        The code below will start three consumer threads. It is not necessary
//        when running in a cloud environment.
//        factory.setConcurrency(3);
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setErrorHandler((thrownException, data) -> {
            log.info("Exception in consumerConfig is {} and the record is {}", thrownException.getMessage(), data);
            //You can persist the record, just in case you need to track
        });
//        factory.setRetryTemplate(retryTemplate());
        factory.setCommonErrorHandler(commonErrorHandler());
        return factory;
    }

    private CommonErrorHandler commonErrorHandler() {
        FixedBackOff fixedBackOff = new FixedBackOff(1000, FixedBackOff.UNLIMITED_ATTEMPTS);
        fixedBackOff.setMaxAttempts(3);
        CommonErrorHandler commonErrorHandler = new DefaultErrorHandler(fixedBackOff);
        return commonErrorHandler;
    }

    private RetryTemplate retryTemplate() {
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(1000);
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(simpleRetryPolicy());
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        return retryTemplate;
    }

    private RetryPolicy simpleRetryPolicy() {
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(3);
        return simpleRetryPolicy;
    }
}
