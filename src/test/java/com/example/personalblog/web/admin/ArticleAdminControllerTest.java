package com.example.personalblog.web.admin;

import com.example.personalblog.article.Article;
import com.example.personalblog.article.ArticleNotFoundException;
import com.example.personalblog.article.ArticleService;
import com.example.personalblog.config.SecurityConfig;
import com.example.personalblog.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ArticleAdminController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ArticleAdminControllerTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @MockitoBean
    ArticleService articleService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser
    void newForm_showsEmptyForm() throws Exception {
        mockMvc.perform(get("/admin/articles/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/article-form"));
    }

    @Test
    @WithMockUser
    void create_withValidData_redirectsToAdmin() throws Exception {
        Article saved = new Article("id1", "Title", "Content", LocalDate.of(2024, 1, 1), "Admin");
        when(articleService.create(any())).thenReturn(saved);

        mockMvc.perform(post("/admin/articles")
                        .with(csrf())
                        .param("title", "Title")
                        .param("content", "Content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(articleService).create(any());
    }

    @Test
    @WithMockUser
    void create_withBlankTitle_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/admin/articles")
                        .with(csrf())
                        .param("title", "")
                        .param("content", "Content"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/article-form"));

        verify(articleService, never()).create(any());
    }

    @Test
    @WithMockUser
    void editForm_prefillsExistingArticle() throws Exception {
        Article article = new Article("id1", "Existing Title", "Existing content", LocalDate.of(2024, 1, 1), "Admin");
        when(articleService.getById("id1")).thenReturn(article);

        mockMvc.perform(get("/admin/articles/id1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/article-form"))
                .andExpect(content().string(containsString("Existing Title")))
                .andExpect(content().string(containsString("Existing content")));
    }

    @Test
    @WithMockUser
    void editForm_returns404_whenArticleNotFound() throws Exception {
        when(articleService.getById("missing")).thenThrow(new ArticleNotFoundException("missing"));

        mockMvc.perform(get("/admin/articles/missing/edit"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void update_withValidData_redirectsToAdmin() throws Exception {
        Article updated = new Article("id1", "New Title", "New content", LocalDate.of(2024, 2, 2), "Admin");
        when(articleService.update(eq("id1"), any())).thenReturn(updated);

        mockMvc.perform(post("/admin/articles/id1")
                        .with(csrf())
                        .param("title", "New Title")
                        .param("content", "New content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(articleService).update(eq("id1"), any());
    }

    @Test
    @WithMockUser
    void update_withBlankContent_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/admin/articles/id1")
                        .with(csrf())
                        .param("title", "Title")
                        .param("content", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/article-form"));

        verify(articleService, never()).update(any(), any());
    }

    @Test
    @WithMockUser
    void delete_removesArticleAndRedirectsToAdmin() throws Exception {
        mockMvc.perform(post("/admin/articles/id1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(articleService).delete("id1");
    }

    @Test
    @WithMockUser
    void delete_returns404_whenArticleNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ArticleNotFoundException("missing"))
                .when(articleService).delete("missing");

        mockMvc.perform(post("/admin/articles/missing/delete").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
