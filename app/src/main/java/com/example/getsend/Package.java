package com.example.getsend;

public class Package {
    private String size, location, destination, geoLocation, geoDestination;
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

    public String getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

    public String getGeoDestination() {
        return geoDestination;
    }

    public void setGeoDestination(String geoDestination) {
        this.geoDestination = geoDestination;
    }

    public Package(String size, String location, String geoLocation, String geoDestination, String destination, double weight) {
        this.size = size;
        this.location = location;
        this.geoLocation = geoLocation;
        this.geoDestination = geoDestination;
        this.destination = destination;
        this.weight = weight;
    }
}
