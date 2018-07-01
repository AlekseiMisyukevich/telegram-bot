package com.telegram.api;

import com.telegram.bot.Bot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

public class App {

//    private static final String HOST = "128.199.98.92";
//    private static final String PORT = "88";
//    private static final String certRelPath = FileSystems.getDefault().getPath("./cert.pem").toString();

    public static void main(String[] args) {

        ApiContextInitializer.init();

        TelegramBotsApi botapi = new TelegramBotsApi();
        Bot bot = new Bot();

        try {
            botapi.registerBot(bot);
            bot.executeTasks();
        } catch (TelegramApiRequestException e) {
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

//
//        final String url = String.format("https://%s:%s/%s", HOST, PORT, bot.getBotToken());
//
//        try {
//            final URI certAbsUri = new URI( certRelPath );
//            bot.setWebhook(url, certAbsUri.getPath());
//            botapi.registerBot(bot);
//            bot.executeTasks();
//        }
//        catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        } catch (TelegramApiRequestException e) {
//            throw new RuntimeException(e);
//        }
//        catch (Exception e) {
//            throw new RuntimeException(e);
//        }

    }

}

