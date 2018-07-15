package com.telegram.bot;

import com.telegram.MessageBuilder;
import com.telegram.RepleyKeyboardBuilder;
import com.telegram.dao.UserRepo;
import com.telegram.handler.RegistrationHandler;
import com.telegram.handler.RoundHandler;
import com.telegram.model.CustomUser;
import com.telegram.model.Round;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static java.lang.Math.toIntExact;

public class Bot extends TelegramLongPollingBot {

    private final String TOKEN = "608768766:AAHk7FUNTIerYiCsYVsThpqAVog5ALRlLHU";
    private final String BOT_NAME = "doublegrambot";
    private final long DELAY = 7200;
    private final String REPLIED_MSG_INDICATOR = "Greetings...";
    private volatile byte cnt;

    private RegistrationHandler registrationHandler;
    private RoundHandler roundHandler;
    private MessageBuilder msgBuilder;
    private UserRepo repo;
    private RepleyKeyboardBuilder replyKeyboard;

    private ScheduledExecutorService scheduler;
    private ReentrantLock lock;

    private static final Logger LOGGER = Logger.getLogger(Bot.class.getName());

    public Bot() {
        Round round = new Round();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.registrationHandler = new RegistrationHandler();
        this.roundHandler = new RoundHandler(round);
        this.msgBuilder = new MessageBuilder(round);
        this.repo = new UserRepo();
        this.lock = new ReentrantLock();
        this.cnt = 10;
        this.replyKeyboard = new RepleyKeyboardBuilder();
    }

    /*@Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        if (update.hasMessage()) {
            String msg = update.getMessage().getText();
            long chatID = update.getMessage().getChatId();
            String username = update.getMessage().getChat().getUserName();
            if (!update.getMessage().getText().isEmpty()) {
                sendMsg(msg, chatID, username);
            }
        } else if (update.getMessage().isReply()) {
            long chatID = update.getMessage().getChatId();
            String username = update.getMessage().getText();
            registrationHandler.createNotification(chatID, username);
            LOGGER.info(chatID + " " + username + " added.");
        } else if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            long msgID = update.getCallbackQuery().getMessage().getMessageId();
            long chatID = update.getCallbackQuery().getMessage().getChatId();
            String username = update.getCallbackQuery().getFrom().getUserName();
            if (!callData.isEmpty() && !username.isEmpty()) {
                answerCallBack(callData, msgID, chatID, username);
            }
        }
        return  null;
    }*/

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasCallbackQuery() && update.hasMessage() && !update.getMessage().isReply()) {

            String msg = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();
            String username = update.getMessage().getChat().getUserName();

            if (!msg.isEmpty() && !username.isEmpty()) {
                sendMsg(msg, chatId, userId, username);
            } else {
                return;
            }
        } else if (!update.hasCallbackQuery() && update.getMessage().isReply() && update.getMessage().getReplyToMessage().getText().contains(REPLIED_MSG_INDICATOR)) {
            System.out.println(update.getMessage().getReplyToMessage().getText());
            User newUser = update.getMessage().getFrom();
            final String msg = "startReply";
            long userID = newUser.getId();
            long chatID = update.getMessage().getChatId();
            String firstName = newUser.getFirstName();
            String lastName = newUser.getLastName();
            String lang = newUser.getLanguageCode();
            String username = update.getMessage().getChat().getUserName();
            String instaName = update.getMessage().getText();

            if (!username.isEmpty() && chatID != 0) {
                sendMsg(msg, userID, chatID, firstName, lastName, lang, username, instaName);
            } else {
                return;
            }

        } else if (update.hasCallbackQuery()) {

            if (!update.getCallbackQuery().getData().isEmpty()) {
                answerCallBack(update);
            } else {
                return;
            }
        }
    }

    public synchronized void sendMsg(String msg, long userId, long chatId, String firstName, String lastName, String lang, String username, String instaName) {
        switch (msg) {
            case "startReply": {
                repo.add(userId, new CustomUser(chatId, firstName, lastName, username, instaName, lang));
                registrationHandler.createNotification(chatId, msgBuilder.onInlineButtonSend());
                SendMessage sendMessage = new SendMessage();
                try {
                    sendMessage.setChatId(chatId);
                    sendMessage.setText(msgBuilder.getRoundStatus());
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
    }

    public synchronized void sendMsg(String msg, long chatId, long userId, String username) {
        switch (msg) {
            case "/start": {
                if (!repo.contains(userId)) {
                    SendMessage sendMessage = new SendMessage();
                    try {
                        sendMessage.setChatId(chatId);
                        sendMessage.setText(msgBuilder.greeting());
                        sendMessage.setReplyMarkup(replyKeyboard.getNameButton(username));
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                } else {
                    SendMessage sendMessage = new SendMessage().setChatId(chatId).setText(msgBuilder.getBuiltinCommands());
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
            } case "/help": {
                SendMessage sendMessage = new SendMessage().setChatId(chatId);
                sendMessage.setText(msgBuilder.getBuiltinCommands());
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                break;
            } case "/round": {
                final InlineKeyboardMarkup keyboardMarkup = replyKeyboard.getEnganeButton();
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
            } case "/whoami": {
                SendMessage sendMessage = new SendMessage().setChatId(chatId);
                sendMessage.setText(msgBuilder.about());
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
    }

    public synchronized void answerCallBack(Update update) {
        String callData = update.getCallbackQuery().getData();
        switch (callData) {
            case "reg": {
                User newUser = update.getCallbackQuery().getFrom();
                long userID = newUser.getId();
                long msgID = update.getCallbackQuery().getMessage().getMessageId();
                long chatID = update.getCallbackQuery().getMessage().getChatId();
                String firstName = newUser.getFirstName();
                String lastName = newUser.getLastName();
                String username = newUser.getUserName();
                String instaName = username;
                String lang = newUser.getLanguageCode();

                if (repo.contains(userID)) {
                    EditMessageText newMessage = new EditMessageText();
                    newMessage.setChatId(chatID).setMessageId(toIntExact(msgID)).setText(msgBuilder.alreadyRegistered());
                    try {
                        execute(newMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        EditMessageText newMessage = new EditMessageText();
                        newMessage.setChatId(chatID).setMessageId(toIntExact(msgID)).setText(msgBuilder.getRoundStatus());
                        if (lock.tryLock()) {
                            repo.add(userID, new CustomUser(chatID, firstName, lastName, username, instaName, lang));
                            registrationHandler.createNotification(chatID, msgBuilder.onInlineButtonSend());
                            LOGGER.info(username + " added.");
                        }
                        try {
                            execute(newMessage);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
            case "round": {
                User newUser = update.getCallbackQuery().getFrom();
                long msgID = update.getCallbackQuery().getMessage().getMessageId();
                long userID = newUser.getId();
                long chatID = update.getCallbackQuery().getMessage().getChatId();
                String username = newUser.getUserName();
                String instaName = repo.getById(userID).getInstaName();

                if (roundHandler.getRound().isRegistrationOngoing() && !roundHandler.isUserRegistered(chatID)) {
                    EditMessageText newMessage = new EditMessageText();
                    newMessage.setChatId(chatID).setMessageId(toIntExact(msgID)).setText(msgBuilder.onRegistrationMessage());
                    try {
                        if (lock.tryLock()) {
                            roundHandler.addUser(chatID, instaName);
                            LOGGER.info(username + " joined round under nickname " + instaName);
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
                } else if (roundHandler.getRound().isRegistrationOngoing() && roundHandler.isUserRegistered(chatID)) {
                    SendMessage newMessage = new SendMessage();
                    newMessage.setChatId(chatID).setText(msgBuilder.alreadyRegistered());
                    try {
                        execute(newMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    } finally {
                        break;
                    }
                } else if (roundHandler.getRound().isRoundOngoing()) {
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
            ArrayList<SendMessage> messages = registrationHandler.getMessages();
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

            Iterator<SendMessage> iter = messages.iterator();
            while (iter.hasNext()) {
                try {
                    execute(iter.next());
                } catch(TelegramApiException e) {
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

            Iterator<SendMessage> iter = roundHandler.buildMessageIterator();
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
        ZonedDateTime zonedStartTime = zonedNow.withHour(17).withMinute(30).withSecond(0).withNano(0);
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
        ZonedDateTime zonedStartTime = zonedNow.withHour(18).withMinute(0).withSecond(0).withNano(0);
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
        ZonedDateTime zonedStartTime = zonedNow.withHour(19).withMinute(00).withSecond(0).withNano(0);
        if (zonedNow.compareTo(zonedStartTime) > 0) {
            zonedStartTime = zonedStartTime.plusDays(1);
        }
        Duration duration = Duration.between(zonedNow, zonedStartTime);
        return duration.getSeconds();
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
