package com.syms.runite.io;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public final class FileOnDisk {

  private RandomAccessFile file;
  private final long maximumLength;
  private long position;

  public FileOnDisk(File file, String access) throws IOException {
    this(file, access, -1L);
  }

  public FileOnDisk(File file, String access, long maximumLength) throws IOException {
    if (maximumLength == -1L) {
      maximumLength = Long.MAX_VALUE;
    }

    if (file.length() >= maximumLength) {
      file.delete();
    }

    this.file = new RandomAccessFile(file, access);
    this.maximumLength = maximumLength;
    this.position = 0L;

    int read = this.file.read();
    if (read != -1 && !access.equals("r")) {
      this.file.seek(0L);
      this.file.write(read);
    }
    this.file.seek(0L);
  }

  public void seek(long position) throws IOException {
    file.seek(position);
    this.position = position;
  }

  public int read(byte[] dest, int off, int len) throws IOException {
    int read = file.read(dest, off, len);
    if (read > 0) {
      this.position += (long) read;
    }
    return read;
  }

  public void write(byte[] src, int off, int len) throws IOException {
    if (position + (long) len > maximumLength) {
      this.file.seek(maximumLength + 1L);
      this.file.write(1);
      throw new EOFException();
    }

    this.file.write(src, off, len);
    this.position += (long) len;
  }

  public long length() throws IOException {
    return file.length();
  }

  public void close() throws IOException {
    if(file == null) {
      return;
    }

    file.close();
    file = null;
  }
}
