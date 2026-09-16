package com.example.personalblog.web;

import com.example.personalblog.article.Article;
import com.example.personalblog.article.ArticleNotFoundException;
import com.example.personalblog.article.ArticleService;
import com.example.personalblog.config.SecurityConfig;
import com.example.personalblog.markdown.MarkdownRenderer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
@Import({SecurityConfig.class, MarkdownRenderer.class, GlobalExceptionHandler.class})
class ArticleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ArticleService articleService;

    @Test
    void detail_showsArticle_whenFound() throws Exception {
        Article article = new Article("id1", "Hello World", "**bold** content", LocalDate.of(2024, 1, 1));
        when(articleService.getById("id1")).thenReturn(article);

        mockMvc.perform(get("/article/id1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello World")))
                .andExpect(content().string(containsString("<strong>bold</strong>")));
    }

    @Test
    void detail_returns404_whenArticleNotFound() throws Exception {
        when(articleService.getById("missing")).thenThrow(new ArticleNotFoundException("missing"));

        mockMvc.perform(get("/article/missing"))
                .andExpect(status().isNotFound());
    }
}
