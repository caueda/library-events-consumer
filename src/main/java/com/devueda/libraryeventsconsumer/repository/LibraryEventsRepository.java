package com.devueda.libraryeventsconsumer.repository;

import com.devueda.libraryeventsconsumer.model.LibraryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryEventsRepository extends JpaRepository<LibraryEvent, Integer> {
}
