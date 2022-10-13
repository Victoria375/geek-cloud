package ru.geekbrains.model;

public class AuthRequest implements CloudMessage{

    private boolean authorization;
    private String login;
    private String password;

    public AuthRequest(boolean authorization) {
        this.authorization = authorization;
    }

    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public boolean isAuthorization() {
        return authorization;
    }

    public void setAuthorization(boolean authorization) {
        this.authorization = authorization;
    }

    @Override
    public MessageType getType() {
        return MessageType.AUTH_REQUEST;
    }
}
