package io.qpointz.mill.services.rewriters;

import com.google.protobuf.util.JsonFormat;
import io.qpointz.mill.services.SqlProvider;
import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
import io.substrait.plan.ProtoPlanConverter;
import io.substrait.proto.Plan;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootTest(classes = {RewriterBaseTest.class, DefaultServiceConfiguration.class})
@ComponentScan("io.qpointz")
@ActiveProfiles("test-cmart")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RewriterBaseTest {

    @Autowired
    @Getter
    ResourceLoader resourceLoader;

    @Autowired
    @Getter
    SqlProvider sqlProvider;

    protected Plan loadProtoPlan(String location) throws IOException {
        val builder = Plan.newBuilder();
        val inputStream = resourceLoader
                .getResource(location)
                .getInputStream();
        val reader = new InputStreamReader(inputStream);
        JsonFormat.parser().merge(reader, builder);
        return builder.build();
    }

    protected io.substrait.plan.Plan loadPlan(String location) throws IOException {
        val protoPlan = loadProtoPlan(location);
        val converter = new io.substrait.plan.ProtoPlanConverter();
        return converter.from(protoPlan);
    }

    protected io.substrait.plan.Plan loadTestPlan(String name) throws IOException {
        val planBuilder = Plan.newBuilder();
        try (val reader = new InputStreamReader(new FileInputStream("../test/plans/" + name + ".json")) ) {
            JsonFormat.parser().merge(reader, planBuilder);
            ProtoPlanConverter converter = new ProtoPlanConverter();
            return converter.from(planBuilder.build());
        }
    }

}
