package com.example.personalblog.article;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @ParameterizedTest
    @ValueSource(strings = {
            "../secret", "../../secret", "..\\secret", "..\\..\\secret",
            "../../../etc/passwd", "/etc/passwd", "C:/Windows/win.ini"
    })
    void findById_returnsEmpty_forPathTraversalPayloads(String maliciousId) throws IOException {
        Path outsideFile = tempDir.resolveSibling("secret.json");
        Files.writeString(outsideFile, "{}");
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);

        try {
            assertThat(repository.findById(maliciousId)).isEmpty();
        } finally {
            Files.deleteIfExists(outsideFile);
        }
    }

    @Test
    void save_writesArticleToNewJsonFile_withGeneratedId() throws Exception {
        Article toSave = new Article(null, "New Title", "New content", LocalDate.of(2024, 5, 1));
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);

        Article saved = repository.save(toSave);

        assertThat(saved.id()).isNotBlank();
        Path expectedFile = tempDir.resolve(saved.id() + ".json");
        assertThat(Files.exists(expectedFile)).isTrue();

        Article reloaded = objectMapper.readValue(expectedFile.toFile(), Article.class);
        assertThat(reloaded.title()).isEqualTo("New Title");
        assertThat(reloaded.content()).isEqualTo("New content");
    }

    @Test
    void save_createsStorageDirectory_whenMissing() {
        Path missingDir = tempDir.resolve("does-not-exist-yet");
        FileArticleRepository repository = new FileArticleRepository(missingDir.toString(), objectMapper);

        Article saved = repository.save(new Article(null, "Title", "Content", LocalDate.now()));

        assertThat(Files.exists(missingDir.resolve(saved.id() + ".json"))).isTrue();
    }

    @Test
    void save_allowsConcurrentWrites_withoutCorruption() throws Exception {
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);
        int count = 20;
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Future<Article>> futures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = i;
            futures.add(executor.submit(() ->
                    repository.save(new Article(null, "Title " + index, "Content " + index, LocalDate.now()))));
        }
        for (Future<Article> future : futures) {
            future.get();
        }
        executor.shutdown();

        assertThat(repository.findAll()).hasSize(count);
    }

    @Test
    void update_overwritesExistingFile() throws Exception {
        writeArticleFile("abc-123", "Old Title", "Old content", LocalDate.of(2024, 1, 1));
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);

        Article updated = repository.update("abc-123",
                new Article(null, "New Title", "New content", LocalDate.of(2024, 2, 2)));

        assertThat(updated.id()).isEqualTo("abc-123");
        Article reloaded = objectMapper.readValue(tempDir.resolve("abc-123.json").toFile(), Article.class);
        assertThat(reloaded.title()).isEqualTo("New Title");
        assertThat(reloaded.content()).isEqualTo("New content");
        assertThat(reloaded.publishedDate()).isEqualTo(LocalDate.of(2024, 2, 2));
    }

    @Test
    void update_throwsArticleNotFoundException_whenIdDoesNotExist() {
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);

        assertThatThrownBy(() ->
                repository.update("missing", new Article(null, "T", "C", LocalDate.now())))
                .isInstanceOf(ArticleNotFoundException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../secret2", "../../secret2", "..\\secret2", "..\\..\\secret2",
            "../../../etc/passwd", "/etc/passwd", "C:/Windows/win.ini"
    })
    void update_throwsArticleNotFoundException_forPathTraversalPayloads(String maliciousId) throws Exception {
        Path outsideFile = tempDir.resolveSibling("secret2.json");
        Files.writeString(outsideFile, "{}");
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);

        try {
            assertThatThrownBy(() ->
                    repository.update(maliciousId, new Article(null, "T", "C", LocalDate.now())))
                    .isInstanceOf(ArticleNotFoundException.class);
        } finally {
            Files.deleteIfExists(outsideFile);
        }
    }

    @Test
    void delete_removesExistingFile() throws Exception {
        writeArticleFile("abc-123", "Title", "Content", LocalDate.now());
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);

        repository.delete("abc-123");

        assertThat(Files.exists(tempDir.resolve("abc-123.json"))).isFalse();
    }

    @Test
    void delete_throwsArticleNotFoundException_whenIdDoesNotExist() {
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);

        assertThatThrownBy(() -> repository.delete("missing"))
                .isInstanceOf(ArticleNotFoundException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../secret3", "../../secret3", "..\\secret3", "..\\..\\secret3",
            "../../../etc/passwd", "/etc/passwd", "C:/Windows/win.ini"
    })
    void delete_throwsArticleNotFoundException_forPathTraversalPayloads(String maliciousId) throws Exception {
        Path outsideFile = tempDir.resolveSibling("secret3.json");
        Files.writeString(outsideFile, "{}");
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);

        try {
            assertThatThrownBy(() -> repository.delete(maliciousId))
                    .isInstanceOf(ArticleNotFoundException.class);
            assertThat(Files.exists(outsideFile)).isTrue();
        } finally {
            Files.deleteIfExists(outsideFile);
        }
    }

    @Test
    void concurrentReadsAndWrites_neverThrowOrCorruptData() throws Exception {
        FileArticleRepository repository = new FileArticleRepository(tempDir.toString(), objectMapper);
        Article seed = repository.save(new Article(null, "Seed", "Seed content", LocalDate.now()));

        int readerCount = 4;
        int writerCount = 4;
        int iterationsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(readerCount + writerCount);
        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger errors = new AtomicInteger(0);

        for (int i = 0; i < readerCount; i++) {
            futures.add(executor.submit(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    try {
                        repository.findAll();
                        repository.findById(seed.id());
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                }
            }));
        }
        for (int i = 0; i < writerCount; i++) {
            int writerIndex = i;
            futures.add(executor.submit(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    try {
                        repository.update(seed.id(),
                                new Article(null, "Title " + writerIndex + "-" + j, "Content " + j, LocalDate.now()));
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                }
            }));
        }
        for (Future<?> future : futures) {
            future.get();
        }
        executor.shutdown();

        assertThat(errors.get()).isZero();
    }

    private void writeArticleFile(String id, String title, String content, LocalDate date) throws IOException {
        String json = """
                {"id":"%s","title":"%s","content":"%s","publishedDate":"%s"}
                """.formatted(id, title, content, date);
        Files.writeString(tempDir.resolve(id + ".json"), json);
    }
}
