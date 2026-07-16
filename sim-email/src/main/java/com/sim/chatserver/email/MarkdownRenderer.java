package com.sim.chatserver.email;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;

public class MarkdownRenderer {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();

    String toHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return null;
        }
        return renderer.render(parser.parse(markdown));
    }
}
