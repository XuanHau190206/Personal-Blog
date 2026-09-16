package com.example.personalblog.markdown;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownRendererTest {

    private final MarkdownRenderer renderer = new MarkdownRenderer();

    @Test
    void renders_heading() {
        assertThat(renderer.toHtml("# Hello")).contains("<h1>Hello</h1>");
    }

    @Test
    void renders_bold() {
        assertThat(renderer.toHtml("**bold**")).contains("<strong>bold</strong>");
    }

    @Test
    void renders_unorderedList() {
        String html = renderer.toHtml("- one\n- two");
        assertThat(html).contains("<ul>").contains("<li>one</li>").contains("<li>two</li>");
    }

    @Test
    void renders_codeBlock() {
        String html = renderer.toHtml("```\ncode here\n```");
        assertThat(html).contains("<pre><code>").contains("code here");
    }

    @Test
    void returnsEmptyString_forNullInput() {
        assertThat(renderer.toHtml(null)).isEmpty();
    }
}
