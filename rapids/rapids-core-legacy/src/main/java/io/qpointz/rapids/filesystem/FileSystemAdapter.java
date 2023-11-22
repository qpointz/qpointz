package io.qpointz.rapids.filesystem;

import java.io.Closeable;
import java.nio.file.FileSystem;
import java.nio.file.Path;

public interface FileSystemAdapter extends Closeable {

    FileSystem getFileSytem();

    Path getTraverseRoot();

}
