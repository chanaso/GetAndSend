package com.example.getsend;

public class Package {
    private String size, location, destination;
    private double weight;

    public Package(){

    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getLocation() { return location; }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setWeight(double weight){ this.weight = weight; }

    public double getWeight(){ return weight; }

    public Package(String size, String location, String destination, double weight) {
        this.size = size;
        this.location = location;
        this.destination = destination;
        this.weight = weight;
    }
}
