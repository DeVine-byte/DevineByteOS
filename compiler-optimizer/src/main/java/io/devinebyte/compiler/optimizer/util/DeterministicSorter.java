package io.devinebyte.compiler.optimizer.util;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class DeterministicSorter {
    private DeterministicSorter() {}

    public static <T> List<T> sortByName(Stream<T> stream, java.util.function.Function<T, String> nameExtractor) {
        return stream.sorted(Comparator.comparing(nameExtractor)).toList();
    }
}
