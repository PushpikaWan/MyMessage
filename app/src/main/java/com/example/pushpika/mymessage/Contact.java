package com.example.pushpika.mymessage;

public class Contact {
    private String email;
    private String name;
    private String id;

    public Contact(String email, String name, String id) {
        this.email = email;
        this.name = name;
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return id;
    }

    public void setID(String ID) {
        this.id = id;
    }
}
