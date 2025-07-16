package io.qpointz.mill.services.rewriters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.*;

@Builder
@AllArgsConstructor
public class TableFacetsCollection implements Map<List<String>, TableFacet> {

    private final Map<List<String>, TableFacet> facets;

    @Override
    public int size() {
        return this.facets.size();
    }

    @Override
    public boolean isEmpty() {
        return this.facets.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.facets.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.facets.containsValue(value);
    }

    @Override
    public TableFacet get(Object key) {
        return this.facets.get(key);
    }

    @Override
    public TableFacet put(List<String> key, TableFacet value) {
        return this.facets.put(key, value);
    }

    @Override
    public TableFacet remove(Object key) {
        return this.facets.remove(key);
    }

    @Override
    public void putAll(Map<? extends List<String>, ? extends TableFacet> m) {
        this.facets.putAll(m);
    }

    @Override
    public void clear() {
        this.facets.clear();
    }

    @Override
    public Set<List<String>> keySet() {
        return this.facets.keySet();
    }

    @Override
    public Collection<TableFacet> values() {
        return this.facets.values();
    }

    @Override
    public Set<Entry<List<String>, TableFacet>> entrySet() {
        return this.facets.entrySet();
    }
}
