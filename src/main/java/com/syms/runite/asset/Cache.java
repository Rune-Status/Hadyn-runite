package com.syms.runite.asset;

import com.syms.runite.io.BufferedFile;

public final class Cache {

  public static final int REFERENCE_LENGTH = 6;
  public static final int BLOCK_LENGTH = 520;
  public static final int HEADER_LENGTH = 8;

  private final int type;
  private final BufferedFile data;
  private final BufferedFile index;

  public Cache(int type, BufferedFile data, BufferedFile index) {
    this.type = type;
    this.data = data;
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
    return null;
  }
}
