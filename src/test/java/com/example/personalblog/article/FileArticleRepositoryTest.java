package com.example.personalblog.article;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FileArticleRepositoryTest {

    @TempDir
    Path tempDir;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void findAll_returnsEmptyList_whenDirectoryIsEmpty() {
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void findAll_returnsEmptyList_whenDirectoryDoesNotExist() {
        Path missingDir = tempDir.resolve("does-not-exist");
        FileArticleRepository repository = new FileArticleRepository(missingDir.toString(), objectMapper);

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void findAll_parsesArticlesAndSortsByPublishedDateDescending() throws IOException {
        writeArticleFile("older", "Older Post", "Content A", LocalDate.of(2024, 1, 1));
        writeArticleFile("newer", "Newer Post", "Content B", LocalDate.of(2024, 6, 1));

        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);
        List<Article> articles = repository.findAll();

        assertThat(articles).hasSize(2);
        assertThat(articles.get(0).id()).isEqualTo("newer");
        assertThat(articles.get(1).id()).isEqualTo("older");
    }

    @Test
    void findById_returnsArticle_whenFileExists() throws IOException {
        writeArticleFile("abc-123", "Hello", "World", LocalDate.of(2024, 3, 1));
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);

        Optional<Article> found = repository.findById("abc-123");

        assertThat(found).isPresent();
        assertThat(found.get().title()).isEqualTo("Hello");
        assertThat(found.get().content()).isEqualTo("World");
    }

    @Test
    void findById_returnsEmpty_whenFileDoesNotExist() {
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);

        assertThat(repository.findById("missing")).isEmpty();
    }

    @Test
    void findById_returnsEmpty_forPathTraversalAttempt() throws IOException {
        Path outsideFile = tempDir.resolveSibling("secret.json");
        Files.writeString(outsideFile, "{}");
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);

        try {
            assertThat(repository.findById("../secret")).isEmpty();
        } finally {
            Files.deleteIfExists(outsideFile);
        }
    }

    private void writeArticleFile(String id, String title, String content, LocalDate date) throws IOException {
        String json = """
                {"id":"%s","title":"%s","content":"%s","publishedDate":"%s"}
                """.formatted(id, title, content, date);
        Files.writeString(tempDir.resolve(id + ".json"), json);
    }
}
