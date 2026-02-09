package com.embrace.cup.zoo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UtilRest {
    
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ObjectMapper mapper = new ObjectMapper();
    

    public static String get(String url, Map<String, String> headers) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                                        .uri(URI.create(url))
                                        .GET();
            if (headers != null) {
                headers.forEach(builder::header);
            }
            HttpRequest request = builder.build();
            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String post(String url, Object body, Map<String, String> headers) {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
            if (headers != null) {
                headers.forEach(builder::header);
            }
            HttpRequest request = builder.build();

            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getForMap(String url, Map<String, String> headers) {
        
        try {
            String json = get(url, headers);
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> postForMap(String url, Object body, Map<String, String> headers) {
        try {
            String json = post(url, body, headers);
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
