package com.userdata.emailservice.services;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

import java.time.Duration;

public class EmailService {
    
    private final EmailClient emailClient;
    private final String senderAddress;

    public EmailService() {
        // Get connection string and sender address from environment variables
        String connectionString = System.getenv("COMMUNICATION_SERVICE_CONNECTION_STRING");
        this.senderAddress = System.getenv("SENDER_EMAIL_ADDRESS");

        if (connectionString == null || connectionString.trim().isEmpty()) {
            throw new RuntimeException("COMMUNICATION_SERVICE_CONNECTION_STRING environment variable is not set");
        }

        if (senderAddress == null || senderAddress.trim().isEmpty()) {
            throw new RuntimeException("SENDER_EMAIL_ADDRESS environment variable is not set");
        }

        // Create email client
        this.emailClient = new EmailClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        System.out.println("Email service initialized with sender: " + senderAddress);
    }

    public boolean sendEmail(String receiverEmail, String subject, String htmlBody) {
        try {
            System.out.println("Sending email to: " + receiverEmail);
            System.out.println("Subject: " + subject);

            // Create email message
            EmailMessage emailMessage = new EmailMessage()
                    .setSenderAddress(senderAddress)
                    .setToRecipients(receiverEmail)
                    .setSubject(subject)
                    .setBodyHtml(htmlBody);

            // Send email and get poller
            SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(emailMessage);

            // Wait for completion (with timeout)
            PollResponse<EmailSendResult> response = poller.waitForCompletion(Duration.ofMinutes(2));

            if (response != null && response.getValue() != null) {
                EmailSendResult result = response.getValue();
                EmailSendStatus status = result.getStatus();

                System.out.println("Email send status: " + status);
                System.out.println("Message ID: " + result.getId());

                // Check if email was sent successfully
                if (status == EmailSendStatus.SUCCEEDED) {
                    System.out.println("Email sent successfully to: " + receiverEmail);
                    return true;
                } else {
                    System.err.println("Email sending failed with status: " + status);
                    if (result.getError() != null) {
                        System.err.println("Error: " + result.getError().getMessage());
                    }
                    return false;
                }
            } else {
                System.err.println("No response received from email service");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
