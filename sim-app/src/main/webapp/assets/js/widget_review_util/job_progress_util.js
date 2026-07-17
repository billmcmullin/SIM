// widget_review_util/job_progress_util.js

/**
 * Convert backend phase names into user-friendly text.
 */
export function humanizePhase(phase) {
    const p = String(phase || "").toUpperCase();
    switch (p) {
        case "QUEUED": return "Waiting to start";
        case "MAP": return "Reviewing chats";
        case "REDUCE": return "Preparing final report";
        case "COMPLETED": return "Finished";
        case "FAILED": return "Could not complete";
        case "CANCELLED": return "Stopped";
        default: return "In progress";
    }
}

/**
 * Parse synthesis progress hints from backend activity text.
 * Supports formats like:
 *  - "synthesis L2 chunk 3/10"
 *  - "synthesis level 2"
 *  - "final synthesis attempt 2/4"
 */
export function parseSynthesisFromActivity(activity) {
    if (!activity) {
        return null;
    }
    const txt = String(activity).toLowerCase();

    const levelChunk = txt.match(/synthesis\s+l(\d+).*chunk\s+(\d+)\s*\/\s*(\d+)/i);
    if (levelChunk) {
        return {
            level: parseInt(levelChunk[1], 10) || 0,
            chunk: parseInt(levelChunk[2], 10) || 0,
            totalChunks: parseInt(levelChunk[3], 10) || 0
        };
    }

    const levelOnly = txt.match(/synthesis\s+level\s+(\d+)/i);
    if (levelOnly) {
        return { level: parseInt(levelOnly[1], 10) || 0 };
    }

    const finalAttempt = txt.match(/final\s+synthesis\s+attempt\s+(\d+)\s*\/\s*(\d+)/i);
    if (finalAttempt) {
        return {
            finalAttempt: parseInt(finalAttempt[1], 10) || 0,
            finalAttemptTotal: parseInt(finalAttempt[2], 10) || 0
        };
    }

    return null;
}

/**
 * Weighted progress model:
 * - QUEUED: small %
 * - MAP: 5..80 based on batches
 * - REDUCE: 80..99 based on synthesis hints
 * - done: 100
 */
export function deriveWeightedProgress({ phase, done, completedBatches, totalBatches, synth }) {
    if (done) {
        return 100;
    }
    const p = String(phase || "").toUpperCase();

    if (p === "QUEUED") {
        return 3;
    }

    if (p === "MAP") {
        if (totalBatches > 0) {
            const ratio = Math.max(0, Math.min(1, completedBatches / Math.max(1, totalBatches)));
            return 5 + Math.round(ratio * 75);
        }
        return 15;
    }

    if (p === "REDUCE") {
        let reduceRatio = 0.1;

        if (synth?.finalAttempt > 0 && synth?.finalAttemptTotal > 0) {
            reduceRatio = Math.max(reduceRatio, Math.min(1, synth.finalAttempt / synth.finalAttemptTotal));
        } else if (synth?.level > 0 && synth?.totalChunks > 0) {
            const chunkRatio = Math.max(0, Math.min(1, synth.chunk / Math.max(1, synth.totalChunks)));
            const lvlFactor = Math.min(0.95, 0.2 + (synth.level * 0.15));
            reduceRatio = Math.max(reduceRatio, Math.min(1, lvlFactor * 0.7 + chunkRatio * 0.3));
        }

        return 80 + Math.round(reduceRatio * 19);
    }

    return 10;
}

export function deriveSynthesisPercent(synth) {
    if (synth?.finalAttempt > 0 && synth?.finalAttemptTotal > 0) {
        return Math.max(1, Math.min(99, Math.round((synth.finalAttempt / synth.finalAttemptTotal) * 100)));
    }
    if (synth?.chunk > 0 && synth?.totalChunks > 0) {
        return Math.max(1, Math.min(99, Math.round((synth.chunk / synth.totalChunks) * 100)));
    }
    return 10;
}

/**
 * Human-friendly line for reduce/finalization step.
 */
export function formatSynthesisLine(phase, synth) {
    const p = String(phase || "").toUpperCase();
    if (p !== "REDUCE") {
        return "";
    }

    if (synth?.finalAttempt > 0 && synth?.finalAttemptTotal > 0) {
        return `Finalizing report (${synth.finalAttempt}/${synth.finalAttemptTotal})`;
    }

    return "Finalizing report...";
}

/**
 * Helper for safely updating synth state from activity text.
 */
export function mergeSynthFromActivity(currentSynth, activity) {
    const parsed = parseSynthesisFromActivity(activity);
    if (!parsed) {
        return currentSynth;
    }
    return { ...(currentSynth || {}), ...parsed };
}

/**
 * Reset shape for synthesis tracking.
 */
export function createInitialSynthState() {
    return {
        level: 0,
        chunk: 0,
        totalChunks: 0,
        finalAttempt: 0,
        finalAttemptTotal: 4
    };
}
