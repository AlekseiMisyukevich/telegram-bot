package com.telegram.api;

import com.telegram.bot.Bot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class App {


    private static ScheduledExecutorService scheduler;

    public static void main(String[] args) {
        ApiContextInitializer.init();

        scheduler = Executors.newScheduledThreadPool(3);

        TelegramBotsApi botapi = new TelegramBotsApi();
        try {
            Bot bot = new Bot();
            botapi.registerBot(bot);
            bot.executeTasks();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

