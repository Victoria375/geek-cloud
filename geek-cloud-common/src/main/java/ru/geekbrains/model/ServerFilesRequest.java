package ru.geekbrains.model;

import lombok.Getter;

@Getter
public class ServerFilesRequest implements CloudMessage{
    private final String directory;

    public ServerFilesRequest(String directory) {
        this.directory = directory;
    }

    @Override
    public MessageType getType() {
        return MessageType.SERVER_FILES_REQUEST;
    }
}
