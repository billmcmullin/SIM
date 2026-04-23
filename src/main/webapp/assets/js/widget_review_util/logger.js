// widget_review_util/logger.js

const PREFIX = "[widget_review]";

export function info(message, meta) {
    console.info(`${PREFIX} ${message}`, meta || "");
}

export function warn(message, meta) {
    console.warn(`${PREFIX} ${message}`, meta || "");
}

export function error(message, meta) {
    console.error(`${PREFIX} ${message}`, meta || "");
}

export function timed(label) {
    const start = performance.now();
    return {
        end(meta) {
            const ms = Math.round((performance.now() - start) * 100) / 100;
            console.info(`${PREFIX} ${label} completed in ${ms}ms`, meta || "");
        }
    };
}
