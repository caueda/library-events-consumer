package com.devueda.libraryeventsconsumer.consumer;

import com.devueda.libraryeventsconsumer.service.LibraryEventsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventsConsumer {

    private LibraryEventsService libraryEventService;

    public LibraryEventsConsumer(LibraryEventsService libraryEventService) {
        this.libraryEventService = libraryEventService;
    }

    @KafkaListener(topics = {"library-events"})
    public void onMessage(ConsumerRecord<Integer, String> integerStringConsumerRecord) throws JsonProcessingException {

        log.info("Consumer Record : {}", integerStringConsumerRecord);

        libraryEventService.processLibraryEvent(integerStringConsumerRecord);

    }
}
