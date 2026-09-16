package com.example.personalblog.web;

import com.example.personalblog.article.Article;
import com.example.personalblog.article.ArticleService;
import com.example.personalblog.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
class HomeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ArticleService articleService;

    @Test
    void home_showsArticleList_whenArticlesExist() throws Exception {
        Article article = new Article("id1", "Hello World", "content", LocalDate.of(2024, 1, 1), "Admin");
        when(articleService.listPublished()).thenReturn(List.of(article));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Hello World")));
    }

    @Test
    void home_showsEmptyMessage_whenNoArticles() throws Exception {
        when(articleService.listPublished()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Chưa có bài viết nào được xuất bản.")));
    }

    @Test
    void home_showsPostButton_linkingToArticleCreationPage() throws Exception {
        when(articleService.listPublished()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Post")))
                .andExpect(content().string(containsString("/admin/articles/new")));
    }
}
