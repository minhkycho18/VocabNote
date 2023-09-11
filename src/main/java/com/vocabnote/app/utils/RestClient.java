package com.vocabnote.app.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabnote.app.exception.HttpErrorStatusException;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RestClient {
    private String url;
    private String token;
    private String httpMethod;
    private RequestBody requestBody;
    private String contentType;

    public RestClient() {
    }

    public RestClient setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public RestClient setBearerToken(String token) {
        this.token = token;
        return this;
    }

    public <T> RestClient setJsonRequestBody(T requestBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        MediaType mediaType = MediaType.parse("application/json");
        try {
            String json = objectMapper.writeValueAsString(requestBody);
            this.requestBody = RequestBody.create(json, mediaType);
        } catch (JsonProcessingException e) {
            throw new HttpErrorStatusException(400);
        }
        return this;
    }

    public RestClient setMultipartRequestBody(String keyName, File file) {
        this.requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(keyName, file.getName(),
                        RequestBody.create(file, MediaType.parse("image/jpeg")))
                .build();
        return this;
    }

    public RestClient setUrl(String url) {
        this.url = url;
        return this;
    }

    public RestClient setMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public <T> T call(Class<T> clazz) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();
        Request.Builder requestBuilder = new Request.Builder()
                .url(this.url)
                .method(this.httpMethod, this.requestBody);
        if (this.contentType != null) {
            requestBuilder.addHeader("Content-Type", this.contentType);
        }
        if (this.token != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + this.token);
        }
        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() / 100 == 4 || response.code() / 100 == 5) {
                throw new HttpErrorStatusException(response.code());
            }
            ObjectMapper objectMapper = new ObjectMapper();
            if (response.body() != null) {
                return objectMapper.readValue(response.body().string(), clazz);
            }
        } catch (IOException e) {
            throw new HttpErrorStatusException(400);
        }
        return null;
    }
}

