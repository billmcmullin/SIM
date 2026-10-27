package com.sim.chatserver.service;

import java.io.StringReader;
import java.util.List;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonNumber;
import com.sim.chatserver.model.review.ReviewJobStatus;
import com.sim.chatserver.util.CommonLiteralConstants;

final class ReviewJobStatusCodec {
    private static final String EMPTY = "";
    private ReviewJobStatusCodec() { }

    static String encode(ReviewJobStatus status) {
        return Json.createObjectBuilder(status.toJson()).add(CommonLiteralConstants.FIELD_RAW_RESPONSE_BODY, status.getRawResponseBody()).build().toString();
    }

    static ReviewJobStatus decode(String payload) {
        try (var reader = Json.createReader(new StringReader(payload))) {
            return decode(reader.readObject());
        }
    }

    static ReviewJobStatus decode(JsonObject json) {
        return ReviewJobStatus.builder().jobId(json.getString(CommonLiteralConstants.FIELD_JOB_ID)).requestId(json.getString(CommonLiteralConstants.FIELD_REQUEST_ID, EMPTY))
            .phase(ReviewJobStatus.Phase.valueOf(json.getString(CommonLiteralConstants.FIELD_PHASE)))
            .done(json.getBoolean(CommonLiteralConstants.FIELD_DONE)).success(json.getBoolean(CommonLiteralConstants.FIELD_SUCCESS))
            .totalSelected(json.getInt(CommonLiteralConstants.FIELD_TOTAL_SELECTED)).totalBatches(json.getInt(CommonLiteralConstants.FIELD_TOTAL_BATCHES))
            .completedBatches(json.getInt(CommonLiteralConstants.FIELD_COMPLETED_BATCHES)).failedBatches(json.getInt(CommonLiteralConstants.FIELD_FAILED_BATCHES))
            .retries(json.getInt(CommonLiteralConstants.FIELD_RETRIES)).allSelectedChatIds(strings(json, CommonLiteralConstants.FIELD_ALL_SELECTED_CHAT_IDS))
            .usedChatIds(strings(json, CommonLiteralConstants.FIELD_USED_CHAT_IDS)).missingChatIds(strings(json, CommonLiteralConstants.FIELD_MISSING_CHAT_IDS))
            .coveragePercent(json.getInt(CommonLiteralConstants.FIELD_COVERAGE_PERCENT)).coverageComplete(json.getBoolean(CommonLiteralConstants.FIELD_COVERAGE_COMPLETE))
            .startedAtEpochMs(json.getJsonNumber(CommonLiteralConstants.FIELD_STARTED_AT_EPOCH_MS).longValue())
            .updatedAtEpochMs(json.getJsonNumber(CommonLiteralConstants.FIELD_UPDATED_AT_EPOCH_MS).longValue())
            .finishedAtEpochMs(json.getJsonNumber(CommonLiteralConstants.FIELD_FINISHED_AT_EPOCH_MS).longValue())
            .httpStatus(json.getInt(CommonLiteralConstants.FIELD_HTTP_STATUS)).message(json.getString(CommonLiteralConstants.FIELD_MESSAGE, EMPTY))
            .errorMessage(json.getString(CommonLiteralConstants.FIELD_ERROR_MESSAGE, EMPTY)).finalReport(json.getString(CommonLiteralConstants.FIELD_FINAL_REPORT, EMPTY))
            .rawResponseBody(json.getString(CommonLiteralConstants.FIELD_RAW_RESPONSE_BODY, EMPTY)).contentType(json.getString(CommonLiteralConstants.FIELD_CONTENT_TYPE))
            .failedBatchIndexes(json.getJsonArray(CommonLiteralConstants.FIELD_FAILED_BATCH_INDEXES).getValuesAs(JsonNumber.class).stream().map(JsonNumber::intValue).toList())
            .warnings(strings(json, CommonLiteralConstants.FIELD_WARNINGS)).build();
    }

    private static List<String> strings(JsonObject json, String name) {
        return json.getJsonArray(name).getValuesAs(JsonString.class).stream().map(JsonString::getString).toList();
    }
}