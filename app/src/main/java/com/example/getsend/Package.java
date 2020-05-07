package com.example.getsend;

// Package status options:
// Waiting for delivery
// Waiting for approval
// On the way...
// Arrived -only the owner approve the delivery
// Arrived :) -both owner and deliveryman approve the delivery

public class Package {
    private String size, location, destination, geoLocation, geoDestination, packageOwnerId, deliveryman, status, packageId;
    private double weight;

    public Package(){
        this.deliveryman = "";
        this.status = "Waiting for delivery";
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
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

    public String getPackageOwnerId() {
        return packageOwnerId;
    }

    public void setPackageOwnerId(String packageOwnerId) {
        this.packageOwnerId = packageOwnerId;
    }

    public String getDeliveryman() {
        return deliveryman;
    }

    public void setDeliveryman(String deliveryman) {
        this.deliveryman = deliveryman;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Package(String size, String location, String geoLocation, String geoDestination,
                   String destination, double weight, String packageOwnerId, String packageId) {
        this.size = size;
        this.location = location;
        this.geoLocation = geoLocation;
        this.geoDestination = geoDestination;
        this.destination = destination;
        this.weight = weight;
        this.packageOwnerId = packageOwnerId;
        this.packageId = packageId;
        this.deliveryman = "";
        this.status = "Waiting for delivery";
    }
}
