package com.example.getsend;

public class User {
    public String name, phone, password;

    public User(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPass() {
        return password;
    }

    public void setPass(String pass) {
        this.password = pass;
    }

    public User(String name, String phone, String pass) {
        this.name = name;
        this.phone = phone;
        this.password = pass;
    }
}
