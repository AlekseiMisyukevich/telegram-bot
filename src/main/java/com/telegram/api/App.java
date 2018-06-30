package com.telegram.api;

import com.telegram.bot.Bot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.WebhookBot;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class App {

    private static final String HOST = "127.0.0.1";
    private static final String PORT = "443";
    private static final String certRelPath = "bot/src/main/resources/cert.pem";

    public static void main(String[] args) {

        ApiContextInitializer.init();

        TelegramBotsApi botapi = new TelegramBotsApi();
        Bot bot = new Bot();

        final String url = String.format("https://%s:%s/%s", HOST, PORT, bot.getBotToken());

        try {
            final URI certAbsUri = new URI( certRelPath );
            System.out.print( certAbsUri.getPath() );
            bot.setWebhook(url, certAbsUri.getPath());

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (TelegramApiRequestException e) {
            throw new RuntimeException(e);
        }

        try {
            botapi.registerBot(bot);
            bot.executeTasks();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}

