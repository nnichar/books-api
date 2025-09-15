package com.example.books_api.model;

import java.time.LocalDate;

public record BookDetail(Long id, String title, String author, LocalDate publishedDate) {}
