package io.qpointz.rapids.formats;

import java.io.Closeable;
import java.io.IOException;

public interface DataReader<T> extends Closeable {

    public T read() throws IOException;

}
