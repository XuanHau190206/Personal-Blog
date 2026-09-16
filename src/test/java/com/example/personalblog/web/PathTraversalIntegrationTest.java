package com.example.personalblog.web;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PathTraversalIntegrationTest {

    @TempDir
    static Path storageDir;

    @DynamicPropertySource
    static void overrideStoragePath(DynamicPropertyRegistry registry) {
        registry.add("blog.storage.path", () -> storageDir.toString());
    }

    @Autowired
    MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {"..\\secret", "..\\..\\secret", "not-a-real-id"})
    void articleDetail_rejectsPathTraversalId_endToEnd(String maliciousId) throws Exception {
        // Encoded backslashes are rejected with 400 by Spring Security's StrictHttpFirewall
        // before ever reaching the controller; a plain unknown id reaches
        // FileArticleRepository and comes back 404 via ArticleNotFoundException.
        // Either way the request must be rejected and never leak file content.
        mockMvc.perform(get("/article/" + maliciousId))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret"))));
    }
}
