package com.matheusmaciel.redirectUrlShortener;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.util.Map;

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {

    private final S3Client s3Client = S3Client.builder().build();

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        String pathParameters = (String) input.get("rawPath");
        String shortUrlCode = pathParameters.replace("/", "");

        if(shortUrlCode.isEmpty()) {
            throw  new IllegalArgumentException("Short url code cannot be null or empty");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("url-shortener-storage-maciel")
                .key(shortUrlCode + ".json")
                .build();

        return null;

    }
}