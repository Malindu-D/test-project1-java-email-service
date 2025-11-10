package com.userdata.emailservice.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserData {
    private int id;
    private String name;
    private int age;
    private LocalDateTime createdAt;
    private String email;

    public UserData() {
    }

    public UserData(int id, String name, int age, LocalDateTime createdAt, String email) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.createdAt = createdAt;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return createdAt.format(formatter);
    }

    @Override
    public String toString() {
        return "UserData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", createdAt=" + createdAt +
                ", email='" + email + '\'' +
                '}';
    }
}
