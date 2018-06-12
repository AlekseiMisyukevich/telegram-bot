package com.telegram.bot;

import com.telegram.MessageBuilder;
import com.telegram.handler.RegistrationHandler;
import com.telegram.handler.RoundHandler;
import com.telegram.model.Round;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
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
    private final long DELAY = 7200;
    private volatile byte cnt;

    private RegistrationHandler registrationHandler;
    private RoundHandler roundHandler;
    private MessageBuilder msgBuilder;

    private ScheduledExecutorService scheduler;
    private ReentrantLock lock;

    public Bot() {
        Round round = new Round();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.registrationHandler = new RegistrationHandler();
        this.roundHandler = new RoundHandler(round);
        this.msgBuilder = new MessageBuilder(round);
        this.lock = new ReentrantLock();
        this.cnt = 11;
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
                        registrationHandler.createNotification(chatId, username);
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
                final InlineKeyboardMarkup keyboardMarkup = getMarkup();
                SendMessage sendMessage = new SendMessage().setChatId(chatId);
                if (roundHandler.getRound().isRoundOngoing()) {
                    sendMessage.setText(msgBuilder.onRegistrationEnded());
                }
                else if (roundHandler.getRound().isRegistrationOngoing() && !roundHandler.isUserRegistered(chatId)) {
                    sendMessage.setText(msgBuilder.getInvitationMsg());
                    sendMessage.setReplyMarkup(keyboardMarkup);
                }
                else if (roundHandler.getRound().isRegistrationOngoing() && roundHandler.isUserRegistered(chatId)) {
                    sendMessage.setText(msgBuilder.alreadyRegistered());
                }
                else {
                    sendMessage.setText(msgBuilder.getRoundStatus());
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

    public void executeTasks() {
        scheduler.scheduleWithFixedDelay(() -> {

            Map<String, SendMessage> map = registrationHandler.getMap();

            if (map.isEmpty()) {
                try {
                    Thread.currentThread().wait(DELAY);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                if (lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
                    roundHandler.getRound().setRoundOngoing(false);
                    roundHandler.getRound().setRegistrationOngoing(true);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }

            for (ConcurrentHashMap.Entry<String, SendMessage> entry : map.entrySet()) {
                SendMessage msg = entry.getValue().setText(msgBuilder.onInlineButtonSend());
                try {
                    execute(msg);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }, getRegistrationStartTime(), DELAY, TimeUnit.SECONDS);

        scheduler.scheduleWithFixedDelay(() -> {
            if (!roundHandler.getRound().isRegistrationOngoing()) {
                try {
                    Thread.currentThread().wait(DELAY);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                if (lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
                    roundHandler.getRound().setRoundOngoing(true);
                    roundHandler.getRound().setRegistrationOngoing(false);
                    if (this.cnt == 13) {
                        this.cnt = 1;
                        roundHandler.getRound().setIteration(cnt);
                    }
                    else {
                        this.cnt++;
                        roundHandler.getRound().setIteration(cnt);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }

            Iterator<SendMessage> iter = roundHandler.messageIterator();
            while (iter.hasNext()) {
                try {
                    execute(iter.next());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }, getRoundStartTime(), DELAY, TimeUnit.SECONDS);

        scheduler.scheduleWithFixedDelay(() -> {

            try {
                if (lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
                    roundHandler.getRound().setRoundOngoing(false);
                    roundHandler.getRound().setRegistrationOngoing(false);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }

            final Set<Long> chatID = roundHandler.getChatAndUserIds().keySet();

            try {
                for (long id: chatID) {
                    SendMessage msg = new SendMessage().setChatId(id).setText(msgBuilder.onRoundEnded());
                    execute(msg);
                }
            }
            catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            finally {
                roundHandler.getChatAndUserIds().clear();
            }
        }, getEndOfRoundTime(), DELAY, TimeUnit.SECONDS);
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

    private final long getRegistrationStartTime() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedStartTime = zonedNow.withHour(19).withMinute(30).withSecond(0).withNano(0);
        if (zonedNow.compareTo(zonedStartTime) > 0) {
            zonedStartTime = zonedStartTime.plusDays(1);
        }
        Duration duration = Duration.between(zonedNow, zonedStartTime);
        return duration.getSeconds();
    }

    private final long getRoundStartTime() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedStartTime = zonedNow.withHour(20).withMinute(0).withSecond(0).withNano(0);
        if (zonedNow.compareTo(zonedStartTime) > 0) {
            zonedStartTime = zonedStartTime.plusDays(1);
        }
        Duration duration = Duration.between(zonedNow, zonedStartTime);
        return duration.getSeconds();
    }

    private final long getEndOfRoundTime() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedStartTime = zonedNow.withHour(21).withMinute(20).withSecond(0).withNano(0);
        if (zonedNow.compareTo(zonedStartTime) > 0) {
            zonedStartTime = zonedStartTime.plusDays(1);
        }
        Duration duration = Duration.between(zonedNow, zonedStartTime);
        return duration.getSeconds();
    }

    private InlineKeyboardMarkup getMarkup () {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton().setText("Click to engage").setCallbackData("chatID");
        row.add(button);
        rows.add(row);
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }
}
