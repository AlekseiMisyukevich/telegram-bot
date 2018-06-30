package com.telegram.bot;

import com.telegram.MessageBuilder;
import com.telegram.dao.UserRepo;
import com.telegram.handler.RegistrationHandler;
import com.telegram.handler.RoundHandler;
import com.telegram.model.Round;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.exceptions.TelegramApiValidationException;

import java.io.Serializable;
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

public class Bot extends TelegramWebhookBot {

    private final String TOKEN = "608768766:AAHk7FUNTIerYiCsYVsThpqAVog5ALRlLHU";
    private final String BOT_NAME = "doublegrambot";
    private final long DELAY = 7200;
    private volatile byte cnt;

    private RegistrationHandler registrationHandler;
    private RoundHandler roundHandler;
    private MessageBuilder msgBuilder;
    private UserRepo repo;

    private ScheduledExecutorService scheduler;
    private ReentrantLock lock;

    public Bot() {
        Round round = new Round();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.registrationHandler = new RegistrationHandler();
        this.roundHandler = new RoundHandler(round);
        this.msgBuilder = new MessageBuilder(round);
        this.repo = new UserRepo();
        this.lock = new ReentrantLock();
        this.cnt = 6;
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {

        if (update.hasMessage()) {
            String msg = update.getMessage().getText();
            long chatID = update.getMessage().getChatId();
            String username = update.getMessage().getChat().getUserName();
            if (!update.getMessage().getText().isEmpty()) {
                sendMsg(msg, chatID, username);
            }

        } else if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            long msgID = update.getCallbackQuery().getMessage().getMessageId();
            long chatID = update.getCallbackQuery().getMessage().getChatId();
            String username = update.getCallbackQuery().getFrom().getUserName();
            System.out.println(msgID + " " + chatID + " " + username);
            if (!callData.isEmpty() && !username.isEmpty()) {
                answerCallBack(callData, msgID, chatID, username);
            }
        }

        return null;
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
            try {
                if (lock.tryLock(2000, TimeUnit.MILLISECONDS)) {
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
            try {
                if (lock.tryLock(2000, TimeUnit.MILLISECONDS)) {
                    roundHandler.getRound().setRoundOngoing(true);
                    roundHandler.getRound().setRegistrationOngoing(false);
                    if (this.cnt == 12) {
                        roundHandler.getRound().setIteration(cnt);
                        this.cnt = 1;
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
                if (lock.tryLock(2000, TimeUnit.MILLISECONDS)) {
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
    public String getBotPath() {
        return null;
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
        ZonedDateTime zonedStartTime = zonedNow.withHour(3).withMinute(30).withSecond(0).withNano(0);
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
        ZonedDateTime zonedStartTime = zonedNow.withHour(4).withMinute(0).withSecond(0).withNano(0);
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
        ZonedDateTime zonedStartTime = zonedNow.withHour(5).withMinute(20).withSecond(0).withNano(0);
        if (zonedNow.compareTo(zonedStartTime) > 0) {
            zonedStartTime = zonedStartTime.plusDays(1);
        }
        Duration duration = Duration.between(zonedNow, zonedStartTime);
        return duration.getSeconds();
    }

    private final InlineKeyboardMarkup getMarkup () {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton().setText("Click to engage").setCallbackData("chatID");
        row.add(button);
        rows.add(row);
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    /* private final ZonedDateTime offsetStart() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime offset = null;

        final int hours = zonedNow.getHour();
        final int mins = zonedNow.getMinute();

        if ( hours == 0 && mins > 0 ) {
            offset = zonedNow.withHour(2).withMinute(0).withSecond(0).withNano(0);
            this.cnt = 1;
            this.roundHandler.getRound().setIteration(this.cnt);
            this.roundHandler.getRound().setRoundOngoing(false);
            this.roundHandler.getRound().setRegistrationOngoing(false);
        } else if ( (hours % 2 != 0) && mins < 30 ) {
            offset = zonedNow.withHour(hours + 1).withMinute(0).withSecond(0).withNano(0); }
        else if ( (hours % 2 != 0) && mins > 30 ) {
            offset = zonedNow.withHour(hours + 1).withMinute(0).withSecond(0).withNano(0);
        }
        else if ( hours != 0 && hours % 2 == 0 && mins < 30 ) {
            offset = zonedNow.withHour(hours).withMinute(0).withSecond(0).withNano(0);
        }
        else if ( hours != 0 && hours % 2 == 0 && mins > 30 ) {
            offset = zonedNow.withHour(hours).withMinute(0).withSecond(0).withNano(0);
        }

        return offset;
    } */
}
