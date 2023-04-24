package io.qpointz.rapids.azure;

import com.azure.storage.blob.nio.AzureFileSystem;
import com.azure.storage.common.StorageSharedKeyCredential;
import io.qpointz.rapids.filesystem.FileSystemAdapter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;

public class AzureFileSystemAdapter implements FileSystemAdapter {

    private final FileSystem azureFileSystem;
    private final Path traverseRoot;

    public AzureFileSystemAdapter(FileSystem azureFileSystem, Path traverseRoot) {
        this.azureFileSystem = azureFileSystem;
        this.traverseRoot = traverseRoot;
    }

    @Override
    public FileSystem getFileSytem() {
        return this.azureFileSystem;
    }

    @Override
    public Path getTraverseRoot() {
        return this.traverseRoot;
    }

    public static AzureFileSystemAdapter create(String accountName, String accountKey, String containerName, String traverseRoot) throws IOException {
        var props = new HashMap<String,Object>();
        var credential = new StorageSharedKeyCredential(accountName, accountKey);
        props.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL, credential);
        props.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, containerName);
        var fileSystemURI = URI.create("azb://?endpoint=https://%s.blob.core.windows.net".formatted(accountName));

        FileSystem fs = null;
        try {
            fs = FileSystems.getFileSystem(fileSystemURI);
        }
        catch (FileSystemNotFoundException ex) {
            fs = FileSystems.newFileSystem(fileSystemURI, props);
        }
        var traverseRootPath = fs.getPath(traverseRoot);
        return new AzureFileSystemAdapter(fs, traverseRootPath);
    }

    @Override
    public void close() throws IOException {
        this.azureFileSystem.close();
    }
}
