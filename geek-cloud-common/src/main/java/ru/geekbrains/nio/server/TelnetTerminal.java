package ru.geekbrains.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class TelnetTerminal {
    /**
     * Support commands:
     * cd path - go to dir
     * touch filename - create file with filename
     * mkdir dirname - create directory with dirname
     * cat filename - show filename bytes
     */

    private Path current;
    private ServerSocketChannel server;
    private Selector selector;

    private ByteBuffer buf;

    public TelnetTerminal() throws IOException {
        current = Path.of("geek-cloud-common");
        buf = ByteBuffer.allocate(256);
        server = ServerSocketChannel.open();
        selector = Selector.open();
        server.bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = keys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isAcceptable()) {
                    handleAccept();
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                keyIterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        buf.clear();
        StringBuilder sb = new StringBuilder();
        while (true) {
            int read = channel.read(buf);
            if (read == 0) {
                break;
            }
            if (read == -1) {
                channel.close();
                return;
            }
            buf.flip();
            while (buf.hasRemaining()) {
                sb.append((char) buf.get());
            }
            buf.clear();
        }
        System.out.println("Received: " + sb);
        String command = sb.toString().trim();
        if (command.equals("ls")) {
            String files = Files.list(current)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.joining("\n\r"));
            channel.write(ByteBuffer.wrap(files.getBytes(StandardCharsets.UTF_8)));
        } else if (command.startsWith("cd")) {
            String[] comm = command.split(" +");
            if (comm.length == 2) {
                String directory = comm[1];
                Path path = current.resolve(directory);
                if (Files.exists(path)) {
                    current = path;
                    String message = "change directory...\n\r ";
                    channel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                } else {
                    String message = "no directory with that name: " + directory + "\n\r";
                    channel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                }
            } else {
                String message = "add directory name\n\r ";
                channel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
            }
        } else if (command.startsWith("touch")) {
            String[] comm = command.split(" +");
            if (comm.length == 2) {
                String fileName = comm[1];
                Path file = Paths.get(current + "/" + fileName);
                Files.createFile(file);
            } else {
                String message = "add file name\n\r ";
                channel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
            }
        } else if (command.startsWith("mkdir")) {
            String[] comm = command.split(" +");
            if (comm.length == 2) {
                String dirName = comm[1];
                Path path = Paths.get(current + "/" + dirName);
                Files.createDirectory(path);
            } else {
                String message = "add directory name\n\r ";
                channel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
            }
        }

        else if (command.startsWith("cat")) {
            String[] comm = command.split(" +");
            if (comm.length == 2) {
                String fileName = comm[1];
                Path path = current.resolve(fileName);
                if (Files.exists(path)) {
                    System.out.println(Files.readString(path));
                } else {
                    String message = "no file with that name: " + fileName + "\n\r";
                    channel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                }
            } else {
                String message = "add file name\n\r ";
                channel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
            }
        } else {
            byte[] bytes = command.getBytes(StandardCharsets.UTF_8);
            channel.write(ByteBuffer.wrap(bytes));
        }
    }


    private void handleAccept() throws IOException {
        SocketChannel socketChannel = server.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted");
    }

    public static void main(String[] args) throws IOException {
        new TelnetTerminal();
    }
}
