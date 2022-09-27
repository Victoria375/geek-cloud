package ru.geekbrains.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class PathExamples {

    public static void main(String[] args) throws IOException {
        // 1. read file bytes
        // 2. write bytes to file
        // 3. move in / out of directory (resolve, getParent)
        Path dir = Path.of("geek-cloud-common", "..", "geek-cloud-common", "..", "geek-cloud-common").normalize();
        System.out.println(dir);
        System.out.println(Files.size(dir.resolve("1.txt")));
        System.out.println(dir.toAbsolutePath());

        //1.
        Path file1 = dir.resolve("1.txt");
        String string = Files.readString(file1);
        System.out.println(string);
        // InputStream stream = Files.newInputStream(file1);
        Files.copy(
                dir.resolve("forest.jpg"),
                dir.resolve("copy.jpg"),
                StandardCopyOption.REPLACE_EXISTING // если файл уже существует, то он перезаписывается
        );

        //2.
        Files.writeString(file1, "I'm Mike!", StandardOpenOption.APPEND); // append добавляет запись, иначе текс перезапишется
    }

}
