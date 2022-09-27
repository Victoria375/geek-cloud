package ru.geekbrains.nio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

public class ChannelExamples {

    public static void main(String[] args) {
        // ByteBuffer read
        // ByteBuffer write
        ByteBuffer buf = ByteBuffer.allocate(15);

        buf.put("Hello world".getBytes());
        buf.flip(); // set limit
        StringBuilder sb = new StringBuilder();
        while (buf.hasRemaining()) {
            byte b = buf.get();
            sb.append((char) b);
        }
        System.out.println(sb);
        buf.rewind(); // read without mutate
        buf.mark(); // установить метку
        buf.reset(); // откатывает на метку

        // Channel read / write
        buf.clear();
        sb = new StringBuilder();
        try (RandomAccessFile raf = new RandomAccessFile("geek-cloud-common/1.txt", "rw")) {
            FileChannel channel = raf.getChannel();
            channel.write(ByteBuffer.wrap("New message".getBytes()), channel.size());
            // 800
            while (true) {
                int read = channel.read(buf);
                if (read == -1) {
                    break;
                }
                buf.flip();
                while (buf.hasRemaining()) {
                    sb.append((char) buf.get());
                };
                buf.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(sb);
    }

}
