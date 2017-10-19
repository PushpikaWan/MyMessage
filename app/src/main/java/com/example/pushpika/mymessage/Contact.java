package com.example.pushpika.mymessage;

public class Contact {
    private String email;
    private String fname;
    private String lname;
    private String id;

    public Contact(String email, String fname, String lname, String id) {
        this.email = email;
        this.fname = fname;
        this.lname = lname;
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getID() {
        return id;
    }

    public void setID(String ID) {
        this.id = id;
    }
}
