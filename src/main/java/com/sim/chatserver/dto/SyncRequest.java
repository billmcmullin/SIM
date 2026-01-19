package com.sim.chatserver.dto;

import java.util.List;

public class SyncRequest {
    public String baseUrl;
    public String apiKey;
    public String scope;
    public List<String> terms;
}
