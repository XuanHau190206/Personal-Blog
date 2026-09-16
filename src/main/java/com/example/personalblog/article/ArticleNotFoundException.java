package com.example.personalblog.article;

public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(String id) {
        super("Article not found: " + id);
    }
}
