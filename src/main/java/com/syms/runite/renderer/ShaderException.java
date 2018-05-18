package com.syms.runite.renderer;

import static java.lang.String.format;

public final class ShaderException extends RuntimeException {

  public ShaderException(String message, String log) {
    super(format("%s\n%s", message, log));
  }
}
