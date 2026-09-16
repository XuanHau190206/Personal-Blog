package com.example.personalblog.article;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    ArticleRepository articleRepository;

    @Test
    void listPublished_delegatesToRepository() {
        Article article = new Article("id1", "Title", "Content", LocalDate.now());
        when(articleRepository.findAll()).thenReturn(List.of(article));
        ArticleService service = new ArticleService(articleRepository);

        assertThat(service.listPublished()).containsExactly(article);
    }

    @Test
    void getById_returnsArticle_whenFound() {
        Article article = new Article("id1", "Title", "Content", LocalDate.now());
        when(articleRepository.findById("id1")).thenReturn(Optional.of(article));
        ArticleService service = new ArticleService(articleRepository);

        assertThat(service.getById("id1")).isEqualTo(article);
    }

    @Test
    void getById_throwsArticleNotFoundException_whenMissing() {
        when(articleRepository.findById("missing")).thenReturn(Optional.empty());
        ArticleService service = new ArticleService(articleRepository);

        assertThatThrownBy(() -> service.getById("missing"))
                .isInstanceOf(ArticleNotFoundException.class);
    }
}
