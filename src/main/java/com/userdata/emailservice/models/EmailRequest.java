package com.userdata.emailservice.models;

public class EmailRequest {
    private String receiverEmail;

    public EmailRequest() {
    }

    public EmailRequest(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    @Override
    public String toString() {
        return "EmailRequest{" +
                "receiverEmail='" + receiverEmail + '\'' +
                '}';
    }
}
