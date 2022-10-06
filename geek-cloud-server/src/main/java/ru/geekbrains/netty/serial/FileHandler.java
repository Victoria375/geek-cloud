package ru.geekbrains.netty.serial;

import ru.geekbrains.model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path serverDir;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverDir = Path.of("server_files");
        ctx.writeAndFlush(new ListMessage(serverDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        log.debug("Received: {}", cloudMessage.getType());
        if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(serverDir.resolve(fileMessage.getFileName()), fileMessage.getBytes());
            ctx.writeAndFlush(new ListMessage(serverDir));
        } else if (cloudMessage instanceof FileRequest fileRequest) {
                if (true) {
                    switch (fileRequest.getCommand()){
                        case DELETE:
                            Files.delete(Path.of(fileRequest.getFileName()));
                            ctx.writeAndFlush(new ListMessage(serverDir));
                            break;
                        case DOWNLOAD:
                            ctx.writeAndFlush(new FileMessage(serverDir.resolve(fileRequest.getFileName())));
                            break;
                    }
                }
        } else if (cloudMessage instanceof ServerFilesRequest serverFilesRequest) {
            if (new File(serverDir.toString() + File.separator + serverFilesRequest.getDirectory()).isDirectory()) {
                serverDir = serverDir.resolve(serverFilesRequest.getDirectory());
                log.debug("serverDir: {}", serverDir);
                ctx.writeAndFlush(new ListMessage(serverDir));
            }
        }
    }

}
