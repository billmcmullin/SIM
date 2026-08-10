package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MarkdownRendererTest {

    private final MarkdownRenderer renderer = new MarkdownRenderer();

    @Test
    void toHtml_returnsNull_forNullOrBlankInput() {
        assertNull(renderer.toHtml(null));
        assertNull(renderer.toHtml(""));
        assertNull(renderer.toHtml("   \t\n"));
    }

    @Test
    void toHtml_rendersMarkdownToHtml() {
        String html = renderer.toHtml("# Title\n\n**bold** text");

        assertTrue(html.contains("<h1>Title</h1>"), html);
        assertTrue(html.contains("<strong>bold</strong>"), html);
    }
}
