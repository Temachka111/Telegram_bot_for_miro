package com.example.Telegram_bot_for_Miro.config;

import com.example.Telegram_bot_for_Miro.component.Bot;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
public class TelegramConfig {

    private final Bot bot;

    @PostConstruct
    public void register() throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
    }
}