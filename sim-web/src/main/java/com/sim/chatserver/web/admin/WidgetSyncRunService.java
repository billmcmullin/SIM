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
        Objects.requireNonNull(serverConfigLoader, "serverConfigLoader");
        Objects.requireNonNull(widgetLoader, "widgetLoader");
        Objects.requireNonNull(startProgress, "startProgress");
        Objects.requireNonNull(currentWidgetProgressUpdater, "currentWidgetProgressUpdater");
        Objects.requireNonNull(clearCurrentWidgetProgress, "clearCurrentWidgetProgress");
        Objects.requireNonNull(tableNameResolver, "tableNameResolver");
        Objects.requireNonNull(widgetSyncFunction, "widgetSyncFunction");

        ServerConfig config = serverConfigLoader.get();
        if (config == null) {
            throw new IllegalStateException("Server configuration is missing.");
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
                throw new IllegalStateException("Widget sync interrupted before processing next widget.");
            }

            WidgetEntry widget = validWidgets.get(i);
            String widgetId = widget == null ? "" : defaultString(widget.getWidgetId());
            String tableName = defaultString(tableNameResolver.apply(widgetId));
            currentWidgetProgressUpdater.update(widgetId, tableName, i + 1, totalWidgets);

            T status = widgetSyncFunction.apply(config, widgetId);
            statuses.add(status);
        }

        clearCurrentWidgetProgress.run();
        return statuses;
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }
}
