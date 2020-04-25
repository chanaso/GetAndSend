package com.example.getsend;

public class User {
    private String name, phone, pass, packages;
    private int rate, type;
    //type: 0- deliverman, 1- get delivery.

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
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setRate(int rate){ this.rate = rate; }
    public int getRate(){ return rate; }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public User(String name, String phone, String pass) {
        this.name = name;
        this.phone = phone;
        this.pass = pass;
        this.rate = 0;
        this.type = -1;
        this.packages = "";
    }
}
