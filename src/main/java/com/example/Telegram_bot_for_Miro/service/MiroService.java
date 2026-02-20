package com.example.Telegram_bot_for_Miro.service;

import com.example.Telegram_bot_for_Miro.auth.OAuthService;
import com.example.Telegram_bot_for_Miro.auth.TokenStorage;
import com.example.Telegram_bot_for_Miro.dto.FrameData;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Random;

@RequiredArgsConstructor
@Service
public class MiroService {

    @Value("${miro.board-id}")
    private String boardId;

    private final TokenStorage tokenStorage;
    private final OAuthService oauthService;
    private final OkHttpClient client = new OkHttpClient();

    public void createSticky(String text) {

        try {

            FrameData frame = getFrameByTitle("Heap");

            int x = randomInFrame(frame.x(), frame.width(), 200);
            int y = randomInFrame(frame.y(), frame.height(), 150);

            String json = """
        {
          "data": {
            "content": "%s"
          },
          "position": {
            "x": %d,
            "y": %d
          }
        }
        """.formatted(text, x, y);

            Request request = new Request.Builder()
                    .url("https://api.miro.com/v2/boards/" + boardId + "/sticky_notes")
                    .post(RequestBody.create(json, MediaType.get("application/json")))
                    .addHeader("Authorization", "Bearer " + tokenStorage.getAccessToken())
                    .build();

            try (Response response = client.newCall(request).execute()) {
                System.out.println("MIRO STATUS: " + response.code());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int random() {
        return new Random().nextInt(1000);
    }

    private int randomInFrame(double frameCenter, double frameSize, double objectSize) {

        double padding = 20;

        double min = frameCenter - frameSize / 2 + objectSize / 2 + padding;
        double max = frameCenter + frameSize / 2 - objectSize / 2 - padding;

        return (int) (min + Math.random() * (max - min));
    }

    private FrameData getFrameByTitle(String title) throws Exception {

        Request request = new Request.Builder()
                .url("https://api.miro.com/v2/boards/" + boardId + "/items?type=frame")
                .addHeader("Authorization", "Bearer " + tokenStorage.getAccessToken())
                .build();

        try (Response response = client.newCall(request).execute()) {

            System.out.println("FRAME STATUS: " + response.code());

            String json = response.body().string();
            System.out.println("FRAME RESPONSE: " + json);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            if (!root.has("data")) {
                throw new RuntimeException("No 'data' field in response");
            }

            for (JsonNode item : root.get("data")) {

                String frameTitle = item.path("data").path("title").asText("");

                if (frameTitle.equalsIgnoreCase(title)) {

                    double x = item.path("position").path("x").asDouble();
                    double y = item.path("position").path("y").asDouble();
                    double width = item.path("geometry").path("width").asDouble();
                    double height = item.path("geometry").path("height").asDouble();

                    return new FrameData(x, y, width, height);
                }
            }
        }

        throw new RuntimeException("Frame not found: " + title);
    }
}