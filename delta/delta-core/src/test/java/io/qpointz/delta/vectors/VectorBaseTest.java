package io.qpointz.delta.vectors;

import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.proto.VectorBlock;
import lombok.val;

public class VectorBaseTest {

    protected VectorBlock testVectorBlock() {
        val strV = Vector.StringVector.newBuilder();
        val strVNulls = Vector.NullsVector.newBuilder();
        val intV = Vector.I32Vector.newBuilder();
        val intVNulls = Vector.NullsVector.newBuilder();
        for (var i=0;i<100;i++) {
            val isNull = i % 2 == 0;
            strV.addValues(String.format("Item %d", i));
            strVNulls.addNulls(isNull);

            intV.addValues(i);
            intVNulls.addNulls(isNull);
        }
        return VectorBlock.newBuilder()
                .addVectors(Vector.newBuilder().setStringVector(strV.build()).setNulls(strVNulls).build())
                .addVectors(Vector.newBuilder().setI32Vector(intV.build()).setNulls(intVNulls).build())
                .build();

    }

}
