package com.telegram.bot;

import com.telegram.MessageBuilder;
import com.telegram.RepleyKeyboardBuilder;
import com.telegram.dao.UserRepo;
import com.telegram.handler.RegistrationHandler;
import com.telegram.handler.RoundHandler;
import com.telegram.model.Round;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
        this.cnt = 12;
        this.replyKeyboard = new RepleyKeyboardBuilder();
    }

//    @Override
//    public BotApiMethod onWebhookUpdateReceived(Update update) {
//        if (update.hasMessage()) {
//            String msg = update.getMessage().getText();
//            long chatID = update.getMessage().getChatId();
//            String username = update.getMessage().getChat().getUserName();
//            if (!update.getMessage().getText().isEmpty()) {
//                sendMsg(msg, chatID, username);
//            }
//        } else if (update.getMessage().isReply()) {
//            long chatID = update.getMessage().getChatId();
//            String username = update.getMessage().getText();
//            registrationHandler.createNotification(chatID, username);
//            LOGGER.info(chatID + " " + username + " added.");
//        } else if (update.hasCallbackQuery()) {
//            String callData = update.getCallbackQuery().getData();
//            long msgID = update.getCallbackQuery().getMessage().getMessageId();
//            long chatID = update.getCallbackQuery().getMessage().getChatId();
//            String username = update.getCallbackQuery().getFrom().getUserName();
//            if (!callData.isEmpty() && !username.isEmpty()) {
//                answerCallBack(callData, msgID, chatID, username);
//            }
//        }
//        return  null;
//    }


    @Override
    public void onUpdateReceived(Update update) {
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
    }

    public synchronized void sendMsg(String msg, Long chatId, String username) {
        switch (msg) {
            case "/start": {
                if ( !registrationHandler.contains(chatId) ) {
                    SendMessage sendMessage = new SendMessage().setChatId(chatId);
                    try {
                        if (lock.tryLock()) {
                            sendMessage.setText(msgBuilder.greeting());
                            sendMessage.setReplyMarkup(replyKeyboard.getNameButton(username));
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
                } else {
                    SendMessage sendMessage = new SendMessage().setChatId(chatId);
                    sendMessage.setText(msgBuilder.getBuiltinCommands());
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

    public synchronized void answerCallBack(String callData, Long msgID, Long chatID, String username) {
        if (callData.contains(replyKeyboard.getFLAG())) {
            SendMessage sendMessage = new SendMessage().setChatId(chatID);
            try {
                if (lock.tryLock()) {
                    registrationHandler.createNotification(chatID, username);
                    LOGGER.info(chatID + " " + username + " added.");
                }
            } finally {
                lock.unlock();
            }
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        switch (callData) {
            case "chatID": {
                if (roundHandler.getRound().isRegistrationOngoing() && !roundHandler.isUserRegistered(chatID)) {
                    EditMessageText newMessage = new EditMessageText();
                    newMessage.setChatId(chatID).setMessageId(toIntExact(msgID)).setText(msgBuilder.onRegistrationMessage());
                    try {
                        if (lock.tryLock()) {
                            roundHandler.addUser(chatID, username);
                            LOGGER.info(chatID + " " + username + " joined round.");
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
        ZonedDateTime zonedStartTime = zonedNow.withHour(21).withMinute(30).withSecond(0).withNano(0);
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
        ZonedDateTime zonedStartTime = zonedNow.withHour(22).withMinute(0).withSecond(0).withNano(0);
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
        ZonedDateTime zonedStartTime = zonedNow.withHour(23).withMinute(00).withSecond(0).withNano(0);
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
