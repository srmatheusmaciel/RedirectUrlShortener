package com.matheusmaciel.redirectUrlShortener;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Main implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final S3Client s3Client = S3Client.builder().build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        String pathParameters = (String) input.get("rawPath");
        String shortUrlCode = pathParameters.replace("/", "");

        if(shortUrlCode.isEmpty()) {
            throw  new IllegalArgumentException("Short url code cannot be null or empty");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("url-shortener-storage-maciel")
                .key(shortUrlCode + ".json")
                .build();

        InputStream s3ObjectSream;

        try {
            s3ObjectSream = s3Client.getObject(getObjectRequest);

        } catch (Exception e) {
            throw new RuntimeException("Error getting object from s3" + e.getMessage());
        }

        UrlData urlData;

        try {
            urlData = objectMapper.readValue(s3ObjectSream, UrlData.class);
        } catch (Exception e) {
            throw new RuntimeException("Error reading object from s3" + e.getMessage());
        }

        long currentTimeInSeconds = System.currentTimeMillis() / 1000;

        Map<String, Object> response = new HashMap<>();

        if(urlData.getExpirationTime() < currentTimeInSeconds) {

            response.put("statusCode", 410);
            response.put("body", "This short url has expired");
            return response;

        }

        response.put("statusCode", 302);
        Map<String, String> headers = new HashMap<>();
        headers.put("Location", urlData.getOriginalUrl());
        response.put("headers", headers);

        return response;


    }
}