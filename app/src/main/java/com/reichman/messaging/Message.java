package com.reichman.messaging;

import java.io.Serializable;
import java.util.Date;

// Message Model Class
public class Message implements Serializable {

    // Define Variables For Model
    private String TextInMessage;
    private String UserInMessage;
    private String UserPhotoUrl;
    private long TimeOfMessage;

    // Message Constructor
    public Message(String TextInMessage, String UserInMessage, String UserPhotoUrl) {
        // Assign Constructor Values
        this.TextInMessage = TextInMessage;
        this.UserInMessage = UserInMessage;
        this.UserPhotoUrl = UserPhotoUrl;

        // Get Time Of Message
        this.TimeOfMessage = new Date().getTime();
    }

    // Empty Message Constructor For Firebase
    public Message() {

    }


    // Get Message
    public String getMessage() {
        return TextInMessage;
    }

    // Set Message
    public void setMessage(String messageText) {
        this.TextInMessage = messageText;
    }

    // Get User
    public String getUser() {
        return UserInMessage;
    }

    // Set User
    public void setUser(String messageUser) {
        this.UserInMessage = messageUser;
    }

    // Get Photo
    public String getPhoto() {
        return UserPhotoUrl;
    }

    // Set Photo
    public void setPhoto(String userPhoto) {
        this.UserPhotoUrl = userPhoto;
    }

    // Get Time
    public long getTime() {
        return TimeOfMessage;
    }

    // Set Time
    public void setTime(long messageTime) {
        this.TimeOfMessage = messageTime;
    }
}
