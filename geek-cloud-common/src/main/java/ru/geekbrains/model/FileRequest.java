package ru.geekbrains.model;

import lombok.Getter;

@Getter
public class FileRequest implements CloudMessage {

    public enum Command {
        DELETE,
        DOWNLOAD,
        RENAME
    }

    private final String fileName;
    private Command command;

    public FileRequest(String fileName) {
        this.fileName = fileName;
    }

    public Command getCommand() {
        return command;
    }

    public FileRequest(Command command, String fileName) {
        this.command = command;
        this.fileName = fileName;
    }

    @Override
    public MessageType getType() {
        return MessageType.FILE_REQUEST;
    }
}