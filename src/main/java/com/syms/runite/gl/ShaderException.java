package com.syms.runite.gl;

import static java.lang.String.format;

public final class ShaderException extends RuntimeException {

  public ShaderException(String message, Throwable reason) {
    super(message, reason);
  }

  public ShaderException(String message, String log) {
    super(format("%s\n%s", message, log));
  }
}
