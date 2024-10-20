package io.qpointz.mill;

import io.qpointz.mill.proto.*;
import io.qpointz.mill.types.logical.*;
import io.qpointz.mill.vectors.VectorProducer;
import lombok.Getter;
import lombok.val;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

public class TestVectorData {

    public static class LogicalTypeVectorData<T> {

        @Getter
        private final VectorProducer<T> vectorProducer;

        @Getter
        private final String name;

        private final ArrayList<T> values = new ArrayList<>();
        private final ArrayList<Boolean> nulls = new ArrayList<>();
        private final Function<T, String> toSupport;

        @Getter
        private final LogicalType<T, ?> logicalType;

        private final int getLength() {
            return values.size();
        }


        public LogicalTypeVectorData(String name, LogicalType<T, ?> logicalType, Function<T,String> sr, T[] initial) {

            this.vectorProducer = logicalType.getVectorProducer();
            this.logicalType = logicalType;
            this.name = name;
            this.toSupport = sr;
            for (T i: initial) {
                this.append(i, i==null);
            }
        }

        public LogicalTypeVectorData<T> appendNotNull(T value) {
            return this.append(value, false);
        }

        public LogicalTypeVectorData<T> appendNull() {
            return this.append(null, true);
        }

        public LogicalTypeVectorData<T> append(T value, Boolean isNull) {
            this.values.add(value);
            this.nulls.add(isNull);
            this.vectorProducer.append(value, isNull);
            return this;
        }

        public void writeSupport(StringBuilder builder) {
            for (var idx=0;idx<values.size();idx++) {
                val isNull = this.nulls.get(idx);
                val r = String.format("%s|%d|%s|%s|%s\n",
                        this.name,
                        idx,
                        this.logicalType.getLogicalTypeId().toString(),
                        this.nulls.get(idx).toString().toUpperCase(),
                        isNull ? "<NULL>" : this.toSupport.apply(values.get(idx)));
                builder.append(r);
            }
        }

    }

    public static void GenerateRoundTripData(List<LogicalTypeVectorData<?>> contexts, String suitePath) throws IOException {
        val sc = VectorBlockSchema.newBuilder();
        val vb = VectorBlock.newBuilder();
        val sb = new StringBuilder();
        var vectorSize = 0;

        val maxSize = contexts.stream().map(k -> k.getLength()).reduce(Integer::max).get();

        for (var idx=0;idx<contexts.size();idx++) {
            val ctx = contexts.get(idx);

            while(ctx.getLength()<maxSize) {
                ctx.appendNull();
            }

            val vector = ctx.vectorProducer.vectorBuilder().build();
            vb.addVectors(vector);

            val logicalDataType = LogicalDataType.newBuilder()
                    .setTypeId(ctx.logicalType.getLogicalTypeId())
                    .setPrecision(-1)
                    .setScale(-1)
                    .build();

            val dataType = DataType.newBuilder()
                    .setNullability(DataType.Nullability.NULL)
                    .setType(logicalDataType)
                    .build();

            val field = Field.newBuilder()
                    .setName(ctx.getName())
                    .setFieldIdx(idx)
                    .setType(dataType);
            sc.addFields(field.build());
            ctx.writeSupport(sb);
            vectorSize = Math.max(vectorSize, ctx.values.size());
        }

        val vectorBlock = vb.setVectorSize(vectorSize)
                .setSchema(sc.build())
                .build();

        try (val fos = new FileOutputStream(suitePath + ".bin")) {
            fos.write(vectorBlock.toByteArray());
        }
        try (val fos = new PrintWriter(suitePath + ".ref")) {
            fos.write(sb.toString());
        }
        try (val fos = new PrintWriter(suitePath + ".msg")) {
            fos.write(vectorBlock.toString());
        }

        System.out.println(vectorBlock.toString());
        System.out.println(vectorBlock.toByteString().toString());
        System.out.println(sb.toString());
    }

    public static String bytesToString(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes) + "|base64";
    }

    public static void main(String[] args) throws IOException {
        val ctx = List.<LogicalTypeVectorData<?>>of(
                //Big Int
                new LogicalTypeVectorData<>("BigInt", BigIntLogical.INSTANCE, Object::toString,
                        new Long[]{1L, null, Long.MIN_VALUE, Long.MAX_VALUE}),

                //Binary
                new LogicalTypeVectorData<>("Binary", BinaryLogical.INSTANCE, TestVectorData::bytesToString  ,
                        new byte[][]{
                                new byte[] {100, 12, -100, 12, (byte)234, (byte)-234},
                                null,
                                new byte[] {},
                                Base64.getDecoder().decode("3wWJVd0GE1CELMW6eiBFa2irYE3cLZIU0Z2Kx+yNctvVQaFcbPLhJXVGgzHP7Y/3zWbmkeMUAOvi" +
                                        "CTSJp6DPYjwEHt4cXihT8KNZs2sdr9Kqs0ntAdnSW8H3So39nm7Bf5e+qjODSWErbZZB1cMgNP4b" +
                                        "k8lt+dUwjOePQPBGAnb88WNmv8mQurqSq//TPjfE4SS7UuetCByRc6ZitY0dp7W/r+UtzrIwMpc3" +
                                        "xsa9LYikmjYyk8Xjr92eLAnKu4lhA9udQuhwWzpC9ZLKUw/6Dcjswp1Ftbv2ZQX8MaYVK/D1p7ts" +
                                        "AowdK1xrdB4J7zv1roSj1ZlTcIwn/x8nxKM7MTK3s7pd9wkvR0IrnE+A3h73822nEibfvpTI80Op" +
                                        "tdl7H//ALPBbqQfRe/1dvZcsC1O1x9FaCtQA6N5qokoIt4iRP9rVxnz2QvLXjeA6gLCnvzARYSvF" +
                                        "eN5qF7R9e7jrN8NKvV8C0LJbZs/9DhPiSj36s+fNUsyvsOtKFIbq4bRBQamn/oEKtO/FYwIkyC4D" +
                                        "ptAVCM0kJtFxCODZlYAK5OCzBu5VRrq5pVcfF6D973TX9/ETUEdcPzx4UfCbhqlA0IgwMgELmmxQ" +
                                        "4PKWsZaspSgR3K53Pcvnt1O7oxeAvfTEz6PnQlqBpaoi4Gg3bqh8Oj6XZWL6ThhTUOB4rJV19M3c" +
                                        "RSZpMDwHCa9towWkk/FGui8MENEEBGicXS0c15GrnGD9S85wIvnfpiCfrjU8oxqXYH6S3lUiFOVR" +
                                        "RjFSSyyp/kVVb0BnzMbxEbLnP05cjbbt2yLrK1QEdCekb1qbUhlKQwER3ms6zU8aoRoNA7ZfIOvi" +
                                        "mpK0F/DSDUaHKeqKdOLjxYIYSzgu5XJN8v9oEir0uRP8j0AOkDaz4jZW1Gf4DBeowt8NTHURMlj7" +
                                        "uvvYqdTJ2rc2ZW0+9EyVYBbyj3yXJJPN9zuKG0711I6Dv8AUYmv72JfWwpuzqRiLajvdqFTpJH3p" +
                                        "TIe+fukQ3I/EiDK/Xk83PeP5vfuQrs3L4dfcXvnpKfBqJw0RbuT3E8nd+6GYgEcbCpG+JjQYSvqU" +
                                        "WyvF9S2fyfy9+9JMwGM3cooe3FfG8lrDQ8b8wjbS9R/nAIEPkaFqcmsAWzVob/IZCNaIPkWEdUYV" +
                                        "I3MbrFuWnKfHKb8wX0PfDcJtL0O/RZVcsNwGhLaVJR589um5ZsI66rFi4RMbdzJBBg4k7D/ouhGd" +
                                        "Brdg7CQWsThI0mzjIp4GfQqfZqyMmpkYj/emWcTfMJtwaf350UJoIKvR8dfhN35mCwq74UNbSuwz" +
                                        "Ret74nBS1nzNrc31xOUUD0WN7b5Vl+HyY1kwUxAmFiaLMZyzOqyX+XdFGv+1L5BlKKHMSoflPg=="),
                        }),

                //Boolean
                new LogicalTypeVectorData<>("Boolean", BoolLogical.INSTANCE, Object::toString,
                        new Boolean[] {
                            true,
                            null,
                            false,
                            null
                        }),

                //DATE
                new LogicalTypeVectorData<>("Date", DateLogical.INSTANCE, k -> DateLogical.fromPhysical(k).toString(),
                    new Long[]{
                            DateLogical.toPhysical(LocalDate.EPOCH),
                            null,
                            DateLogical.MIN_DAYS,
                            DateLogical.MAX_DAYS
                    }),

                //Double
                new LogicalTypeVectorData<>("Double", DoubleLogical.INSTANCE, Object::toString,
                        new Double[] {-23423423.234D, Double.MIN_VALUE, Double.MAX_VALUE, null}),

                //Float
                new LogicalTypeVectorData<>("Float", FloatLogical.INSTANCE, k -> String.format("%.27e",k),
                        new Float[] {-3.4e38f, 3.4e38f, -23423.234F, null}),

                //INTERVAL_DAY
                // new Context<>("IntervalDay", DateLogical.INSTANCE, Object::toString,

                //INTERVAL_YEAR
                // new Context<>("IntervalDay", DateLogical.INSTANCE, Object::toString,

                //Int
                new LogicalTypeVectorData<>("Int", IntLogical.INSTANCE, Object::toString,
                        new Integer[] {23, Integer.MIN_VALUE, Integer.MAX_VALUE, null }),

                //SmallInt
                new LogicalTypeVectorData<>("SmallInt", SmallIntLogical.INSTANCE, Object::toString,
                    new Integer[] {null, Integer.MIN_VALUE, Integer.MAX_VALUE, -34234}),

                //String
                new LogicalTypeVectorData<>("String", StringLogical.INSTANCE, k->k,
                        new String[] {"Hello world", null, "", "NULL", "StrangeString"}),

                //Time
                new LogicalTypeVectorData<>("Time", TimeLogical.INSTANCE, k-> TimeLogical.fromPhysical(k).format( DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSSS")),
                        new Long[]{
                                TimeLogical.toPhysical(LocalTime.of(13,10,8,238049834)),
                                null,
                                TimeLogical.MIN,
                                TimeLogical.MAX
                        }),

                //Timestamp
                new LogicalTypeVectorData<>("Timestamp", TimestampLogical.INSTANCE, k-> TimestampLogical.fromPhysical(k).format( DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS")),
                    new Long[] {
                            TimestampLogical.toPhysical(LocalDateTime.of(2012, 10, 3, 14, 12, 45, 345334533)),
                            null,
                            TimestampLogical.MIN,
                            TimestampLogical.MAX,
                    }),
                //TimestampTZ
                new LogicalTypeVectorData<>("TimestampTZ", TimestampTZLogical.INSTANCE, k-> TimestampTZLogical.fromPhysical(k).format( DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS")),
                        new Long[] {
                                TimestampTZLogical.toPhysical(ZonedDateTime.of(2012, 10, 3, 14, 12, 45, 345334533, ZoneId.of("CET"))),
                                null,
                                TimestampTZLogical.MIN,
                                TimestampTZLogical.MAX,
                        }),
                //TinyInt
                new LogicalTypeVectorData<>("TinyInt", TinyIntLogical.INSTANCE, Object::toString,
                        new Integer[] {(int)Short.MIN_VALUE, (int)Short.MAX_VALUE, 234})
                        .appendNull(),

                //UUID
                new LogicalTypeVectorData<>("GUId", UUIDLogical.INSTANCE, k-> UUIDLogical.fromPhysical(k).toString(),
                        new byte[][]{
                            UUIDLogical.toPhysical(UUID.randomUUID()),
                            null,
                            UUIDLogical.toPhysical(UUID.randomUUID()),
                            UUIDLogical.toPhysical(UUID.randomUUID())
                        })
        );
        TestVectorData.GenerateRoundTripData(ctx, "test/messages/logical-types");
    }




}
