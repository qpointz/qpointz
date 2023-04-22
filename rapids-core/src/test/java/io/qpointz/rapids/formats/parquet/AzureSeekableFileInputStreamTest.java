package io.qpointz.rapids.formats.parquet;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.datalake.DataLakeServiceClientBuilder;
import org.apache.parquet.io.SeekableInputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AzureSeekableFileInputStreamTest {

    private static String refFileName = "hallo.bin";
    private static Path refFilePath = Paths.get(".tmp", refFileName).toAbsolutePath();

    @BeforeAll
    public static void prepareInputFile() throws IOException {
        // prepare local file
        var dirPath = Paths.get(".tmp").toAbsolutePath();
        if (!Files.exists(dirPath)) {
            Files.createDirectory(dirPath);
        }

        if (Files.exists(refFilePath)) {
            Files.delete(refFilePath);
        }
        var filePath = Files.createFile(refFilePath);
        var fos = new FileOutputStream(filePath.toFile(), false);
        var r = new Random(Instant.now().toEpochMilli());
        var bytes = new byte[2048];
        for (var i=0;i<10000;i++) {
            r.nextBytes(bytes);
            fos.write(bytes);
            fos.flush();
        }
        fos.close();

        //ingest
        var storageAccountName = System.getenv("RAPIDS_IT_AZURE_STORAGE_ACCOUNT_NAME");
        var storageAccountKey = System.getenv("RAPIDS_IT_AZURE_STORAGE_ACCOUNT_KEY");
        var fileSystem = "rapids-it";
        var credentials = new StorageSharedKeyCredential(storageAccountName, storageAccountKey);
        var endpoint = "https://%s.dfs.core.windows.net".formatted(storageAccountName);
        var dlsClient = new DataLakeServiceClientBuilder()
                .endpoint(endpoint)
                .credential(credentials)
                .buildClient();
        var fsclient = dlsClient.getFileSystemClient(fileSystem);
        if (!fsclient.exists()) {
            fsclient.create();
        }
        var dirclient = fsclient.getDirectoryClient("rapids/azure/stream");
        if (dirclient.exists()) {
            dirclient.deleteWithResponse(true, null, null, null);
        }
        dirclient.create();
        System.out.println("Directory created");
        var fileClient = dirclient.createFile(refFileName);
        File file = refFilePath.toFile();
        long fileLength = file.length();
        try (var refFileStream = new BufferedInputStream(new FileInputStream(file))) {
            fileClient.append(refFileStream, 0, fileLength);
            fileClient.flush(fileLength,true);
        }
        System.out.println("hallo");
  }

    SeekableInputStream stream1() throws FileNotFoundException {
        return new RapidsSeekableFileInputStream(new FileInputStream(refFilePath.toFile()));
    }

    SeekableInputStream stream2() throws FileNotFoundException {
        return new RapidsSeekableFileInputStream(new FileInputStream(refFilePath.toFile()));
    }

    @Test
    public void seekForward() throws IOException {
        Function<SeekableInputStream, byte[]> fn = s -> {
            try {
                s.seek(1024L);
                var b = new byte[100];
                s.read(b);
                return b;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        var s1 = stream1();
        var bf1 = fn.apply(s1);
        var s2 = stream2();
        var bf2 = fn.apply(s2);
        assertTrue(Arrays.equals(bf1,bf2));
        assertEquals(s1.getPos(), s2.getPos());
    }

    @Test
    public void seekBackward() throws IOException {
        Function<SeekableInputStream, byte[]> fn = s -> {
            try {
                var b = new byte[1024];
                for (var i=0;i<10;i++) {
                    s.read(b);
                }
                s.seek(2048L);
                s.read(b);
                return b;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        var s1 = stream1();
        var bf1 = fn.apply(s1);
        var s2 = stream2();
        var bf2 = fn.apply(s2);
        assertTrue(Arrays.equals(bf1,bf2));
        assertEquals(s1.getPos(), s2.getPos());
    }

    @Test
    void readStart() throws FileNotFoundException {
        Function<SeekableInputStream, byte[]> fn = s -> {
            try {
                var b = new byte[100];
                s.read(b);
                return b;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        };
        var bf1 = fn.apply(stream1());
        var bf2 = fn.apply(stream2());
        assertTrue(Arrays.equals(bf1,bf2));
    }
}