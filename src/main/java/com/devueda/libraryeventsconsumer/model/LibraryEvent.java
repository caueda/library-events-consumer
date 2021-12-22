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
    @OneToOne(mappedBy = "libraryEvent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    private Book book;
    @Enumerated(EnumType.STRING)
    private LibraryEventType libraryEventType;
}
