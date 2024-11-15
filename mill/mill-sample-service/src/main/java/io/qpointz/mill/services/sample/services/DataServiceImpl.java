package io.qpointz.mill.services.sample.services;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.dispatchers.PlanDispatcher;
import io.qpointz.mill.sql.RecordReader;
import io.qpointz.mill.sql.RecordReaders;
import io.qpointz.mill.sql.VectorBlockRecordIterator;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {

    @Autowired
    ExecutionProvider executionProvider;

    @Autowired
    PlanDispatcher planDispatcher;

    public List<Object> listOfVals(String schemaName, String tableName) {
        val planHelper = planDispatcher.plan();
        val builder = planHelper.substraitBuilder();

        //create table scan
        val namedScan = planHelper.createNamedScan(schemaName, tableName);

        //reference first column
        val fieldRef = builder.fieldReference(namedScan, 0);

        //project only one field
        val project = builder.project(r -> List.of(fieldRef), namedScan);

        //select top 10 elements of projection
        val top = builder.limit(10, project);

        //convert to plan
        val plan = planHelper.createPlan(top);

        //execute and read into list
        val iter = executionProvider.execute(plan, QueryExecutionConfig.newBuilder().setFetchSize(1000).build());

        val reader = RecordReaders.recordReader(iter);


        val res = new ArrayList<Object>();
        while (reader.next()) {
            res.add(reader.getObject(0));
        }
        return res;
    }

}
