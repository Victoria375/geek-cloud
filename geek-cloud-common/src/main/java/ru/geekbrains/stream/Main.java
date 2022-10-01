package ru.geekbrains.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Main {

    static int foo(int x, int y) {
        return x + y;
    }

    static int sum(int x, int y, Func func) {
        return func.apply(x, y);
    }

    static void printString(Consumer<String> consumer, String value) {
        consumer.accept(value);
    }

    public static void main(String[] args) {
        Func sum = (a, b) -> a + b;
        System.out.println(sum.apply(1, 2));
        Func sumRef = Integer::sum;
        Calc calc = Main::sum;
        System.out.println(calc.calc(1, 2, sum));

        // action
        Consumer<String> printer = System.out::println;
        printer.accept("Hello world");
        Consumer<Integer> consumer = value -> System.out.println(value);

        // for filtering
        Predicate<Integer> isOdd = value -> value % 2 == 1;
        System.out.println(isOdd.test(2));

        // for transform
        Function<String, Integer> toInt = Integer::parseInt;
        System.out.println(toInt.apply("1234"));

        // getter
        Supplier<List<String>> emptyList = ArrayList::new;
        emptyList.get();
        Supplier<Integer> value = () -> 0;
    }
}
