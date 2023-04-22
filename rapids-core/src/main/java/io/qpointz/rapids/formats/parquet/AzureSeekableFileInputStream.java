package io.qpointz.rapids.formats.parquet;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.datalake.*;
import com.azure.storage.file.datalake.models.DataLakeFileOpenInputStreamResult;
import org.apache.parquet.io.SeekableInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class AzureSeekableFileInputStream extends SeekableInputStream {

    private final String storageAccount;
    private final String storageAccountKey;
    private final String fileSystem;
    private final String directory;
    private final String fileName;
    private final DataLakeServiceClient dataLakeClient;
    private final DataLakeFileSystemClient fileSystemClient;
    private final DataLakeDirectoryClient directoryClient;
    private final DataLakeFileClient fileClient;
    private final long pos;
    private DataLakeFileOpenInputStreamResult fileInputStream;
    private InputStream inputStream;

    public AzureSeekableFileInputStream(String storageAccount,
                                        String storageAccountKey,
                                        String fileSystem,
                                        String directory,
                                        String fileName) {
        this.storageAccount = storageAccount;
        this.storageAccountKey = storageAccountKey;
        this.fileSystem = fileSystem;
        this.directory = directory;
        this.fileName = fileName;

        var endpoint = "https://%s.dfs.core.windows.net".formatted(storageAccount);

        var credentials = new StorageSharedKeyCredential(storageAccount, storageAccountKey);

        this.dataLakeClient = new DataLakeServiceClientBuilder()
                .endpoint(endpoint)
                .credential(credentials)
                .buildClient();

        this.fileSystemClient = this.dataLakeClient.getFileSystemClient(this.fileSystem);

        this.directoryClient = this.fileSystemClient.getDirectoryClient(this.directory);

        this.fileClient = this.directoryClient.getFileClient(this.fileName);


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
        this.fileInputStream = this.fileClient.openInputStream();
        this.inputStream = this.fileInputStream.getInputStream();
    }

    @Override
    public long getPos() throws IOException {
        return this.pos;
    }

    @Override
    public void seek(long newPos) throws IOException {

    }

    @Override
    public void readFully(byte[] bytes) throws IOException {
        this.resetStream();
        bytes = this.inputStream.readAllBytes();
    }

    @Override
    public void readFully(byte[] bytes, int start, int len) throws IOException {
        this.resetStream();
        this.inputStream.read(bytes, start, len);
    }

    @Override
    public int read(ByteBuffer buf) throws IOException {
        var remaining = buf.remaining();
        return this.inputStream.read(buf.array(), 0, remaining);
    }

    @Override
    public void readFully(ByteBuffer buf) throws IOException {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public int read() throws IOException {
        return this.inputStream.read();
    }
}
