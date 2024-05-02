package io.qpointz.delta.lineage;

import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

public class LineageItems<T> {

    private Set<Item<T>> items = new HashSet<>();

    private Map<Pair<T>, TableAttribute> attributes = new HashMap<>();

    public void add(T target, int targetIdx, T source, Set<Integer> sourceIdxes) {
        for (val idx : sourceIdxes) {
            this.add(target, targetIdx, source, idx);
        }
    }

    public record Pair<T>(T owner, int idx) {
        public static <T> Pair<T> of(T owner, int idx) {
            return new Pair<>(owner,idx);
        }
    }

    public record TableAttribute(List<String> table, String attribute){
    }

    public record Item<T>(Pair<T> target, Pair<T> source) {
    }

    private record Attribute<T>(Pair<T> owner, TableAttribute attribute) {
    }



    public Set<Pair<T>> sourcesOf(Pair<T> target) {
        val directs = this.items.stream()
                .filter(k-> k.target.equals(target))
                .map(k-> k.source)
                .collect(Collectors.toSet());
        val res = new HashSet<Pair<T>>();
        for (val k : directs) {
            res.add(k);
            res.addAll(sourcesOf(k));
        }
        return res;
    }

    public Set<TableAttribute> attributesOf(T target, int targetIdx) {
        return attributesOf(Pair.of(target, targetIdx));
    }

    public Set<TableAttribute> attributesOf(Pair<T> target) {
        return sourcesOf(target).stream()
                .filter(k-> this.attributes.containsKey(k))
                .map(k-> this.attributes.get(k))
                .collect(Collectors.toSet());
    }

    public void add(T target, int targetIdx, T source, int sourceIdx) {
        items.add(new Item<>(Pair.of(target,targetIdx), Pair.of(source,sourceIdx)));
    }

    public void addAttributes(T source, int sourceIndex, List<String> sourceTableName, String sourceAttribute) {
        attributes.put(Pair.of(source, sourceIndex),
                new TableAttribute(sourceTableName, sourceAttribute));
    }

}
