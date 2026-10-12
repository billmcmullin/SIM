package com.sim.chatserver.web.dashboard.widgets;

public final class SearchTerms {

    static final SearchTerms EMPTY = new SearchTerms(null, null, null);

    public final String global;
    public final String prompt;
    public final String response;

    SearchTerms(String global, String prompt, String response) {
        this.global = WidgetReviewStartServlet.safe(global);
        this.prompt = WidgetReviewStartServlet.safe(prompt);
        this.response = WidgetReviewStartServlet.safe(response);
    }
}
