package com.example.books_api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for creating a new Book.
 *
 * Rules:
 * - `title` and `author` must not be empty.
 * - `publishedDate` must be provided in Buddhist calendar (BE),
 *   formatted as yyyy-MM-dd (e.g., 2568-09-14).
 */

public class BookPostRequest {

    @NotBlank(message = "title must not be empty")
    private String title;

    @NotBlank(message = "author must not be empty")
    private String author;

    @NotBlank(message = "publishedDate must not be empty")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "publishedDate must match yyyy-MM-dd")
    private String publishedDate; // BE

    public BookPostRequest() {}

    public BookPostRequest(String title, String author, String publishedDate) {
        this.title = title;
        this.author = author;
        this.publishedDate = publishedDate;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPublishedDate() { return publishedDate; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }
}
