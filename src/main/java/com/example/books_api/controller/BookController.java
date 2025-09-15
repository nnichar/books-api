package com.example.books_api.controller;

import com.example.books_api.model.BookDetail;
import com.example.books_api.model.BookPostRequest;
import com.example.books_api.model.BookPostResponse;
import com.example.books_api.model.PageResponse;
import com.example.books_api.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@Validated
public class BookController {

    private final BookService bookService;
    public BookController(BookService bookService) { this.bookService = bookService; }

    /**
     * POST /books
     * Accepts one JSON object (single book).
     * Field 'publishedDate' must be in Buddhist calendar (yyyy-MM-dd).
     * Returns 201 Created with generated id in response body.
     * Example request:
     * {
     *   "title": "Spring in Action",
     *   "author": "Steve",
     *   "publishedDate": "2568-09-14"
     * }
     */
    @PostMapping
    public ResponseEntity<BookPostResponse> saveBook(@Valid @RequestBody BookPostRequest request) {
        BookPostResponse resp = bookService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /**
     * GET /books?author={name}
     * Requirement: return books for the given author.
     * Optimized with index on 'author' column.
     * If 'author' is missing or blank -> throw 400 Bad Request.
     * Example: GET /books?author=Steve
     * Optional: GET /books?author=Steve&page=0&size=5&sort=publishedDate,desc
     */
    @GetMapping
    public ResponseEntity<PageResponse<BookDetail>> getBooksByAuthor(
            @RequestParam(value = "author") String author,
            @PageableDefault(size = 20, sort = "publishedDate") Pageable pageable
    ) {
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("author is required");
        }
        var page = bookService.fetchByAuthor(author, pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    /**
     * POST /books/bulk
     * Accepts an array of books (bulk insert).
     * Validates each item in the list.
     * Returns 201 Created with a list of generated ids.
     * Example request:
     * [
     *   { "title": "Book A", "author": "Alice", "publishedDate": "2566-01-01" },
     *   { "title": "Book B", "author": "Bob", "publishedDate": "2567-02-01" }
     * ]
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<BookPostResponse>> saveBooks(
            @Valid @RequestBody List<@Valid BookPostRequest> requests) {

        List<BookPostResponse> responses = requests.stream()
                .map(bookService::save)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * GET /books/all
     * Retrieve all books in the database.
     * Supports pagination & sorting using Spring's Pageable.
     * Example: GET /books/all
     * Optional: GET /books/all?page=0&size=10&sort=publishedDate,desc
     */
    @GetMapping("/all")
    public ResponseEntity<PageResponse<BookDetail>> getAllBooks(
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        var page = bookService.fetchAll(pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

}