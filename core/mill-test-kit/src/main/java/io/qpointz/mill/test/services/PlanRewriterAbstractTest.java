package io.qpointz.mill.test.services;


import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.proto.VectorBlock;
import io.qpointz.mill.data.backend.ExecutionProvider;
import io.qpointz.mill.sql.VectorBlockRecordIterator;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.substrait.plan.Plan;
import lombok.val;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Predicate;

public abstract class PlanRewriterAbstractTest {

    @Autowired
    ExecutionProvider executionProvider;

    protected VectorBlockIterator executePlan(Plan plan) {
        val config = QueryExecutionConfig.newBuilder()
                .setFetchSize(1000)
                .build();
        return executionProvider.execute(plan, config);
    }


    class RecordIterator extends VectorBlockRecordIterator {

        protected RecordIterator(Iterator<VectorBlock> vectorBlocks) {
            super(vectorBlocks);
        }

        @Override
        public void close() {

        }
    }

    protected RecordIterator executePlanAsRecordReader(Plan plan) {
        val iterator = executePlan(plan);
        return new RecordIterator(iterator);
    }

    protected void assertAny(Plan plan, Predicate<RecordIterator> check) {
        val r = assertCheck(plan, check);
        if (!r.contains(true)) {
            throw new AssertionFailedError("Expected some to match. Got none");
        }
    }

    protected void assertNone(Plan plan, Predicate<RecordIterator> check) {
        val r = assertCheck(plan, check);
        if (r.contains(true)) {
            throw new AssertionFailedError("Expected none to match. Got some");
        }
    }

    protected void assertAll(Plan plan, Predicate<RecordIterator> check) {
        val r = assertCheck(plan, check);
        if (r.contains(false)) {
            throw new AssertionFailedError("Expected all to match. Some not");
        }
    }

    private Set<Boolean> assertCheck(Plan plan, Predicate<RecordIterator> check) {
        val res = new HashSet<Boolean>();
        val reader = executePlanAsRecordReader(plan);
        while (reader.hasNext()) {
            res.add(check.test(reader));
        }
        return res;
    }



}
