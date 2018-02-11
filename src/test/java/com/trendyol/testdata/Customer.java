package com.trendyol.testdata;


public class Customer {

    public void setUser(String user) {
        this.user = user;
    }

    private String password;
    private String user;

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return user;
    }
}
