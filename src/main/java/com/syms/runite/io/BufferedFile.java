package com.syms.runite.io;

import java.io.EOFException;
import java.io.IOException;

public final class BufferedFile {

  FileOnDisk file;

  long position;
  long filePosition;

  byte[] writeBuffer;
  byte[] readBuffer;
  long readPosition = -1L;
  long writePosition = -1L;
  int readOffset;
  int writeOffset;
  long readLength;
  long writeLength;

  public BufferedFile(FileOnDisk file, int readLength, int writeLength) throws IOException {
    this.file = file;
    this.writeLength = this.readLength = file.length();
    this.readBuffer = new byte[readLength];
    this.writeBuffer = new byte[writeLength];
  }

  public void seek(long position) throws IOException {
    if (position < 0L) {
      throw new IOException("Invalid seek.");
    }
    this.position = position;
  }

  public void read(byte[] dest) throws IOException {
    read(dest, 0, dest.length);
  }

  public void read(byte[] dest, int off, int len) throws IOException {
    try {
      if (len + off > dest.length) {
        throw new ArrayIndexOutOfBoundsException(len + off - dest.length);
      }

      // Check if the write buffer contains the section of bytes we are looking to read. If it
      // does, copy the section of bytes from the buffer.
      if (writePosition != -1L
          && position >= writePosition
          && writePosition + (long) writeOffset >= position + (long) len) {
        System.arraycopy(writeBuffer, (int) (position - writePosition), dest, off, len);
        position += (long) len;
        return;
      }

      long pos = position;

      int length = len;

      if (position >= readPosition && position < readPosition + (long) readOffset) {
        int read = (int) ((long) readOffset - (position - readPosition));
        if (read > len) {
          read = len;
        }

        System.arraycopy(readBuffer, (int) (position - readPosition), dest, off, read);
        position += (long) read;
        off += read;
        len -= read;
      }

      if (len > readBuffer.length) {
        file.seek(position);

        int read;
        for (filePosition = position; len > 0; len -= read) {
          read = file.read(dest, off, len);
          if (read == -1) {
            break;
          }

          filePosition += (long) read;
          position += (long) read;
          off += read;
        }
      } else if (len > 0) {
        fill();

        int read = len;
        if (len > readOffset) {
          read = readOffset;
        }

        System.arraycopy(readBuffer, 0, dest, off, read);
        off += read;
        len -= read;
        position += (long) read;
      }

      if (writePosition != -1L) {
        if (writePosition > position && len > 0) {
          int read = off + (int) (this.writePosition - this.position);
          if (read > len + off) {
            read = len + off;
          }

          while (off < read) {
            dest[off++] = 0;
            --len;
            ++position;
          }
        }

        long start = -1L;
        long end = -1L;
        if (writePosition >= pos && writePosition < pos + (long) length) {
          start = writePosition;
        } else if (pos >= writePosition && pos < writePosition + (long) writeOffset) {
          start = pos;
        }

        if (writePosition + (long) writeOffset > pos
            && writePosition + (long) writeOffset <= pos + (long) length) {
          end = (long) writeOffset + writePosition;
        } else if (writePosition < pos + (long) length
            && pos + (long) length <= writePosition + (long) writeOffset) {
          end = pos + (long) length;
        }

        if (start > -1L && end > start) {
          int copyLength = (int) (end - start);
          System.arraycopy(writeBuffer,
                           (int) (start - writePosition),
                           dest,
                           (int) (start - pos) + off, copyLength);
          if (end > position) {
            len = (int) ((long) len - (end - position));
            position = end;
          }
        }
      }
    } catch (IOException exception) {
      this.filePosition = -1L;
      throw exception;
    }

    if (len > 0) {
      throw new EOFException();
    }
  }

  public void write(byte[] src, int off, int len) throws IOException {
    try {
      if ((long) len + this.position > this.writeLength) {
        this.writeLength = this.position + (long) len;
      }

      // Flush the bytes to the file that are currently buffered. This will synchronize the
      // current write position with the position we are currently writing at.
      if (writePosition != -1L
          && (position < writePosition ||
          position > writePosition + (long) writeOffset)) {
        flush();
      }

      if (writePosition != -1L
          && position + (long) len > writePosition + (long) writeBuffer.length) {
        int length = (int) ((long) writeBuffer.length - (position - writePosition));
        System.arraycopy(src, off, writeBuffer, (int) (position - writePosition), length);
        position += (long) length;
        off += length;
        len -= length;
        writeOffset = writeBuffer.length;
        flush();
      }

      if (len > writeBuffer.length) {
        if (position != filePosition) {
          file.seek(position);
          filePosition = position;
        }

        file.write(src, off, len);
        filePosition += (long) len;

        if (filePosition > readLength) {
          readLength = filePosition;
        }

        long start = -1L;
        long end = -1L;

        if (position >= readPosition && position < readPosition + (long) readOffset) {
          start = position;
        } else if (readPosition >= position && readPosition < position + (long) len) {
          start = readPosition;
        }

        if (position + (long) len > readPosition
            && position + (long) len <= readPosition + (long) readOffset) {
          end = position + (long) len;
        } else if (readPosition + (long) readOffset > position
            && readPosition + (long) readOffset <= position + (long) len) {
          end = readPosition + (long) readOffset;
        }

        if (start > -1L && end > start) {
          int copyLength = (int) (end - start);
          System.arraycopy(src,
                           (int) (start + (long) off - position),
                           readBuffer,
                           (int) (start - readPosition),
                           copyLength);
        }

        position += (long) len;
      } else {
        if (len <= 0) {
          return;
        }

        if (writePosition == -1L) {
          writePosition = position;
        }

        System.arraycopy(src, off, writeBuffer, (int) (position - writePosition), len);
        position += (long) len;
        if (position - writePosition > (long) writeOffset) {
          writeOffset = (int) (position - writePosition);
        }
      }
    } catch (IOException exception) {
      this.filePosition = -1L;
      throw exception;
    }
  }

  public long length() {
    return writeLength;
  }

  public void close() throws IOException {
    flush();
    file.close();
  }

  /**
   * Fills the read buffer.
   */
  private void fill() throws IOException {
    readOffset = 0;
    if (filePosition != position) {
      file.seek(position);
      filePosition = position;
    }

    int read;
    for (readPosition = position; readOffset < readBuffer.length; readOffset += read) {
      read = file.read(readBuffer, readOffset, readBuffer.length - readOffset);
      if (read == -1) {
        break;
      }
      filePosition += (long) read;
    }
  }

  private void flush() throws IOException {
    if (writePosition == -1L) {
      return;
    }

    if (writePosition != filePosition) {
      file.seek(writePosition);
      filePosition = writePosition;
    }

    file.write(writeBuffer, 0, writeOffset);
    filePosition += (long) writeOffset;
    if (filePosition > readLength) {
      readLength = filePosition;
    }

    long start = -1L;
    long end = -1L;
    if (writePosition >= readPosition && writePosition < (long) readOffset + readPosition) {
      start = writePosition;
    } else if (readPosition >= writePosition && readPosition < (long) writeOffset + writePosition) {
      start = readPosition;
    }

    if (writePosition + (long) writeOffset > readPosition
        && (long) writeOffset + writePosition <= readPosition + (long) readOffset) {
      end = writePosition + (long) writeOffset;
    } else if ((long) readOffset + readPosition > writePosition
        && (long) readOffset + readPosition
        <= (long) writeOffset + writePosition) {
      end = readPosition + (long) readOffset;
    }

    if (start > -1L && end > start) {
      int copyLength = (int) (end - start);
      System.arraycopy(writeBuffer, (int) (start - writePosition), readBuffer,
                       (int) (start - readPosition), copyLength);
    }

    writePosition = -1L;
    writeOffset = 0;
  }
}

