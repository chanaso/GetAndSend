package com.example.getsend;

public class User {
    private String name, phone, password;
    private int rate;

    public User(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() { return phone; }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPass() {
        return password;
    }

    public void setPass(String pass) {
        this.password = pass;
    }

    public void setRate(int rate){ this.rate = rate; }
    public int getRate(){ return rate; }

    public User(String name, String phone, String pass, int rate) {
        this.name = name;
        this.phone = phone;
        this.password = pass;
        this.rate = rate;
    }
}
