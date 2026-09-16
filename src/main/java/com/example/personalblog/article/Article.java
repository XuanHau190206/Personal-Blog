package com.example.personalblog.article;

import java.time.LocalDate;

public record Article(String id, String title, String content, LocalDate publishedDate) {
}
