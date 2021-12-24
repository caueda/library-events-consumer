package com.devueda.libraryeventsconsumer.service;

import com.devueda.libraryeventsconsumer.model.LibraryEvent;
import com.devueda.libraryeventsconsumer.model.LibraryEventType;
import com.devueda.libraryeventsconsumer.repository.LibraryEventsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class LibraryEventsService {

    private LibraryEventsRepository libraryEventRepository;
    private ObjectMapper objectMapper;

    public LibraryEventsService(LibraryEventsRepository libraryEventRepository, ObjectMapper objectMapper) {
        this.libraryEventRepository = libraryEventRepository;
        this.objectMapper = objectMapper;
    }

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("Library Event {}", libraryEvent);
        if(libraryEvent.getLibraryEventType().equals(LibraryEventType.NEW)) {
            save(libraryEvent);
            log.info("library event saved {}", libraryEvent);
            log.info("library.book event saved {}", libraryEvent.getBook());
        } else if(libraryEvent.getLibraryEventType().equals(LibraryEventType.UPDATE)) {
            validate(libraryEvent);
        } else {
            log.info("Invalid Library Event.");
        }
    }

    private void validate(LibraryEvent libraryEvent) {
        if(libraryEvent.getId() == null) {
            throw new IllegalArgumentException("The Library Event Id is missing.");
        }

        Optional<LibraryEvent> libraryEventOptional = libraryEventRepository.findById(libraryEvent.getId());

        if(!libraryEventOptional.isPresent()) {
            throw new IllegalArgumentException("Not a valid Library Event.");
        }

        log.info("Validation is successfull for the Library Event: {}", libraryEventOptional.get());

        save(libraryEvent);
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventRepository.save(libraryEvent);
        log.info("Successfully Persisted the Library Event {}", libraryEvent);
    }
}