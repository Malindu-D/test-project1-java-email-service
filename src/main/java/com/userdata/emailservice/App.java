package com.userdata.emailservice;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.google.gson.Gson;
import com.userdata.emailservice.models.*;
import com.userdata.emailservice.services.*;

public class App {
    
    private static final Gson gson = new Gson();
    private static DatabaseService databaseService;
    private static EmailService emailService;

    public static void main(String[] args) {
        // Get port from environment variable (Azure App Service uses PORT)
        int port = getPort();
        
        // Initialize services
        databaseService = new DatabaseService();
        emailService = new EmailService();

        // Create Javalin app
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            // Enable CORS for all origins (adjust in production)
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });
        }).start(port);

        System.out.println("Java Email Service started on port: " + port);

        // Health check endpoint
        app.get("/api/health", ctx -> {
            ctx.json(new ApiResponse(true, "Email service is healthy"));
        });

        // Send email endpoint
        app.post("/api/email/send", App::handleSendEmail);

        // Handle 404
        app.error(404, ctx -> {
            ctx.json(new ApiResponse(false, "Endpoint not found"));
        });

        // Handle 500
        app.exception(Exception.class, (e, ctx) -> {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).json(new ApiResponse(false, "Internal server error: " + e.getMessage()));
        });
    }

    private static void handleSendEmail(Context ctx) {
        try {
            // Parse request body
            EmailRequest request = gson.fromJson(ctx.body(), EmailRequest.class);
            
            // Validate receiver email
            if (request == null || request.getReceiverEmail() == null || request.getReceiverEmail().trim().isEmpty()) {
                ctx.status(400).json(new ApiResponse(false, "Receiver email is required"));
                return;
            }

            String receiverEmail = request.getReceiverEmail().trim();
            
            // Validate email format
            if (!isValidEmail(receiverEmail)) {
                ctx.status(400).json(new ApiResponse(false, "Invalid email format"));
                return;
            }

            System.out.println("Processing email request for: " + receiverEmail);

            // Get all user data from database
            var userData = databaseService.getAllUserData();
            
            if (userData.isEmpty()) {
                ctx.status(404).json(new ApiResponse(false, "No user data found in database"));
                return;
            }

            System.out.println("Retrieved " + userData.size() + " records from database");

            // Create HTML email with table
            String emailHtml = EmailTemplateBuilder.createEmailHtml(userData);

            // Send email via Azure Communication Service
            boolean sent = emailService.sendEmail(receiverEmail, "User Data Report", emailHtml);

            if (sent) {
                ctx.json(new ApiResponse(true, "Email sent successfully to " + receiverEmail));
            } else {
                ctx.status(500).json(new ApiResponse(false, "Failed to send email"));
            }

        } catch (Exception e) {
            System.err.println("Error in handleSendEmail: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).json(new ApiResponse(false, "Error sending email: " + e.getMessage()));
        }
    }

    private static boolean isValidEmail(String email) {
        // Basic email validation
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private static int getPort() {
        // Azure App Service sets PORT environment variable
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                return Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT environment variable: " + portEnv);
            }
        }
        // Default port for local development
        return 8080;
    }
}
