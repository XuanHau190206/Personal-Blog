package com.example.personalblog.article;

import com.example.personalblog.article.dto.ArticleFormDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @Test
    void create_savesArticle_whenValid() {
        ArticleFormDto form = new ArticleFormDto("Title", LocalDate.of(2024, 1, 1), "Content");
        Article saved = new Article("id1", "Title", "Content", LocalDate.of(2024, 1, 1));
        when(articleRepository.save(any())).thenReturn(saved);
        ArticleService service = new ArticleService(articleRepository);

        Article result = service.create(form);

        assertThat(result).isEqualTo(saved);
        org.mockito.Mockito.verify(articleRepository).save(argThat(a ->
                a.title().equals("Title") && a.content().equals("Content")));
    }

    @Test
    void create_defaultsPublishedDateToToday_whenNull() {
        ArticleFormDto form = new ArticleFormDto("Title", null, "Content");
        when(articleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArticleService service = new ArticleService(articleRepository);

        Article result = service.create(form);

        assertThat(result.publishedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void create_throwsInvalidArticleException_whenTitleBlank() {
        ArticleFormDto form = new ArticleFormDto("   ", LocalDate.now(), "Content");
        ArticleService service = new ArticleService(articleRepository);

        assertThatThrownBy(() -> service.create(form)).isInstanceOf(InvalidArticleException.class);
        verifyNoInteractions(articleRepository);
    }

    @Test
    void create_throwsInvalidArticleException_whenContentBlank() {
        ArticleFormDto form = new ArticleFormDto("Title", LocalDate.now(), "   ");
        ArticleService service = new ArticleService(articleRepository);

        assertThatThrownBy(() -> service.create(form)).isInstanceOf(InvalidArticleException.class);
        verifyNoInteractions(articleRepository);
    }
}
