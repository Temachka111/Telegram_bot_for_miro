package com.example.Telegram_bot_for_Miro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class VoiceService {

    @Value("${openai.api.key}")
    private String openAiKey;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String voiceToText(Message message, String botToken) throws Exception {

        if (!message.hasVoice()) {
            return "";
        }

        String fileId = message.getVoice().getFileId();

        String getFileUrl = "https://api.telegram.org/bot"
                + botToken
                + "/getFile?file_id="
                + fileId;

        Request fileRequest = new Request.Builder()
                .url(getFileUrl)
                .build();

        String filePath;

        try (Response fileResponse = httpClient.newCall(fileRequest).execute()) {

            if (fileResponse.body() == null) return "error";

            String fileJson = fileResponse.body().string();

            JsonNode root = objectMapper.readTree(fileJson);
            filePath = root
                    .path("result")
                    .path("file_path")
                    .asText();

            if (filePath.isEmpty()) return "error";
        }

        String downloadUrl = "https://api.telegram.org/file/bot"
                + botToken
                + "/"
                + filePath;

        Request downloadRequest = new Request.Builder()
                .url(downloadUrl)
                .build();

        byte[] audioBytes;

        try (Response downloadResponse = httpClient.newCall(downloadRequest).execute()) {

            if (downloadResponse.body() == null) return "error";
            audioBytes = downloadResponse.body().bytes();
        }

        Path tempFile = Files.createTempFile("voice", ".ogg");
        Files.write(tempFile, audioBytes);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", "gpt-4o-mini-transcribe")
                .addFormDataPart(
                        "file",
                        "voice.ogg",
                        RequestBody.create(
                                tempFile.toFile(),
                                MediaType.parse("audio/ogg")
                        )
                )
                .build();

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .addHeader("Authorization", "Bearer " + openAiKey)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (response.body() == null) return "error";

            String json = response.body().string();
            System.out.println("OpenAI RAW RESPONSE: " + json);

            if (!response.isSuccessful()) return "error";

            JsonNode root = objectMapper.readTree(json);

            if (root.has("text")) {
                return root.get("text").asText();
            } else {
                return "error";
            }
        }
    }
}