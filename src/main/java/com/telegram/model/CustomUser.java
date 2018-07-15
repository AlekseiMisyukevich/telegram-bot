package com.telegram.model;

public class CustomUser {

    private Long chatId;
    private String firstName;
    private String lastName;
    private String username;
    private String instaName;
    private String langCode;

    public CustomUser(Long chatId, String firstName, String lastName, String username, String instaName, String langCode) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.langCode = langCode;
        this.instaName = instaName;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getInstaName() {
        return instaName;
    }

    public void setInstaName(String instaName) {
        this.instaName = instaName;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }
}
