package com.telegram.bot;

import com.telegram.MessageBuilder;
import com.telegram.handler.NotifierHandler;
import com.telegram.handler.RoundHandler;
import com.telegram.model.Round;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.toIntExact;

public class Bot extends TelegramLongPollingBot {

    private final String TOKEN = "608768766:AAHk7FUNTIerYiCsYVsThpqAVog5ALRlLHU";
    private final String BOT_NAME = "doublegrambot";
    private final long REGISTRATION_DELAY = 2 * 60 * 60;
    private final long ROUND_DELAY = 2 * 60 * 60;
    private volatile int iteration;

    private NotifierHandler notifierHandler;
    private RoundHandler roundHandler;
    private MessageBuilder msgBuilder;

    private ScheduledExecutorService scheduler;
    private ReentrantLock lock;

    public Bot() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.notifierHandler = new NotifierHandler();
        this.roundHandler = new RoundHandler(new Round());
        this.msgBuilder = new MessageBuilder();
        this.lock = new ReentrantLock();
        this.iteration = 1;
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            String msg = update.getMessage().getText();
            long chatID = update.getMessage().getChatId();
            String username = update.getMessage().getChat().getUserName();
            if (!update.getMessage().getText().isEmpty() ) {
                sendMsg(msg, chatID, username);
            }
        } else if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            long msgID = update.getCallbackQuery().getMessage().getMessageId();
            long chatID = update.getCallbackQuery().getMessage().getChatId();
            String username = update.getCallbackQuery().getFrom().getUserName();
            System.out.println(msgID + " " + chatID + " " + username);
            if (!callData.isEmpty() && !username.isEmpty()){
                answerCallBack(callData, msgID, chatID, username);
            }
        }
    }

    public synchronized void sendMsg(String msg, Long chatId, String username) {
        switch (msg) {
            case "/start": {
                SendMessage sendMessage = new SendMessage().setChatId(chatId);
                try {
                    if (lock.tryLock()) {
                        notifierHandler.createNotification(chatId, username);
                        sendMessage.setText(msgBuilder.greeting());
                    }
                } finally {
                    lock.unlock();
                }
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case "/help": {
                SendMessage sendMessage = new SendMessage().setChatId(chatId);
                sendMessage.setText(msgBuilder.getBuiltinCommands());

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case "/round": {
                SendMessage sendMessage = new SendMessage().setChatId(chatId);
                if (roundHandler.getRound().isRoundOngoing() && !roundHandler.getRound().isRegistrationOngoing()) {
                    sendMessage.setText(msgBuilder.onRegistrationEnded());
                }
                else if (roundHandler.getRound().isRegistrationOngoing() && !roundHandler.isUserRegistered(chatId)) {
                    sendMessage.setText(msgBuilder.getInvitationMsg());
                    InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                    List<InlineKeyboardButton> row = new ArrayList<>();
                    InlineKeyboardButton button = new InlineKeyboardButton().setText("Click to engage").setCallbackData("chatID");
                    row.add(button);
                    rows.add(row);
                    keyboardMarkup.setKeyboard(rows);
                    sendMessage.setReplyMarkup(keyboardMarkup);
                }
                else if (roundHandler.getRound().isRegistrationOngoing() && roundHandler.isUserRegistered(chatId)) {
                    sendMessage.setText(msgBuilder.alreadyRegistered());
                }
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
    }
    public synchronized void answerCallBack(String callData, Long msgID, Long chatID, String username) {
        switch (callData) {
            case "chatID": {
                if (roundHandler.getRound().isRegistrationOngoing() && !roundHandler.isUserRegistered(chatID)) {
                    EditMessageText newMessage = new EditMessageText();
                    newMessage.setChatId(chatID).setMessageId(toIntExact(msgID)).setText(msgBuilder.onRegistrationMessage());
                    try {
                        if (lock.tryLock()) {
                            roundHandler.addUser(chatID, username);
                        }
                    } finally {
                        lock.unlock();

                        try {
                            execute(newMessage);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }

                        break;
                    }
                }
                else if (roundHandler.getRound().isRegistrationOngoing() && roundHandler.isUserRegistered(chatID)) {
                    SendMessage newMessage = new SendMessage();
                    newMessage.setChatId(chatID).setText(msgBuilder.alreadyRegistered());
                    try {
                        execute(newMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    } finally {
                        break;
                    }
                }
                else if ( roundHandler.getRound().isRoundOngoing() ) {
                    EditMessageText newMessage = new EditMessageText();
                    newMessage.setChatId(chatID).setMessageId(toIntExact(msgID)).setText(msgBuilder.onRegistrationEnded());
                    try {
                        execute(newMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    } finally {
                        break;
                    }
                }
            }
        }
    }

    public void execute() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (lock.tryLock()) {
                    roundHandler.getRound().setRoundOngoing(false);
                    roundHandler.getRound().setRegistrationOngoing(true);
                }
            } finally {
                lock.unlock();
            }

            Map<String, SendMessage> map = notifierHandler.getMap();

            for (ConcurrentHashMap.Entry<String, SendMessage> entry: map.entrySet()) {
                SendMessage msg = entry.getValue();
                InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton().setText("Click to engage").setCallbackData("chatID")
                        .setCallbackData(entry.getKey())
                        .setCallbackData(entry.getValue().getChatId());
                row.add(button);
                rows.add(row);
                keyboardMarkup.setKeyboard(rows);

                msg.setReplyMarkup(keyboardMarkup);
                try {
                    execute(msg);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                } finally {
                    map.clear();
                }
            }

        }, getZonedRegistrationStartTime(), REGISTRATION_DELAY, TimeUnit.SECONDS);

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (lock.tryLock()) {
                    roundHandler.getRound().setRoundOngoing(true);
                    roundHandler.getRound().setRegistrationOngoing(false);
                    if (this.iteration == 13) {
                        this.iteration = 1;
                        roundHandler.getRound().setIteration(iteration);
                    }
                    else {
                        this.iteration++;
                        roundHandler.getRound().setIteration(iteration);
                    }
                }
            } finally {
                lock.unlock();
            }
            Iterator<SendMessage> iter = roundHandler.messageIterator();
            while (iter.hasNext()) {
                try {
                    execute(iter.next());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                } finally {
                    roundHandler.getChatAndUserIds().clear();
                }
            }
        }, getZonedRoundStartTime(), ROUND_DELAY, TimeUnit.SECONDS);
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }


    private final long getZonedRegistrationStartTime() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedStartTime = zonedNow.withHour(23).withMinute(30).withSecond(0).withNano(0);
        if (zonedNow.compareTo(zonedStartTime) > 0) {
            zonedStartTime = zonedStartTime.plusDays(1);
        }
        Duration duration = Duration.between(zonedNow, zonedStartTime);
        return duration.getSeconds();
    }

    private final long getZonedRoundStartTime() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedStartTime = zonedNow.withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (zonedNow.compareTo(zonedStartTime) > 0) {
            zonedStartTime = zonedStartTime.plusDays(1);
        }
        Duration duration = Duration.between(zonedNow, zonedStartTime);
        return duration.getSeconds();
    }
}
