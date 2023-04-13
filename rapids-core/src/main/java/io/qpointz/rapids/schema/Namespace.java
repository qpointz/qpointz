package io.qpointz.rapids.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
public class Namespace {

    @Builder.Default()
    @Getter
    private final List<String> parent = null;

    @Getter
    private final String name;

    public static Namespace RootNs = new Namespace(null, "root");


    public Namespace createChild(String childName) {
        var parents = new ArrayList<String>();
        if (this.parent!=null) {
            parents.addAll(this.parent);
        }
        parents.add(this.name);
        return new Namespace(parents, childName);
    }
}
