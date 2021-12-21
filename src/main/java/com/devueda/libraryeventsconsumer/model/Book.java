package com.devueda.libraryeventsconsumer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder
public class Book {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String author;
    @OneToOne
    @JoinColumn(name="id")
    private LibraryEvent libraryEvent;
}
