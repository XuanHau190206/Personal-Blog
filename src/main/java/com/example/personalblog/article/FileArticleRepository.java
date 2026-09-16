package com.example.personalblog.article;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class FileArticleRepository implements ArticleRepository {

    private final Path storageDir;
    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public FileArticleRepository(@Value("${blog.storage.path}") String storagePath, ObjectMapper objectMapper) {
        this.storageDir = Path.of(storagePath).normalize();
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Article> findAll() {
        lock.readLock().lock();
        try {
            if (!Files.isDirectory(storageDir)) {
                return List.of();
            }
            List<Article> articles = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageDir, "*.json")) {
                for (Path file : stream) {
                    articles.add(readArticle(file));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            articles.sort(Comparator.comparing(Article::publishedDate).reversed());
            return articles;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Article> findById(String id) {
        lock.readLock().lock();
        try {
            Path file = resolveSafely(id);
            if (file == null || !Files.isRegularFile(file)) {
                return Optional.empty();
            }
            return Optional.of(readArticle(file));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Article save(Article article) {
        lock.writeLock().lock();
        try {
            createStorageDirIfMissing();
            String id = UUID.randomUUID().toString();
            Article toPersist = new Article(id, article.title(), article.content(), article.publishedDate());
            Path file = storageDir.resolve(id + ".json");
            objectMapper.writeValue(file.toFile(), toPersist);
            return toPersist;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Article update(String id, Article article) {
        lock.writeLock().lock();
        try {
            Path file = resolveSafely(id);
            if (file == null || !Files.isRegularFile(file)) {
                throw new ArticleNotFoundException(id);
            }
            Article toPersist = new Article(id, article.title(), article.content(), article.publishedDate());
            objectMapper.writeValue(file.toFile(), toPersist);
            return toPersist;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void createStorageDirIfMissing() {
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path resolveSafely(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        Path candidate = storageDir.resolve(id + ".json").normalize();
        if (!candidate.startsWith(storageDir)) {
            return null;
        }
        return candidate;
    }

    private Article readArticle(Path file) {
        return objectMapper.readValue(file.toFile(), Article.class);
    }
}
