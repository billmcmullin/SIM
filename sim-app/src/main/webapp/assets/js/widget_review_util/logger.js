// widget_review_util/logger.js

const PREFIX = "[widget_review]";
const isDebugEnabled = (() => {
    try {
        const cfg = (typeof globalThis !== "undefined" && globalThis.widgetReviewConfig)
            ? globalThis.widgetReviewConfig
            : null;
        if (cfg && cfg.debug === true) {
            return true;
        }
        const storage = (typeof globalThis !== "undefined" && globalThis.localStorage)
            ? globalThis.localStorage
            : null;
        if (storage && storage.getItem("widgetReviewDebug") === "1") {
            return true;
        }
    } catch {
        // ignore storage/window access errors
    }
    return false;
})();

function ts() {
    return new Date().toISOString();
}

function fmt(message) {
    return `${PREFIX} ${ts()} ${message}`;
}

function hasMeta(meta) {
    return meta !== undefined && meta !== null && meta !== "";
}

export function info(message, meta) {
    if (hasMeta(meta)) {
        console.info(fmt(message), meta);
    } else {
        console.info(fmt(message));
    }
}

export function warn(message, meta) {
    if (hasMeta(meta)) {
        console.warn(fmt(message), meta);
    } else {
        console.warn(fmt(message));
    }
}

export function error(message, meta) {
    if (hasMeta(meta)) {
        console.error(fmt(message), meta);
    } else {
        console.error(fmt(message));
    }
}

/**
 * Debug logger (only logs when debug is enabled).
 */
export function debug(message, meta) {
    if (!isDebugEnabled) {
        return;
    }
    if (hasMeta(meta)) {
        console.debug(fmt(message), meta);
    } else {
        console.debug(fmt(message));
    }
}

/**
 * Timed helper with optional fail() support.
 */
export function timed(label) {
    const perf = (typeof globalThis !== "undefined" && globalThis.performance)
        ? globalThis.performance
        : null;
    const now = perf && typeof perf.now === "function" ? () => perf.now() : () => Date.now();
    const start = now();

    return {
        end(meta) {
            const ms = Math.round((now() - start) * 100) / 100;
            if (hasMeta(meta)) {
                console.info(fmt(`${label} completed in ${ms}ms`), meta);
            } else {
                console.info(fmt(`${label} completed in ${ms}ms`));
            }
        },
        fail(meta) {
            const ms = Math.round((now() - start) * 100) / 100;
            if (hasMeta(meta)) {
                console.error(fmt(`${label} failed in ${ms}ms`), meta);
            } else {
                console.error(fmt(`${label} failed in ${ms}ms`));
            }
        }
    };
}
