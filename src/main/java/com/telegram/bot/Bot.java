package com.telegram.bot;

import com.telegram.MessageBuilder;
import com.telegram.handler.NotifierHandler;
import com.telegram.handler.RoundHandler;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.toIntExact;

public class Bot extends TelegramLongPollingBot {

    private final String TOKEN = "608768766:AAHk7FUNTIerYiCsYVsThpqAVog5ALRlLHU";
    private final String BOT_NAME = "doublegrambot";
    private final long REGISTRATION_DELAY = 3 * 60 * 60;
    private final long ROUND_DELAY = 3 * 60 * 60;

    private Bot bot;
    private NotifierHandler notifierHandler;
    private RoundHandler roundHandler;
    private MessageBuilder msgBuilder;

    private ScheduledExecutorService scheduler;
    private ReentrantLock lock;

    public Bot() {

        this.scheduler = Executors.newScheduledThreadPool(2);
        this.notifierHandler = new NotifierHandler();
        this.roundHandler = new RoundHandler();
        this.msgBuilder = new MessageBuilder();

    }

    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            switch (update.getMessage().getText()) {
                case "/start": {
                    Long chatId = update.getMessage().getChatId();
                    try {
                        if (lock.tryLock()) {
                            notifierHandler.createNotification(chatId);
                        }
                    } finally {
                        lock.unlock();
                    }
                    SendMessage message = new SendMessage().setChatId(chatId).setText(msgBuilder.greeting());
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                case "/help": {
                    Long chatId = update.getMessage().getChatId();
                    SendMessage message = new SendMessage().setChatId(chatId).setText(msgBuilder.getBuiltinCommands());
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                case "/round": {
                    Long chatId = update.getMessage().getChatId();
                    SendMessage msg = null;
                    if (roundHandler.getRound().isRoundOngoing()) {
                        msg = new SendMessage().setChatId(chatId).setText(msgBuilder.getRoundStatus(roundHandler.getRound().getIteration()));
                    }
                    else if (roundHandler.getRound().isRegistrationOngoing()) {
                        msg = new SendMessage().setChatId(chatId).setText(msgBuilder.getInvitationMsg());
                        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                        List<InlineKeyboardButton> row = new ArrayList<>();
                        InlineKeyboardButton button = new InlineKeyboardButton().setText("Click to engage").setCallbackData("chatID");
                        row.add(button);
                        rows.add(row);
                        keyboardMarkup.setKeyboard(rows);
                        msg.setReplyMarkup(keyboardMarkup);
                    }
                    if (msg != null) {
                        try {
                            execute(msg);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

        } else if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            long msgID = update.getCallbackQuery().getMessage().getMessageId();
            long chatID = update.getCallbackQuery().getMessage().getMessageId();
            String username = update.getMessage().getChat().getUserName();
            switch (callData) {
                case "chatID": {
                    EditMessageText newMessage = null;
                    if (roundHandler.getRound().isRegistrationOngoing()) {
                        try {
                            if (lock.tryLock()) {
                                newMessage = new EditMessageText().setChatId(chatID).setMessageId(toIntExact(msgID)).setText(msgBuilder.onRegistrationMessage());
                                roundHandler.addUser(chatID, username);
                            }
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        newMessage = new EditMessageText().setChatId(chatID).setMessageId(toIntExact(msgID)).setText(msgBuilder.onRegistrationEnded());
                    }
                    if (newMessage != null) {
                        try {
                            execute(newMessage);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    @PostConstruct
    private void execute() {
        scheduler.scheduleWithFixedDelay(() -> {
            roundHandler.getRound().setRegistrationOngoing(true);
            notifierHandler.getRound().setRoundOngoing(false);
            Iterator<SendMessage> iter = notifierHandler.getIterator();
            while (iter.hasNext()) {
                InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton().setText("Click to engage").setCallbackData("chatID");
                row.add(button);
                rows.add(row);
                keyboardMarkup.setKeyboard(rows);
                iter.next().setReplyMarkup(keyboardMarkup);
                try {
                    execute(iter.next());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }, getZonedRegistrationStartTime(), REGISTRATION_DELAY, TimeUnit.SECONDS);

        scheduler.scheduleWithFixedDelay(() -> {
            roundHandler.getRound().setRegistrationOngoing(false);
            int iteration = 1;
            notifierHandler.getRound().setRoundOngoing(true);
            if (iteration == 8) {
                iteration = 1;
                roundHandler.getRound().setIteration(iteration);
            } else {
                roundHandler.getRound().setIteration(iteration);
                iteration++;
            }
            Iterator<SendMessage> iter = notifierHandler.getIterator();

            while (iter.hasNext()) {
                try {
                    execute(iter.next());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }, getZonedRoundStartTime(), ROUND_DELAY, TimeUnit.HOURS);
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

    private long getZonedRegistrationStartTime() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedStartTime = zonedNow.withHour(23).withMinute(30);
        if (zonedNow.compareTo(zonedStartTime) > 0) {
            zonedStartTime = zonedStartTime.plusDays(1);
        }
        Duration duration = Duration.between(zonedNow, zonedStartTime);
        return duration.getSeconds();
    }

    private long getZonedRoundStartTime() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedStartTime = zonedNow.withHour(00).withMinute(00);
        if (zonedNow.compareTo(zonedStartTime) > 0) {
            zonedStartTime = zonedStartTime.plusDays(1);
        }
        Duration duration = Duration.between(zonedNow, zonedStartTime);
        return duration.getSeconds();
    }
}
