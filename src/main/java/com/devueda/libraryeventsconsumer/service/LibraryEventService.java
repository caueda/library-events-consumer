package com.devueda.libraryeventsconsumer.service;

import com.devueda.libraryeventsconsumer.model.LibraryEvent;
import com.devueda.libraryeventsconsumer.model.LibraryEventType;
import com.devueda.libraryeventsconsumer.repository.LibraryEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LibraryEventService {

    private LibraryEventRepository libraryEventRepository;
    private ObjectMapper objectMapper;

    public LibraryEventService(LibraryEventRepository libraryEventRepository, ObjectMapper objectMapper) {
        this.libraryEventRepository = libraryEventRepository;
        this.objectMapper = objectMapper;
    }

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("Library Event {}", libraryEvent);
        if(libraryEvent.getLibraryEventType().equals(LibraryEventType.NEW)) {
            save(libraryEvent);
        } else if(libraryEvent.getLibraryEventType().equals(LibraryEventType.UPDATE)) {

        } else {
            log.info("Invalid Library Event.");
        }
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventRepository.save(libraryEvent);
        log.info("Successfully Persisted the Library Event {}", libraryEvent);
    }
}