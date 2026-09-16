package com.example.personalblog.article;

import com.example.personalblog.article.dto.ArticleFormDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public List<Article> listPublished() {
        return articleRepository.findAll();
    }

    public Article getById(String id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }

    public Article create(ArticleFormDto form) {
        validate(form.title(), form.content());
        LocalDate publishedDate = form.publishedDate() != null ? form.publishedDate() : LocalDate.now();
        Article toSave = new Article(null, form.title(), form.content(), publishedDate);
        return articleRepository.save(toSave);
    }

    public Article update(String id, ArticleFormDto form) {
        validate(form.title(), form.content());
        LocalDate publishedDate = form.publishedDate() != null ? form.publishedDate() : LocalDate.now();
        Article toSave = new Article(null, form.title(), form.content(), publishedDate);
        return articleRepository.update(id, toSave);
    }

    public void delete(String id) {
        articleRepository.delete(id);
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
