package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sim.chatserver.service.dashboard.DashboardDrilldownSelectionQueryService;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.term.TextSanitizer;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Convenience endpoint for date drilldowns: -
 * /dashboard/sessions/drilldown/date-review-relative?day=today|yesterday -
 * /dashboard/sessions/drilldown/date-review-relative?date=YYYY-MM-DD Optional
 * term filtering: - ...&term=Your%20Term
 */
@WebServlet(name = "DashboardRelativeDateSelectionServlet", urlPatterns = {"/dashboard/sessions/drilldown/date-review-relative"})
public class DashboardRelativeDateSelectionServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardRelativeDateSelectionServlet.class.getName());
    private static final String OTHER_PARASOFT_LABEL = "Other Parasoft Match";
    private static final String SCOPE_TERM_ENTRIES = "termEntries";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
        private final transient DashboardDrilldownSelectionQueryService queryService =
            new DashboardDrilldownSelectionQueryService(log);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        LocalDate date = resolveDate(req, resp);
        if (date == null) {
            return; // error already sent
        }

        String rawTerm = ServletRequestParamUtil.firstParam(req, "term", 256, true, true);
        String requestedTerm = (rawTerm == null) ? "" : rawTerm.trim();
        String scope = ServletRequestParamUtil.firstParam(req, "scope", 256, true, true);
        boolean termEntriesOnly = SCOPE_TERM_ENTRIES.equalsIgnoreCase(scope == null ? "" : scope.trim());

        List<TermChatSnapshot> snapshots = queryService.collectDateEntries(date);

        if (snapshots.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for the requested day.");
            return;
        }

        if (!requestedTerm.isBlank()) {
            snapshots = filterSnapshotsByTerm(snapshots, requestedTerm);

            if (snapshots.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for the requested day and term.");
                return;
            }
        } else if (termEntriesOnly) {
            snapshots = filterSnapshotsByKnownTerms(snapshots);

            if (snapshots.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No term entry chats found for the requested day.");
                return;
            }
        }

        String selectionLabel = requestedTerm.isBlank()
            ? (termEntriesOnly ? ("Date " + date + " - Term Entries") : ("Date " + date))
            : ("Date " + date + " - " + requestedTerm);

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                selectionLabel,
                snapshots,
                req.getContextPath() + "/dashboard"
        );

        if (selectionId == null || selectionId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create a review selection.");
            return;
        }

        req.setAttribute("selectionId", selectionId);
        req.getRequestDispatcher("/dashboard/widgets/drilldown/review").forward(req, resp);
    
        } catch (Throwable e) {
            java.util.logging.Logger.getLogger("OWASP")
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doGet", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger("OWASP")
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private LocalDate resolveDate(HttpServletRequest req, HttpServletResponse resp) {
        String dateParam = ServletRequestParamUtil.firstParam(req, "date", 256, true, true);
        if (dateParam != null && !dateParam.isBlank()) {
            try {
                return LocalDate.parse(dateParam.trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, "Invalid date parameter for relative date selection");
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid date value. Use YYYY-MM-DD.");
                return null;
            }
        }

        String day = ServletRequestParamUtil.firstParam(req, "day", 256, true, true);
        if (day == null || day.isBlank()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Provide day=today|yesterday or date=YYYY-MM-DD.");
            return null;
        }

        return switch (day.trim().toLowerCase()) {
            case "today" ->
                LocalDate.now(ZoneId.systemDefault());
            case "yesterday" ->
                LocalDate.now(ZoneId.systemDefault()).minusDays(1);
            default -> {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid day value. Use today or yesterday.");
                yield null;
            }
        };
    }

    private List<TermChatSnapshot> filterSnapshotsByTerm(List<TermChatSnapshot> source, String requestedTerm) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        List<TermDefinition> allTerms;
        try {
            allTerms = termsStore().listAll();
        } catch (SQLException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to load term definitions for date+term filtering", e);
            return List.of();
        }

        if (allTerms == null || allTerms.isEmpty()) {
            return List.of();
        }

        List<TermDefinition> activeTerms = new ArrayList<>();
        List<Pattern> compiledPatterns = new ArrayList<>();

        for (TermDefinition term : allTerms) {
            if (term == null || term.isSystemFlag()) {
                continue;
            }
            Pattern compiled = TermMatcher.buildStrictPattern(term);
            if (compiled == null) {
                continue;
            }
            activeTerms.add(term);
            compiledPatterns.add(compiled);
        }

        if (activeTerms.isEmpty()) {
            return List.of();
        }

        List<TermChatSnapshot> out = new ArrayList<>();
        String target = requestedTerm.trim();

        for (TermChatSnapshot snap : source) {
            String prompt = snap == null || snap.getPrompt() == null ? "" : snap.getPrompt();
            String sanitized = TextSanitizer.sanitizeForMatching(prompt);

            TermDefinition bestTerm = null;
            int bestStart = Integer.MAX_VALUE;

            for (int i = 0; i < compiledPatterns.size(); i++) {
                Matcher m = compiledPatterns.get(i).matcher(sanitized);
                if (m.find()) {
                    int start = m.start();
                    if (start < bestStart) {
                        bestStart = start;
                        bestTerm = activeTerms.get(i);
                        if (bestStart == 0) {
                            break;
                        }
                    }
                }
            }

            String classified = (bestTerm != null && bestTerm.getName() != null && !bestTerm.getName().isBlank())
                    ? bestTerm.getName()
                    : OTHER_PARASOFT_LABEL;

            if (classified.equalsIgnoreCase(target)) {
                out.add(snap);
            }
        }

        return out;
    }

    private List<TermChatSnapshot> filterSnapshotsByKnownTerms(List<TermChatSnapshot> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        List<TermDefinition> allTerms;
        try {
            allTerms = termsStore().listAll();
        } catch (SQLException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to load term definitions for term-entry filtering", e);
            return List.of();
        }

        if (allTerms == null || allTerms.isEmpty()) {
            return List.of();
        }

        List<Pattern> compiledPatterns = new ArrayList<>();
        for (TermDefinition term : allTerms) {
            if (term == null || term.isSystemFlag()) {
                continue;
            }

            String name = term.getName();
            if (name == null || name.isBlank() || OTHER_PARASOFT_LABEL.equalsIgnoreCase(name.trim())) {
                continue;
            }

            Pattern compiled = TermMatcher.buildStrictPattern(term);
            if (compiled != null) {
                compiledPatterns.add(compiled);
            }
        }

        if (compiledPatterns.isEmpty()) {
            return List.of();
        }

        List<TermChatSnapshot> out = new ArrayList<>();
        for (TermChatSnapshot snap : source) {
            String prompt = snap == null || snap.getPrompt() == null ? "" : snap.getPrompt();
            String sanitized = TextSanitizer.sanitizeForMatching(prompt);

            boolean matched = false;
            for (Pattern p : compiledPatterns) {
                Matcher m = p.matcher(sanitized);
                if (m.find()) {
                    matched = true;
                    break;
                }
            }

            if (matched) {
                out.add(snap);
            }
        }

        return out;
    }

    protected TermsStore termsStore() {
        return CDI.current().select(TermsStore.class).get();
    }

    private void sendError(HttpServletResponse resp, int status, String message) {
        if (resp == null) {
            return;
        }
        try {
            resp.sendError(status, message == null ? "Request failed." : message);
        } catch (IOException e) {
            log.log(Level.FINE, "Unable to send error response", e);
        }
    }

}
