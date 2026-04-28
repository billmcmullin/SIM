// widget_review_util/logger.js

const PREFIX = "[widget_review]";
const isDebugEnabled = (() => {
    try {
        if (window?.widgetReviewConfig?.debug === true) return true;
        if (localStorage.getItem("widgetReviewDebug") === "1") return true;
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
    if (hasMeta(meta)) console.info(fmt(message), meta);
    else console.info(fmt(message));
}

export function warn(message, meta) {
    if (hasMeta(meta)) console.warn(fmt(message), meta);
    else console.warn(fmt(message));
}

export function error(message, meta) {
    if (hasMeta(meta)) console.error(fmt(message), meta);
    else console.error(fmt(message));
}

/**
 * Debug logger (only logs when debug is enabled).
 */
export function debug(message, meta) {
    if (!isDebugEnabled) return;
    if (hasMeta(meta)) console.debug(fmt(message), meta);
    else console.debug(fmt(message));
}

/**
 * Timed helper with optional fail() support.
 */
export function timed(label) {
    const start = performance.now();

    return {
        end(meta) {
            const ms = Math.round((performance.now() - start) * 100) / 100;
            if (hasMeta(meta)) console.info(fmt(`${label} completed in ${ms}ms`), meta);
            else console.info(fmt(`${label} completed in ${ms}ms`));
        },
        fail(meta) {
            const ms = Math.round((performance.now() - start) * 100) / 100;
            if (hasMeta(meta)) console.error(fmt(`${label} failed in ${ms}ms`), meta);
            else console.error(fmt(`${label} failed in ${ms}ms`));
        }
    };
}
