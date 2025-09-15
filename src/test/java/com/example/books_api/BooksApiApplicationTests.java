package com.example.books_api;

import com.example.books_api.repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BooksApiApplicationTests {

    // Run a real MySQL database inside Testcontainers (requires Docker)
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("booksdb")
            .withUsername("test")
            .withPassword("test");

    // Dynamically register datasource properties so Spring Boot connects to the containerized MySQL
    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.properties.hibernate.jdbc.time_zone", () -> "UTC");
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired BookRepository bookRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() {
        bookRepository.deleteAll();
    }

    /**
     * Test Case 1:
     * POST /books should accept Buddhist Era (BE) date
     * and persist it as Gregorian date in the database.
     * Example: BE 2568-09-14 -> AD 2025-09-14
     */
    @Test
    void createBook_acceptsBuddhistDate_andPersistsGregorian() throws Exception {
        String body = """
            { "title": "Spring in Action", "author": "Steve", "publishedDate": "2568-09-14" }
        """;

        String resp = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated()) // If your API returns 200 instead, change to isOk()
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(resp).get("id").asLong();

        // Verify that GET returns the date in Gregorian format
        mockMvc.perform(get("/books").param("author", "Steve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is((int) id)))
                .andExpect(jsonPath("$.content[0].publishedDate", is("2025-09-14")));
    }

    /**
     * Test Case 2:
     * Validation should reject empty title/author
     * and reject invalid date format.
     */
    @Test
    void validation_rejectsEmptyTitleAuthor_andBadFormatDate() throws Exception {
        String bad = """
            { "title": "", "author": "", "publishedDate": "25-09-14" }
        """;
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bad))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test Case 3:
     * Validation should reject dates with year <= 1000 (AD).
     */
    @Test
    void validation_rejectsYearTooSmall() throws Exception {
        // Example: AD 0999 -> BE 1542, should be rejected
        String badYear = """
            { "title": "Too Old", "author": "Anon", "publishedDate": "1542-01-01" }
        """;
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badYear))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test Case 4:
     * Validation should reject dates in the future (> current year).
     */
    @Test
    void validation_rejectsFutureYear() throws Exception {
        int currentAd = LocalDate.now().getYear();
        int futureAd = currentAd + 1;
        int futureBe = futureAd + 543; // Convert AD -> BE

        String badFuture = String.format("""
            { "title": "From the Future", "author": "Time", "publishedDate": "%d-01-01" }
        """, futureBe);

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badFuture))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test Case 5:
     * GET /books?author=... should return only books for that author.
     * Also verify pagination and sorting work correctly
     * (to avoid full table scans).
     */
    @Test
    void getByAuthor_returnsOnlyThatAuthor_withPaginationAndSort() throws Exception {
        // Seed data with multiple authors
        postBook("A1", "Alice", "2565-01-01"); // 2022
        postBook("A2", "Alice", "2567-01-01"); // 2024
        postBook("B1", "Bob",   "2566-01-01"); // 2023

        // page=0, size=1, sort=publishedDate,desc -> should return Alice's latest book (A2)
        mockMvc.perform(get("/books")
                        .param("author", "Alice")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "publishedDate,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].author", is("Alice")))
                .andExpect(jsonPath("$.content[0].title", is("A2")))
                .andExpect(jsonPath("$.totalElements", is(2)));

        // page=1, size=1 -> should return Alice's older book (A1)
        mockMvc.perform(get("/books")
                        .param("author", "Alice")
                        .param("page", "1")
                        .param("size", "1")
                        .param("sort", "publishedDate,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("A1")));
    }

    /**
     * Test Case 6:
     * Ensure there is an index on the 'author' column in the books table.
     * This helps optimize queries and avoid full table scans.
     */
    @Test
    void schema_hasIndexOnAuthor() {
        Integer cnt = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'books'
              AND index_name = 'idx_books_author'
        """, Integer.class);

        assertTrue(cnt != null && cnt > 0,
                "Expected index idx_books_author on books(author)");
    }

    // ---------- Helper method ----------
    private long postBook(String title, String author, String beDate) throws Exception {
        String body = """
            { "title": "%s", "author": "%s", "publishedDate": "%s" }
        """.formatted(title, author, beDate);

        String resp = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        JsonNode n = objectMapper.readTree(resp);
        return n.get("id").asLong();
    }
}