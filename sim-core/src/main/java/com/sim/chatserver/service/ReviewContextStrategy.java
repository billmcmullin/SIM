package com.sim.chatserver.service;

final class ReviewContextStrategy {

    final int topRelevantCount;
    final int newestCount;
    final int oldestCount;
    final int randomCount;
    final int maxHashLines;

    ReviewContextStrategy(int topRelevantCount, int newestCount, int oldestCount, int randomCount, int maxHashLines) {
        this.topRelevantCount = topRelevantCount;
        this.newestCount = newestCount;
        this.oldestCount = oldestCount;
        this.randomCount = randomCount;
        this.maxHashLines = maxHashLines;
    }
}
