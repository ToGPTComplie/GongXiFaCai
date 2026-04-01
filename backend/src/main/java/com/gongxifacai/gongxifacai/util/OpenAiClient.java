package com.gongxifacai.gongxifacai.util;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * OpenAI API 调用工具类（风格与 HoldingServiceImpl 中的 Yahoo Finance 调用保持一致）
 */
@Slf4j
@Component
public class OpenAiClient {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.base-url}")
    private String baseUrl;

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    /**
     * 发送 chat 请求，要求返回 JSON 格式
     *
     * @param systemPrompt 系统提示词（角色设定 + 返回格式要求）
     * @param userPrompt   用户输入（组合数据）
     * @return OpenAI 返回的 JSON 字符串
     */
    public String chat(String systemPrompt, String userPrompt) {
        try {
            // 构建请求体
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);

            // 强制返回 JSON 格式
            JsonObject responseFormat = new JsonObject();
            responseFormat.addProperty("type", "json_object");
            requestBody.add("response_format", responseFormat);

            // 构建 messages 数组
            JsonArray messages = new JsonArray();

            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", systemPrompt);
            messages.add(systemMessage);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", userPrompt);
            messages.add(userMessage);

            requestBody.add("messages", messages);

            log.debug("Calling OpenAI API, model: {}", model);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("OpenAI API error, statusCode: {}, body: {}", response.statusCode(), response.body());
                throw new BusinessException(CommonErrorCode.SYSTEM_ERROR, "OpenAI API call failed with status: " + response.statusCode());
            }

            JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray choices = responseJson.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new BusinessException(CommonErrorCode.SYSTEM_ERROR, "OpenAI returned empty choices");
            }

            String content = choices.get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString();

            log.debug("OpenAI response received successfully");
            return content;

        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to call OpenAI API", ex);
            throw new BusinessException(CommonErrorCode.SYSTEM_ERROR, "Failed to call OpenAI API: " + ex.getMessage());
        }
    }
}

