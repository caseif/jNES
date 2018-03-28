package net.caseif.jnes.disassembler.util;

import com.google.common.collect.ImmutableList;

import java.util.stream.Collector;

public class CollectionHelper {

    public static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> toImmutableList() {
        return Collector.of(ImmutableList.Builder<T>::new, ImmutableList.Builder::add, (l, r) -> l.addAll(r.build()),
                ImmutableList.Builder::build);
    }

}
