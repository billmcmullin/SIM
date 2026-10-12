package com.sim.chatserver.service.dashboard;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SessionAccumulatorData {
    public int count = 0;
    public Timestamp lastEntry = null;
    public final Map<String, Integer> widgetCounts = new LinkedHashMap<>();
}
