package com.telegram.bot;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {

    private final String TOKEN = "535311189:AAEg7cibGrM9H1FtgMKAJ86gze7dmNI_9KE";
    private final String BOT_NAME = "roundgrambot";

    public void onUpdateReceived(Update update) {

        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {

            // Set variables
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            if (update.getMessage().getText().equals("/start")) {

                // Create a message object
                SendMessage message = new SendMessage().setChatId(chat_id).setText("You sent " + message_text);

                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();

                rowInline.add(
                        new InlineKeyboardButton().setText("update message text").setCallbackData("update_msg_text"));

                rowsInline.add(rowInline);
                keyboard.setKeyboard(rowsInline);

                try {
                    execute(message); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            } else {

            }

        } else if (update.hasCallbackQuery()) {

            String callData = update.getCallbackQuery().getData();
            long msgID = update.getCallbackQuery().getMessage().getMessageId();
            long chatID = update.getCallbackQuery().getMessage().getMessageId();

            if (callData.equals("update_msg_text")) {

                String answer = "Update message text";

                EditMessageText new_message = new EditMessageText().setChatId(chatID).setMessageId(toIntExact(msgID))
                        .setText(answer);

                try {
                    execute(new_message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

}
