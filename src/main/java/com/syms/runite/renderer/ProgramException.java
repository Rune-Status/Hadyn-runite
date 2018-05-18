package com.syms.runite.renderer;

import static java.lang.String.format;

public final class ProgramException extends RuntimeException {

  public ProgramException(String message, String log) {
    super(format("%s\n%s", message, log));
  }
}
