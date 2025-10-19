package org.eloquence.eloquenceauth;

public class AuthTicket {
    public String name;
    public String key = null;
    public boolean complete = false;
    public boolean isAuthenticated = false;

    public AuthTicket(String name) {
        this.name = name;
    }
}
