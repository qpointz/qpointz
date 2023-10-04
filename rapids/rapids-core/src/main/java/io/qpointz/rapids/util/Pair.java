package io.qpointz.rapids.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
public class Pair<A, B> {
    @Getter
    private final A left;

    @Getter
    private final B right;

    public static <A,B>Pair<A,B> of(A left, B right ) {
        return new Pair<>(left, right);
    }
}
