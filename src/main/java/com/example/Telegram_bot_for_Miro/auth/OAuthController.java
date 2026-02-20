package com.example.Telegram_bot_for_Miro.auth;

import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class OAuthController {

    @Value("${miro.client-id}")
    private String clientId;

    @Value("${miro.client-secret}")
    private String clientSecret;

    private final TokenStorage tokenStorage;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    @Value("${miro.redirect-uri}")
    private String redirectUri;

    @GetMapping("/oauth/authorize")
    public String authorize() {

        String url = "https://miro.com/oauth/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&scope=boards:read boards:write";

        return "<a href=\"" + url + "\">Authorize Miro</a>";
    }

    @GetMapping("/oauth/callback")
    public String callback(@RequestParam String code) throws Exception {

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .build();

        Request request = new Request.Builder()
                .url("https://api.miro.com/v1/oauth/token")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {

            String json = response.body().string();
            System.out.println("MIRO RESPONSE: " + json);

            JsonNode node = mapper.readTree(json);

            if (node.get("access_token") == null) {
                return "OAuth failed: " + json;
            }

            String accessToken = node.path("access_token").asText();
            String refreshToken = node.path("refresh_token").asText(null);
            int expiresIn = node.path("expires_in").asInt();

            tokenStorage.save(accessToken, refreshToken, expiresIn);

            return "OAuth success. Token saved.";
        }
    }
}
