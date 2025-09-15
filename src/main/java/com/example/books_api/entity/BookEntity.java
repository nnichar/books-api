package com.example.books_api.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_books_author", columnList = "author")
})
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(nullable = false)
    private LocalDate publishedDate;

    public BookEntity() {}

    public BookEntity(String title, String author, LocalDate publishedDate) {
        this.title = title;
        this.author = author;
        this.publishedDate = publishedDate;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public LocalDate getPublishedDate() { return publishedDate; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }
}
