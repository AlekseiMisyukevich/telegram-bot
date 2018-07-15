package com.telegram.dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.telegram.model.CustomUser;
import org.bson.Document;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

public class UserRepo {

    private final String HOST = "127.0.0.1";
    private final String PORT = "27021";

    private static final Logger LOGGER = Logger.getLogger(UserRepo.class.getName());

    private LinkedHashMap<Long, CustomUser> users;

    public UserRepo () {
        this.users = new LinkedHashMap<Long, CustomUser>();
    }

    public void check(String firstName, String lastName, long userID, long chatID, String username) {
        MongoClientURI connectionString = new MongoClientURI("mongodb://" + HOST + ":" + PORT);
        MongoClient mongoClient = new MongoClient(connectionString);
        MongoDatabase database = mongoClient.getDatabase("botDB");
        MongoCollection<Document> collection = database.getCollection("users");
        long found = collection.count(Document.parse("{id : " + Long.toString(userID) + "}"));
        if (found == 0) {
            Document doc = new Document("firstname", firstName)
                    .append("lastname", lastName)
                    .append("id", userID)
                    .append("chat_id", chatID)
                    .append("username", username);
            collection.insertOne(doc);
            mongoClient.close();
            LOGGER.info("CustomUser " + username + " added");
        }
    }

    public void add(Long id, CustomUser user) {
        users.put(id, user);
    }

    public CustomUser getById (Long id) {
        return users.get(id);
    }

    public boolean contains(Long id) {
        return users.containsKey(id);
    }

}
