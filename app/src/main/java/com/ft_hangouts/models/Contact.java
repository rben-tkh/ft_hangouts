package com.ft_hangouts.models;

public class Contact {
    private int id;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private String note;
    private String photoPath;
    private boolean isBlocked;
    private boolean isDeleted;

    public Contact() {
        this.isBlocked = false;
        this.isDeleted = false;
    }

    public Contact(int id, String name, String phoneNumber, String email, String address, String note, String photoPath) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.note = note;
        this.photoPath = photoPath;
        this.isBlocked = false;
        this.isDeleted = false;
    }

    public Contact(String name, String phoneNumber, String email, String address, String note, String photoPath) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.note = note;
        this.photoPath = photoPath;
        this.isBlocked = false;
        this.isDeleted = false;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}