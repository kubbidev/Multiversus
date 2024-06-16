package me.kubbidev.multiversus.command.tabcomplete;

import me.kubbidev.multiversus.util.Predicates;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface CompletionSupplier {

    CompletionSupplier EMPTY = partial -> Collections.emptyList();

    static CompletionSupplier startsWith(String... strings) {
        return startsWith(() -> Arrays.stream(strings));
    }

    static CompletionSupplier startsWith(Collection<String> strings) {
        return startsWith(strings::stream);
    }

    static CompletionSupplier startsWith(Supplier<Stream<String>> stringsSupplier) {
        return partial -> stringsSupplier.get().filter(Predicates.startsWithIgnoreCase(partial)).collect(Collectors.toList());
    }

    static CompletionSupplier contains(String... strings) {
        return contains(() -> Arrays.stream(strings));
    }

    static CompletionSupplier contains(Collection<String> strings) {
        return contains(strings::stream);
    }

    static CompletionSupplier contains(Supplier<Stream<String>> stringsSupplier) {
        return partial -> stringsSupplier.get().filter(Predicates.containsIgnoreCase(partial)).collect(Collectors.toList());
    }

    List<String> supplyCompletions(String partial);

}