package com.devueda.libraryeventsconsumer.service;

import com.devueda.libraryeventsconsumer.model.LibraryEvent;
import com.devueda.libraryeventsconsumer.model.LibraryEventType;
import com.devueda.libraryeventsconsumer.repository.LibraryEventsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Optional;

@Service
@Slf4j
public class LibraryEventsService {

    private LibraryEventsRepository libraryEventRepository;
    private ObjectMapper objectMapper;
    private KafkaTemplate<Integer, String> kafkaTemplate;

    public LibraryEventsService(LibraryEventsRepository libraryEventRepository, ObjectMapper objectMapper, KafkaTemplate<Integer, String> kafkaTemplate) {
        this.libraryEventRepository = libraryEventRepository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("Library Event {}", libraryEvent);
        if(libraryEvent.getId() != null && libraryEvent.getId() == 000) {
            throw new RecoverableDataAccessException("Temporary Network Issue.");
        }
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

    public void handleRecovery(ConsumerRecord<Integer, String> consumerRecord) {
        Integer  key = consumerRecord.key();
        String message = consumerRecord.value();
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, message);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, message, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, message, result);
            }
        });
    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Error Sending the Message and the exception is {}", ex.getMessage());
        try {
            throw ex;
        } catch(Throwable throwable) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message sent successfully for the key: {} and the value is {}, partition is {}", key, value, result.getRecordMetadata().partition());
    }
}
