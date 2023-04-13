package io.qpointz.rapids.filesystem.azure;

import com.azure.core.annotation.Get;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.specialized.BlobInputStream;
import io.qpointz.rapids.formats.SeekableStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

public class SeekableBlobStream extends SeekableStream {

     com.azure.storage.blob.BlobClient blobClient;
     private long pos = -1;

     private BlobInputStream blobStream;

     private void resetStream() {
          this.pos = 0;
     }

     @Override
     public long getPos() throws IOException {
          return this.pos;
     }

     @Override
     public void seek(long newPos) throws IOException {
          this.resetStream();
          this.blobStream().skip(newPos);
          this.pos=newPos;
     }

     private InputStream blobStream() {
          if (this.blobStream==null) {
               this.resetStream();
          }
          return this.blobStream;
     }

     @Override
     public int read() throws IOException {
          int b = this.blobStream().read();
          if (b>=0) {
               this.pos++;
          }
          return b;
     }
}
