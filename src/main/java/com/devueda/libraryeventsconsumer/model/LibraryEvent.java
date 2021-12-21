package com.devueda.libraryeventsconsumer.model;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder
public class LibraryEvent {
    @Id
    @GeneratedValue
    private Integer id;
    @OneToOne(mappedBy = "libraryEvent", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Book book;
    @Enumerated(EnumType.STRING)
    private LibraryEventType libraryEventType;
}
