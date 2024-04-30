package io.qpointz.delta.lineage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LineageItems<T> {

    private Set<Item<T>> items = new HashSet<>();

    private record Item<T>(T target, int targetIdx, T source, int sourceIdx, List<String> sourceTableName, String sourceAttribute) {

    }

    public void add(T target, int targetIdx, T source, int sourceIdx, List<String> sourceTableName, String sourceAttribute) {
        items.add(new Item<>(target, targetIdx, source, sourceIdx, sourceTableName, sourceAttribute));
    }

}
