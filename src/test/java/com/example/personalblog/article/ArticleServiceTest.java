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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    ArticleRepository articleRepository;

    private ArticleService newService() {
        return new ArticleService(articleRepository, "Admin");
    }

    @Test
    void listPublished_delegatesToRepository() {
        Article article = new Article("id1", "Title", "Content", LocalDate.now(), "Admin");
        when(articleRepository.findAll()).thenReturn(List.of(article));
        ArticleService service = newService();

        assertThat(service.listPublished()).containsExactly(article);
    }

    @Test
    void getById_returnsArticle_whenFound() {
        Article article = new Article("id1", "Title", "Content", LocalDate.now(), "Admin");
        when(articleRepository.findById("id1")).thenReturn(Optional.of(article));
        ArticleService service = newService();

        assertThat(service.getById("id1")).isEqualTo(article);
    }

    @Test
    void getById_throwsArticleNotFoundException_whenMissing() {
        when(articleRepository.findById("missing")).thenReturn(Optional.empty());
        ArticleService service = newService();

        assertThatThrownBy(() -> service.getById("missing"))
                .isInstanceOf(ArticleNotFoundException.class);
    }

    @Test
    void create_savesArticle_whenValid() {
        ArticleFormDto form = new ArticleFormDto("Title", "Content");
        Article saved = new Article("id1", "Title", "Content", LocalDate.now(), "Admin");
        when(articleRepository.save(any())).thenReturn(saved);
        ArticleService service = newService();

        Article result = service.create(form);

        assertThat(result).isEqualTo(saved);
        verify(articleRepository).save(argThat(a ->
                a.title().equals("Title") && a.content().equals("Content")));
    }

    @Test
    void create_usesCurrentDateAsPublishedDate() {
        ArticleFormDto form = new ArticleFormDto("Title", "Content");
        when(articleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArticleService service = newService();

        Article result = service.create(form);

        assertThat(result.publishedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void create_usesConfiguredAuthorName() {
        ArticleFormDto form = new ArticleFormDto("Title", "Content");
        when(articleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArticleService service = new ArticleService(articleRepository, "Nguyen Van A");

        Article result = service.create(form);

        assertThat(result.author()).isEqualTo("Nguyen Van A");
    }

    @Test
    void create_throwsInvalidArticleException_whenTitleBlank() {
        ArticleFormDto form = new ArticleFormDto("   ", "Content");
        ArticleService service = newService();

        assertThatThrownBy(() -> service.create(form)).isInstanceOf(InvalidArticleException.class);
        verifyNoInteractions(articleRepository);
    }

    @Test
    void create_throwsInvalidArticleException_whenContentBlank() {
        ArticleFormDto form = new ArticleFormDto("Title", "   ");
        ArticleService service = newService();

        assertThatThrownBy(() -> service.create(form)).isInstanceOf(InvalidArticleException.class);
        verifyNoInteractions(articleRepository);
    }

    @Test
    void update_savesArticle_whenValid() {
        Article existing = new Article("id1", "Old Title", "Old content", LocalDate.of(2024, 1, 1), "Admin");
        when(articleRepository.findById("id1")).thenReturn(Optional.of(existing));
        ArticleFormDto form = new ArticleFormDto("Updated", "Updated content");
        Article updated = new Article("id1", "Updated", "Updated content", LocalDate.of(2024, 1, 1), "Admin");
        when(articleRepository.update(eq("id1"), any())).thenReturn(updated);
        ArticleService service = newService();

        Article result = service.update("id1", form);

        assertThat(result).isEqualTo(updated);
        verify(articleRepository).update(eq("id1"), argThat(a ->
                a.title().equals("Updated") && a.content().equals("Updated content")));
    }

    @Test
    void update_preservesOriginalPublishedDate() {
        Article existing = new Article("id1", "Old Title", "Old content", LocalDate.of(2024, 1, 1), "Admin");
        when(articleRepository.findById("id1")).thenReturn(Optional.of(existing));
        when(articleRepository.update(eq("id1"), any())).thenAnswer(invocation -> invocation.getArgument(1));
        ArticleFormDto form = new ArticleFormDto("Title", "Content");
        ArticleService service = newService();

        Article result = service.update("id1", form);

        assertThat(result.publishedDate()).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    void update_preservesOriginalAuthor_evenIfConfiguredNameChanged() {
        Article existing = new Article("id1", "Old Title", "Old content", LocalDate.of(2024, 1, 1), "Old Author");
        when(articleRepository.findById("id1")).thenReturn(Optional.of(existing));
        when(articleRepository.update(eq("id1"), any())).thenAnswer(invocation -> invocation.getArgument(1));
        ArticleFormDto form = new ArticleFormDto("Title", "Content");
        ArticleService service = new ArticleService(articleRepository, "New Configured Author");

        Article result = service.update("id1", form);

        assertThat(result.author()).isEqualTo("Old Author");
    }

    @Test
    void update_throwsArticleNotFoundException_whenIdDoesNotExist() {
        when(articleRepository.findById("missing")).thenReturn(Optional.empty());
        ArticleFormDto form = new ArticleFormDto("Title", "Content");
        ArticleService service = newService();

        assertThatThrownBy(() -> service.update("missing", form)).isInstanceOf(ArticleNotFoundException.class);
    }

    @Test
    void update_throwsInvalidArticleException_whenTitleBlank() {
        ArticleFormDto form = new ArticleFormDto("   ", "Content");
        ArticleService service = newService();

        assertThatThrownBy(() -> service.update("id1", form)).isInstanceOf(InvalidArticleException.class);
        verifyNoInteractions(articleRepository);
    }

    @Test
    void update_throwsInvalidArticleException_whenContentBlank() {
        ArticleFormDto form = new ArticleFormDto("Title", "   ");
        ArticleService service = newService();

        assertThatThrownBy(() -> service.update("id1", form)).isInstanceOf(InvalidArticleException.class);
        verifyNoInteractions(articleRepository);
    }

    @Test
    void delete_delegatesToRepository() {
        ArticleService service = newService();

        service.delete("id1");

        verify(articleRepository).delete("id1");
    }

    @Test
    void delete_propagatesArticleNotFoundException() {
        doThrow(new ArticleNotFoundException("missing")).when(articleRepository).delete("missing");
        ArticleService service = newService();

        assertThatThrownBy(() -> service.delete("missing")).isInstanceOf(ArticleNotFoundException.class);
    }

    @Test
    void listPublished_cachesResult_untilInvalidated() {
        when(articleRepository.findAll()).thenReturn(List.of());
        ArticleService service = newService();

        service.listPublished();
        service.listPublished();
        service.listPublished();

        verify(articleRepository, times(1)).findAll();
    }

    @Test
    void create_invalidatesCache() {
        when(articleRepository.findAll()).thenReturn(List.of());
        when(articleRepository.save(any()))
                .thenReturn(new Article("id1", "Title", "Content", LocalDate.now(), "Admin"));
        ArticleFormDto form = new ArticleFormDto("Title", "Content");
        ArticleService service = newService();

        service.listPublished();
        service.create(form);
        service.listPublished();

        verify(articleRepository, times(2)).findAll();
    }

    @Test
    void update_invalidatesCache() {
        when(articleRepository.findAll()).thenReturn(List.of());
        when(articleRepository.findById("id1"))
                .thenReturn(Optional.of(new Article("id1", "Old", "Old content", LocalDate.now(), "Admin")));
        when(articleRepository.update(eq("id1"), any()))
                .thenReturn(new Article("id1", "Title", "Content", LocalDate.now(), "Admin"));
        ArticleFormDto form = new ArticleFormDto("Title", "Content");
        ArticleService service = newService();

        service.listPublished();
        service.update("id1", form);
        service.listPublished();

        verify(articleRepository, times(2)).findAll();
    }

    @Test
    void delete_invalidatesCache() {
        when(articleRepository.findAll()).thenReturn(List.of());
        ArticleService service = newService();

        service.listPublished();
        service.delete("id1");
        service.listPublished();

        verify(articleRepository, times(2)).findAll();
    }
}
