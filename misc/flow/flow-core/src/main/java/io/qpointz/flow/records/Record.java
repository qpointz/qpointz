package io.qpointz.flow.records;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Record {
    List<RecordAttribute> attributes();
    Object[] values();
    Collection<RecordMetaEntry> metas();
}
