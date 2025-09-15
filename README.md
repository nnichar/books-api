# 📚 Books API

A simple RESTful API for managing books using **Spring Boot** and **MySQL**.
Supports saving books with **Buddhist calendar dates** (BE) which are automatically converted to **Gregorian calendar (AD)** when stored.

---

## 🚀 Features

* **Save Books with Buddhist Dates**

    * Accepts `publishedDate` in Buddhist Era (BE), e.g., `2568-09-14`.
    * Automatically converts it to Gregorian (AD), e.g., `2025-09-14`.

* **Get Books by Author**

    * Get books by a specific author.
    * Uses a database index on the `author` field for faster queries.
    * Supports pagination (`page`, `size`) and sorting (`sort`).

* **Validation Rules**

    * `title` must not be empty.
    * `author` must not be empty.
    * `publishedDate` must be a valid date:

        * year > **1000**
        * year ≤ **current year**

* **Database**

    * MySQL table: `books`
    * Index on `author` column to make searches faster.

* **Integration Tests**

    * Automated tests for all endpoints.
    * Run with **Testcontainers** (real MySQL in Docker) + **MockMvc**.

---

## 🗄 Database Setup

```sql
CREATE DATABASE booksdb;
CREATE USER 'books_app'@'%' IDENTIFIED BY 'changeme';
GRANT ALL PRIVILEGES ON booksdb.* TO 'books_app'@'%';
```

Verify connection:

```bash
mysql -u books_app -p -h 127.0.0.1 booksdb
```

---

## ▶️ Running the Application

You can run the Spring Boot server in 2 ways:

**Option 1: Maven (command line)**

```bash
./mvnw spring-boot:run
```

**Option 2: IDE (e.g., IntelliJ / Eclipse / VS Code)**

* Navigate to:
  `books-api/src/main/java/com/example/books_api/BooksApiApplication.java`
* Right-click the class → **Run `BooksApiApplication`**

The application will start on: [http://localhost:8080](http://localhost:8080)

---

## 🧪 Running Tests

⚠️ **Important:** Make sure **Docker Desktop** (or Docker Engine) is running,
because Testcontainers needs to start a real MySQL container for the tests.

### 🐳 How to Check if Docker is Running

Run this command in your terminal:

```bash
docker ps
```

* If Docker is running → you will see a table (even if it’s empty).
* If Docker is **not running** → you’ll get an error like:
  `Cannot connect to the Docker daemon...`

---

You can run integration tests in 2 ways:

**Option 1: Maven (command line)**

```bash
./mvnw test
```

**Option 2: IDE (e.g., IntelliJ / Eclipse / VS Code)**

* Navigate to:
  `books-api/src/test/java/com/example/books_api/BooksApiApplicationTests.java`
* Right-click the class → **Run `BooksApiApplicationTests`**

---

## 📌 API

### 1. Save Book (Required)

**POST** `/books`

Request:

```json
{
  "title": "Spring in Action",
  "author": "Steve",
  "publishedDate": "2568-09-14"
}
```

Response:

```json
{
  "id": 1
}
```

---

### 2. Get Books by Author (Required)

**GET** `/books?author={authorName}`

**Example:** `/books?author=J.K. Rowling`

Response:

```json
{
  "content": [
    {
      "id": 2,
      "title": "Harry Potter and the Prisoner of Azkaban",
      "author": "J.K. Rowling",
      "publishedDate": "1999-07-08"
    },
    {
      "id": 3,
      "title": "Harry Potter and the Chamber of Secrets",
      "author": "J.K. Rowling",
      "publishedDate": "1998-07-02"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "number": 0,
  "size": 20,
  "first": true,
  "last": true,
  "sort": "publishedDate: ASC"
}
```

### Optional with Pagination/Sorting

**GET** `/books?author={authorName}&page={pageNumber}&size={pageSize}&sort={field},{direction}`

- `pageNumber` → which page to retrieve (starts from 0)
- `pageSize` → how many records per page
- `field` → which column to sort by (e.g., `publishedDate`, `id`)
- `direction` → `asc` or `desc`

**Example:**  
`/books?author=J.K. Rowling&page=0&size=2&sort=publishedDate,desc`

Response:

```json
{
  "content": [
    {
      "id": 3,
      "title": "Harry Potter and the Chamber of Secrets",
      "author": "J.K. Rowling",
      "publishedDate": "1998-07-02"
    },
    {
      "id": 2,
      "title": "Harry Potter and the Prisoner of Azkaban",
      "author": "J.K. Rowling",
      "publishedDate": "1999-07-08"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "number": 0,
  "size": 2,
  "first": true,
  "last": true,
  "sort": "publishedDate: DESC"
}
```

---

### 3. Save Multiple Books (Optional)

**POST** `/books/bulk`

Request:

```json
[
  {
    "title": "Book One",
    "author": "Alice",
    "publishedDate": "2565-01-01"
  },
  {
    "title": "Book Two",
    "author": "Bob",
    "publishedDate": "2566-02-02"
  }
]
```

Response:

```json
[
  { "id": 4 },
  { "id": 5 }
]
```

---

### 4. Get All Books (Optional)

**GET** `/books/all`

Response:

```json
{
  "content": [
    {
      "id": 1,
      "title": "Spring in Action",
      "author": "Steve",
      "publishedDate": "2025-09-14"
    },
    {
      "id": 2,
      "title": "Harry Potter and the Prisoner of Azkaban",
      "author": "J.K. Rowling",
      "publishedDate": "1999-07-08"
    },
    {
      "id": 3,
      "title": "Harry Potter and the Chamber of Secrets",
      "author": "J.K. Rowling",
      "publishedDate": "1998-07-02"
    },
    {
      "id": 4,
      "title": "Book One",
      "author": "Alice",
      "publishedDate": "2022-01-01"
    },
    {
      "id": 5,
      "title": "Book Two",
      "author": "Bob",
      "publishedDate": "2023-02-02"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "number": 0,
  "size": 20,
  "first": true,
  "last": true,
  "sort": "id: ASC"
}
```

### Optional with Pagination/Sorting

**GET** `/books/all?page={pageNumber}&size={pageSize}&sort={field},{direction}`

- `pageNumber` → which page to retrieve (starts from 0)
- `pageSize` → how many records per page
- `field` → which column to sort by (e.g., `id`, `publishedDate`)
- `direction` → `asc` or `desc`

**Example:**  
`/books/all?page=0&size=5&sort=id,desc`


Response:

```json
{
  "content": [
    {
      "id": 5,
      "title": "Book Two",
      "author": "Bob",
      "publishedDate": "2023-02-02"
    },
    {
      "id": 4,
      "title": "Book One",
      "author": "Alice",
      "publishedDate": "2022-01-01"
    },
    {
      "id": 3,
      "title": "Harry Potter and the Chamber of Secrets",
      "author": "J.K. Rowling",
      "publishedDate": "1998-07-02"
    },
    {
      "id": 2,
      "title": "Harry Potter and the Prisoner of Azkaban",
      "author": "J.K. Rowling",
      "publishedDate": "1999-07-08"
    },
    {
      "id": 1,
      "title": "Spring in Action",
      "author": "Steve",
      "publishedDate": "2025-09-14"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "number": 0,
  "size": 5,
  "first": true,
  "last": true,
  "sort": "id: DESC"
}
```

---

## ❌ Error Handling

The API provides structured error responses:

### 1. Validation Errors (MethodArgumentNotValidException)
Occurs when @Valid fails (empty fields, invalid date format).


**POST** `/books`

**Request**:

```json
{
  "title": "",
  "author": "",
  "publishedDate": "25-09-14"
}
```

**Response**:
```json
{
  "timestamp": "2025-09-14T16:24:10.352+00:00",
  "status": 400,
  "errors": [
    { "field": "title", "message": "must not be empty" },
    { "field": "author", "message": "must not be empty" },
    { "message": "publishedDate must be a valid BE date in format yyyy-MM-dd" }
  ],
  "path": "/books"
}
```

--- 

### 2. Invalid Date Rule (IllegalArgumentException)
Occurs when dates violate rules (year ≤ 1000 or > current year).


**POST** `/books`

**Request**:

```json
{
  "title": "Future Book",
  "author": "Time",
  "publishedDate": "3100-01-01"
}
```

**Response**:
```json
{
  "timestamp": "2025-09-14T16:30:45.123+00:00",
  "status": 400,
  "errors": [
    { "message": "publishedDate year must be > 1000 and <= current year" }
  ],
  "path": "/books"
}
```

--- 

### 3. Missing Query Parameter (MissingServletRequestParameterException)
Occurs when required query parameters are missing.

**GET** `/books`

**Response**:
```json
{
  "timestamp": "2025-09-14T16:32:10.456+00:00",
  "status": 400,
  "errors": [
    { "message": "author is required" }
  ],
  "path": "/books"
}
```