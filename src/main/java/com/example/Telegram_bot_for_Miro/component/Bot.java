package com.example.Telegram_bot_for_Miro.component;

import com.example.Telegram_bot_for_Miro.service.MiroService;
import com.example.Telegram_bot_for_Miro.service.VoiceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class Bot extends TelegramLongPollingBot {

    private final MiroService miroService;
    private final VoiceService voiceService;
    private final String username;
    private final String token;

    public Bot(
            @Value("${telegram.bot.token}") String token,
            @Value("${telegram.bot.username}") String username,
            MiroService miroService,
            VoiceService voiceService
    ) {
        super(token);
        this.token = token;
        this.username = username;
        this.miroService = miroService;
        this.voiceService = voiceService;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (!update.hasMessage()) return;

        Message message = update.getMessage();

        try {

            if (message.hasText()) {
                miroService.createSticky(message.getText());
            }

            else if (message.hasVoice()) {
                String textFromVoice = voiceService.voiceToText(message, token);
                miroService.createSticky(textFromVoice);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}