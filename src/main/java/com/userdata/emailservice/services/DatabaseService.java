package com.userdata.emailservice.services;

import com.userdata.emailservice.models.UserData;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    
    private String getConnectionString() {
        // Get connection string from environment variable
        String connectionString = System.getenv("SQL_CONNECTION_STRING");
        
        if (connectionString == null || connectionString.trim().isEmpty()) {
            throw new RuntimeException("SQL_CONNECTION_STRING environment variable is not set");
        }
        
        return connectionString;
    }

    public List<UserData> getAllUserData() {
        List<UserData> userDataList = new ArrayList<>();
        String query = "SELECT Id, Name, Age, CreatedAt FROM UserData ORDER BY CreatedAt DESC";

        try (Connection conn = DriverManager.getConnection(getConnectionString());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                UserData userData = new UserData();
                userData.setId(rs.getInt("Id"));
                userData.setName(rs.getString("Name"));
                userData.setAge(rs.getInt("Age"));
                
                // Handle CreatedAt timestamp
                Timestamp timestamp = rs.getTimestamp("CreatedAt");
                if (timestamp != null) {
                    userData.setCreatedAt(timestamp.toLocalDateTime());
                }
                
                // Email is not stored in database
                userData.setEmail("");

                userDataList.add(userData);
            }

            System.out.println("Successfully retrieved " + userDataList.size() + " records from database");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve user data from database", e);
        }

        return userDataList;
    }

    public boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(getConnectionString())) {
            return conn.isValid(5); // 5 second timeout
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
