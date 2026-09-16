package com.example.personalblog.article;

public class InvalidArticleException extends RuntimeException {

    public InvalidArticleException(String message) {
        super(message);
    }
}
