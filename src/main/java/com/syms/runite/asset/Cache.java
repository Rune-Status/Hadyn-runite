package com.syms.runite.asset;

import static java.lang.Math.min;

import com.syms.runite.io.BufferedFile;
import java.io.IOException;

public final class Cache {

  public static final int REFERENCE_LENGTH = 6;
  public static final int BLOCK_LENGTH = 520;
  public static final int HEADER_LENGTH = 8;

  public static final int BLOCK_EOE = 0;

  private final int type;
  private final BufferedFile blocks;
  private final BufferedFile index;
  private final byte[] buffer = new byte[BLOCK_LENGTH];

  public Cache(int type, BufferedFile blocks, BufferedFile index) {
    this.type = type;
    this.blocks = blocks;
    this.index = index;
  }

  public void put(int id, byte[] src, int len) {
    if (!put(id, src, len, false)) {
      put(id, src, len, true);
    }
  }

  private boolean put(int id, byte[] src, int len, boolean overwrite) {
    return false;
  }

  public byte[] get(int id) {
    try {
      if (index.length() + REFERENCE_LENGTH < (long) (id * REFERENCE_LENGTH)) {
        return null;
      }

      index.seek(id * REFERENCE_LENGTH);
      index.read(buffer, 0, REFERENCE_LENGTH);

      int size = (buffer[0] & 0xff << 16) | (buffer[1] & 0xff << 8) | buffer[2] & 0xff;
      int block = (buffer[3] & 0xff << 16) | (buffer[4] & 0xff << 8) | buffer[5] & 0xff;

      if (size <= 0) {
        return null;
      }

      if (block <= BLOCK_EOE || block > blocks.length() / BLOCK_LENGTH) {
        return null;
      }

      byte[] bytes = new byte[size];
      int part = 0;

      for (int offset = 0; offset < size; part++) {
        // Premature end of entry.
        if (block == BLOCK_EOE) {
          return null;
        }

        int len = min(BLOCK_LENGTH - HEADER_LENGTH, size - offset);
        
        blocks.seek(block * BLOCK_LENGTH);
        blocks.read(buffer, 0, len + HEADER_LENGTH);

        int blockEntryId = (buffer[1] & 0xff) + ((buffer[0] & 0xff) << 8);
        int blockPart = (buffer[3] & 0xff) + ((buffer[2] & 0xff) << 8);
        int nextBlock = ((buffer[5] & 0xff) << 8) + ((buffer[4] & 0xff) << 16) + (buffer[6] & 0xff);
        int blockType = buffer[7] & 0xff;

        if (blockEntryId != id || part != blockPart || blockType != type) {
          return null;
        }

        if ((long) nextBlock > this.blocks.length() / BLOCK_LENGTH) {
          return null;
        }

        System.arraycopy(buffer, HEADER_LENGTH, bytes, 0, len);

        block = nextBlock;
        offset += len;
        part++;
      }

      return bytes;
    } catch (IOException exception) {
      return null;
    }
  }
}
