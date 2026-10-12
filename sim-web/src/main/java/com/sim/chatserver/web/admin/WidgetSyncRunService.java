package com.sim.chatserver.web.admin;

import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.widget.WidgetEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

final class WidgetSyncRunService {

    private static final String EMPTY = "";
    private static final String MSG_SERVER_CONFIG_LOADER_REQUIRED = "serverConfigLoader";
    private static final String MSG_WIDGET_LOADER_REQUIRED = "widgetLoader";
    private static final String MSG_START_PROGRESS_REQUIRED = "startProgress";
    private static final String MSG_CURRENT_WIDGET_PROGRESS_UPDATER_REQUIRED = "currentWidgetProgressUpdater";
    private static final String MSG_CLEAR_CURRENT_WIDGET_PROGRESS_REQUIRED = "clearCurrentWidgetProgress";
    private static final String MSG_TABLE_NAME_RESOLVER_REQUIRED = "tableNameResolver";
    private static final String MSG_WIDGET_SYNC_FUNCTION_REQUIRED = "widgetSyncFunction";
    private static final String MSG_SERVER_CONFIG_MISSING = "Server configuration is missing.";
    private static final String MSG_WIDGET_SYNC_INTERRUPTED = "Widget sync interrupted before processing next widget.";

    @FunctionalInterface
    interface CurrentWidgetProgressUpdater {
        void update(String widgetId, String tableName, int widgetIndex, int totalWidgets);
    }

    private WidgetSyncRunService() {
    }

    static <T> List<T> run(
            String requestedWidgetId,
            Supplier<ServerConfig> serverConfigLoader,
            Supplier<List<WidgetEntry>> widgetLoader,
            IntConsumer startProgress,
            CurrentWidgetProgressUpdater currentWidgetProgressUpdater,
            Runnable clearCurrentWidgetProgress,
            Function<String, String> tableNameResolver,
            BiFunction<ServerConfig, String, T> widgetSyncFunction
    ) {
        serverConfigLoader = Objects.requireNonNull(serverConfigLoader, MSG_SERVER_CONFIG_LOADER_REQUIRED);
        widgetLoader = Objects.requireNonNull(widgetLoader, MSG_WIDGET_LOADER_REQUIRED);
        startProgress = Objects.requireNonNull(startProgress, MSG_START_PROGRESS_REQUIRED);
        currentWidgetProgressUpdater = Objects.requireNonNull(currentWidgetProgressUpdater, MSG_CURRENT_WIDGET_PROGRESS_UPDATER_REQUIRED);
        clearCurrentWidgetProgress = Objects.requireNonNull(clearCurrentWidgetProgress, MSG_CLEAR_CURRENT_WIDGET_PROGRESS_REQUIRED);
        tableNameResolver = Objects.requireNonNull(tableNameResolver, MSG_TABLE_NAME_RESOLVER_REQUIRED);
        widgetSyncFunction = Objects.requireNonNull(widgetSyncFunction, MSG_WIDGET_SYNC_FUNCTION_REQUIRED);

        ServerConfig config = serverConfigLoader.get();
        if (config == null) {
            throw new IllegalStateException(MSG_SERVER_CONFIG_MISSING);
        }

        List<WidgetEntry> widgets = widgetLoader.get();
        if (widgets == null || widgets.isEmpty()) {
            startProgress.accept(0);
            clearCurrentWidgetProgress.run();
            return List.of();
        }

        if (requestedWidgetId != null && !requestedWidgetId.isBlank()) {
            widgets.removeIf(widget -> widget == null || !requestedWidgetId.equals(widget.getWidgetId()));
        }

        List<WidgetEntry> validWidgets = widgets.stream()
                .filter(widget -> widget != null && widget.getWidgetId() != null && !widget.getWidgetId().isBlank())
                .sorted((left, right) -> left.getWidgetId().compareToIgnoreCase(right.getWidgetId()))
                .toList();

        startProgress.accept(validWidgets.size());

        List<T> statuses = new ArrayList<>(validWidgets.size());
        int totalWidgets = validWidgets.size();
        for (int i = 0; i < totalWidgets; i++) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(MSG_WIDGET_SYNC_INTERRUPTED);
            }

            WidgetEntry widget = validWidgets.get(i);
            String widgetId = widget == null ? EMPTY : defaultString(widget.getWidgetId());
            String tableName = defaultString(tableNameResolver.apply(widgetId));
            currentWidgetProgressUpdater.update(widgetId, tableName, i + 1, totalWidgets);

            T status = widgetSyncFunction.apply(config, widgetId);
            statuses.add(status);
        }

        clearCurrentWidgetProgress.run();
        return statuses;
    }

    private static String defaultString(String value) {
        return value == null ? EMPTY : value;
    }
}