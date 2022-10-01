package ru.geekbrains.stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextProcessor {

    public static void printWordsInLowerCase() throws IOException {
        Files.lines(Path.of("server_files", "123.txt"))
                .flatMap(line -> Stream.of(line.split(" +")))
                .map(String::toLowerCase)
                .map(word -> word.replaceAll("[^A-Za-z]+", ""))
                .filter(StringUtils::isNotBlank)
                .forEach(System.out::println);
    }

    public static Map<String, Integer> getWordsStats() throws IOException {
        return Files.lines(Path.of("server_files", "123.txt"))
                .flatMap(line -> Stream.of(line.split(" +")))
                .map(String::toLowerCase)
                .map(word -> word.replaceAll("[^A-Za-z]+", ""))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toMap(
                        Function.identity(),
                        value -> 1,
                        Integer::sum
                ));
    }

    public static Pair<List<String>, List<String>> splitWordsByLang() throws IOException {

        Map<Boolean, List<String>> byLangMap = Files.lines(Path.of("server_files", "hello.txt"))
                .flatMap(line -> Stream.of(line.split(" +")))
                .map(String::toLowerCase)
                .collect(Collectors.partitioningBy(s -> s.matches("[a-z]+"), Collectors.toList()));

        return Pair.of(byLangMap.get(true), byLangMap.get(false));
    }

    public static Map<String, List<String>> splitByClasses() throws IOException  {
        return Files.lines(Path.of("server_files", "hello1.txt"))
                .flatMap(line -> Stream.of(line.split(" +")))
                .map(String::toLowerCase)
                .collect(Collectors.groupingBy(word -> {
                    if (word.matches("[a-z]+")) {
                        return "en";
                    } else if (word.matches("[0-9]+")) {
                        return "digit";
                    } else {
                        return "ru";
                    }
                }));
    }

    public static List<Pair<String, Integer>> getWordsStatsOrdered() throws IOException {
        return Files.lines(Path.of("server_files", "123.txt"))
                .flatMap(line -> Stream.of(line.split(" +")))
                .map(String::toLowerCase)
                .map(word -> word.replaceAll("[^A-Za-z]+", ""))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toMap(
                        Function.identity(),
                        value -> 1,
                        Integer::sum
                ))
                .entrySet()
                .stream()
                .sorted(Comparator.comparingInt(e -> -e.getValue()))
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .toList();
    }

    public static void main(String[] args) throws IOException {
        System.out.println(splitByClasses());
    }

}
