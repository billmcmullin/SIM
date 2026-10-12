package com.sim.chatserver.security.review;

import java.util.List;

public final class ReviewValidationResult {

    private static final String PREFIX = "ReviewValidationResult{";
    private static final String VALID_PREFIX = "valid=";
    private static final String ERRORS_PREFIX = ", errors=";
    private static final String WARNINGS_PREFIX = ", warnings=";
    private static final String LENGTH_PREFIX = ", length=";
    private static final String EXPECTED_PREFIX = ", expectedChatIds=";
    private static final String FOUND_PREFIX = ", foundChatIds=";
    private static final String MISSING_PREFIX = ", missingChatIds=";
    private static final String UNEXPECTED_PREFIX = ", unexpectedChatIds=";

    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    private final int length;

    private final List<String> expectedChatIds;
    private final List<String> foundChatIds;
    private final List<String> missingChatIds;
    private final List<String> unexpectedChatIds;

    ReviewValidationResult(boolean valid, List<String> errors, List<String> warnings, int length) {
        this(valid, errors, warnings, length, List.of(), List.of(), List.of(), List.of());
    }

    ReviewValidationResult(
            boolean valid,
            List<String> errors,
            List<String> warnings,
            int length,
            List<String> expectedChatIds,
            List<String> foundChatIds,
            List<String> missingChatIds
    ) {
        this(valid, errors, warnings, length, expectedChatIds, foundChatIds, missingChatIds, List.of());
    }

    ReviewValidationResult(
            boolean valid,
            List<String> errors,
            List<String> warnings,
            int length,
            List<String> expectedChatIds,
            List<String> foundChatIds,
            List<String> missingChatIds,
            List<String> unexpectedChatIds
    ) {
        this.valid = valid;
        this.errors = errors == null ? List.of() : List.copyOf(errors);
        this.warnings = warnings == null ? List.of() : List.copyOf(warnings);
        this.length = Math.max(0, length);

        this.expectedChatIds = expectedChatIds == null ? List.of() : List.copyOf(expectedChatIds);
        this.foundChatIds = foundChatIds == null ? List.of() : List.copyOf(foundChatIds);
        this.missingChatIds = missingChatIds == null ? List.of() : List.copyOf(missingChatIds);
        this.unexpectedChatIds = unexpectedChatIds == null ? List.of() : List.copyOf(unexpectedChatIds);
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    final int getLength() {
        return length;
    }

    List<String> getExpectedChatIds() {
        return expectedChatIds;
    }

    public List<String> getFoundChatIds() {
        return foundChatIds;
    }

    List<String> getMissingChatIds() {
        return missingChatIds;
    }

    List<String> getUnexpectedChatIds() {
        return unexpectedChatIds;
    }

    @Override
    public String toString() {
        return PREFIX
                + VALID_PREFIX + valid
                + ERRORS_PREFIX + errors
                + WARNINGS_PREFIX + warnings
                + LENGTH_PREFIX + length
                + EXPECTED_PREFIX + expectedChatIds
                + FOUND_PREFIX + foundChatIds
                + MISSING_PREFIX + missingChatIds
                + UNEXPECTED_PREFIX + unexpectedChatIds
                + '}';
    }
}
