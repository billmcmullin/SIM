package com.sim.chatserver.web.dashboard.topics;

import java.util.regex.Pattern;

final class TopicPattern {

    final String name;
    final Pattern pattern;

    TopicPattern(String name, Pattern pattern) {
        this.name = name;
        this.pattern = pattern;
    }
}
