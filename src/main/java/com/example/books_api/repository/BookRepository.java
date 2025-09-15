package com.example.books_api.repository;

import com.example.books_api.entity.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<BookEntity, Long> {

    Page<BookEntity> findByAuthorIgnoreCase(String author, Pageable pageable);
}