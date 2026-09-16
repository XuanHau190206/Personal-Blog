package com.example.personalblog.article;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {

    List<Article> findAll();

    Optional<Article> findById(String id);

    Article save(Article article);

    Article update(String id, Article article);
}
