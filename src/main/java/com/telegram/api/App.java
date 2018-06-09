package com.telegram.api;

import com.telegram.bot.Bot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.*;

public class App {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi botapi = new TelegramBotsApi();
        try {
            Bot bot = new Bot();

            botapi.registerBot(bot);
            botapi.registerBot(new Bot());

            bot.execute();
        } catch (TelegramApiRequestException e) {
            throw new RuntimeException(e);
        }
    }

}

