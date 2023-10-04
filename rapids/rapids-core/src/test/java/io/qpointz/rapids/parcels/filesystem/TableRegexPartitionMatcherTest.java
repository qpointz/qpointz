package io.qpointz.rapids.parcels.filesystem;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TableRegexPartitionMatcherTest {

    List<URI> uri = Stream.of(
            "/drive/root/dataset1/datataset-2015-10.csv",
            "/drive/root/dataset1/datataset-2015-11.csv",
            "/drive/root/dataset1/datataset-2015-12.csv",
            "/drive/root/dataset2/datataset-2015-10.csv",
            "/drive/root/dataset3/datataset-2015-11.csv",
            "/drive/root/dataset2/datataset-2015-12.csv",
        ""
    ).map(URI::create).toList();


    private TableRegexPartitionMatcher defMatcher = TableRegexPartitionMatcher.builder()
            .regexPattern("/drive/root/(?<dataset>[^/]+)\\/.+(?<year>\\d{4})-(?<month>\\d{2})\\.csv")
            .datasetGroup("dataset")
            .partitionValueGroups(Map.of("year", PartitionValueType.INT, "month", PartitionValueType.STRING))
            .build();

    @Test
    void uriAndStringMustEquals() {
        var m1 = defMatcher.match("/drive/root/dataset1/datataset-2015-10.csv").get();
        var m2 = defMatcher.match(URI.create("/drive/root/dataset1/datataset-2015-10.csv")).get();
        assertEquals(m1.getTableName(), m2.getTableName());
        assertEquals(m1.getPartitionMap(), m2.getPartitionMap());
    }

    @Test
    void returnsEmptyWhenNotMatching() {
        var m1 = defMatcher.match("lala");
        assertEquals(Optional.empty(), m1);
    }

    @Test
    void match() {
        var m = defMatcher.match("/drive/root/dataset1/datataset-2015-10.csv").get();
        assertEquals("dataset1", m.getTableName());
        assertEquals(2015, m.getPartitionMap().get("year").getInt());
        assertEquals("10", m.getPartitionMap().get("month").getString());
    }

    @Test
    void throwsOnWrongFormat() {
        var m = TableRegexPartitionMatcher.builder()
                .regexPattern("/drive/root/(?<dataset>[^/]+)\\/datataset-(?<year>.{4})-(?<month>\\d{2}).csv$")
                .datasetGroup("dataset")
                .partitionValueGroups(Map.of("year", PartitionValueType.INT, "month", PartitionValueType.STRING))
                .build();
        assertThrows(NumberFormatException.class, ()-> m.match("/drive/root/dataset1/datataset-nann-11.csv"));
    }

}