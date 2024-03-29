package io.qpointz.rapids.azure;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.datalake.*;
import com.azure.storage.file.datalake.options.DataLakeFileInputStreamOptions;
import org.apache.parquet.io.SeekableInputStream;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class AzureDataLakeSeekableInputStream extends SeekableInputStream {

    private final DataLakeFileClient fileClient;
    private long pos;
    private InputStream inputStream;

    public static AzureDataLakeSeekableInputStream create(String storageAccount,
                                                          String storageAccountKey,
                                                          String fileSystem,
                                                          String directory,
                                                          String fileName) {
        var endpoint = "https://%s.dfs.core.windows.net".formatted(storageAccount);

        var credentials = new StorageSharedKeyCredential(storageAccount, storageAccountKey);

        var dataLakeClient = new DataLakeServiceClientBuilder()
                .endpoint(endpoint)
                .credential(credentials)
                .buildClient();

        var fileSystemClient = dataLakeClient.getFileSystemClient(fileSystem);

        var directoryClient = fileSystemClient.getDirectoryClient(directory);

        var fileClient = directoryClient.getFileClient(fileName);

        return new AzureDataLakeSeekableInputStream(fileClient);
    }

    public AzureDataLakeSeekableInputStream(DataLakeFileClient fileClient) {
        this.fileClient = fileClient;

        try {
            this.resetStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.pos = 0;
    }

    private void resetStream() throws IOException {
        if (this.inputStream!=null) {
            this.inputStream.close();
        }
        var opts = new DataLakeFileInputStreamOptions();

        this.inputStream = this.fileClient
                .openInputStream(opts)
                .getInputStream();
    }

    @Override
    public long getPos() throws IOException {
        return this.pos;
    }

    @Override
    public void seek(long newPos) throws IOException {
        this.resetStream();
        this.pos = newPos;
        this.inputStream.skip(newPos);
    }

    @Override
    public void readFully(byte[] bytes) throws IOException {
        this.readFully(bytes, 0, bytes.length);
    }

    @Override
    public void readFully(byte[] bytes, int start, int len) throws IOException {
        var read = this.inputStream.read(bytes, start, len);
        this.pos+=read;
    }

    @Override
    public int read(ByteBuffer buf) throws IOException {
        var remaining = buf.remaining();
        var read = this.inputStream.read(buf.array(), 0, remaining);
        this.pos += read;
        return read;
    }

    @Override
    public void readFully(ByteBuffer buf) throws IOException {
        var read = read(buf);
        if (read==-1) {
            throw new EOFException();
        }
    }

    @Override
    public int read() throws IOException {
        this.pos++;
        return this.inputStream.read();
    }
}
