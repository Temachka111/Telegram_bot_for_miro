package com.example.Telegram_bot_for_Miro.auth;

import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class OAuthService {

    @Value("${miro.client-id}")
    private String clientId;

    @Value("${miro.client-secret}")
    private String clientSecret;

    private final TokenStorage tokenStorage;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public void refreshToken() throws Exception {

        String body = """
        {
          "grant_type": "refresh_token",
          "client_id": "%s",
          "client_secret": "%s",
          "refresh_token": "%s"
        }
        """.formatted(clientId, clientSecret, tokenStorage.getRefreshToken());

        Request request = new Request.Builder()
                .url("https://api.miro.com/v1/oauth/token")
                .post(RequestBody.create(body, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {

            String json = response.body().string();
            JsonNode node = mapper.readTree(json);

            String accessToken = node.path("access_token").asText();
            String refreshToken = node.path("refresh_token").asText(null);
            int expiresIn = node.path("expires_in").asInt();

            tokenStorage.save(accessToken, refreshToken, expiresIn);
        }
    }
}
