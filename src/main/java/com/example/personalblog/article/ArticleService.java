package com.example.personalblog.article;

import com.example.personalblog.article.dto.ArticleFormDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final String authorName;
    private final AtomicReference<List<Article>> listCache = new AtomicReference<>();

    public ArticleService(ArticleRepository articleRepository,
                           @Value("${blog.author.name}") String authorName) {
        this.articleRepository = articleRepository;
        this.authorName = authorName;
    }

    public List<Article> listPublished() {
        List<Article> cached = listCache.get();
        if (cached == null) {
            cached = articleRepository.findAll();
            listCache.set(cached);
        }
        return cached;
    }

    public Article getById(String id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }

    public Article create(ArticleFormDto form) {
        validate(form.title(), form.content());
        Article toSave = new Article(null, form.title(), form.content(), LocalDate.now(), authorName);
        Article saved = articleRepository.save(toSave);
        listCache.set(null);
        return saved;
    }

    public Article update(String id, ArticleFormDto form) {
        validate(form.title(), form.content());
        Article existing = getById(id);
        Article toSave = new Article(null, form.title(), form.content(), existing.publishedDate(), existing.author());
        Article updated = articleRepository.update(id, toSave);
        listCache.set(null);
        return updated;
    }

    public void delete(String id) {
        articleRepository.delete(id);
        listCache.set(null);
    }

    private void validate(String title, String content) {
        if (title == null || title.isBlank()) {
            throw new InvalidArticleException("Title must not be blank");
        }
        if (content == null || content.isBlank()) {
            throw new InvalidArticleException("Content must not be blank");
        }
    }
}
