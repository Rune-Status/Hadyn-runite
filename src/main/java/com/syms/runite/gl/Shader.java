package com.syms.runite.gl;

import static java.lang.String.format;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glDeleteShader;

public final class Shader {

  private final int id;
  private boolean destroyed;

  Shader(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void destroy() {
    if (destroyed) {
      throw new IllegalStateException(format("Shader %d has already been destroyed.", id));
    }
    glDeleteShader(id);
    destroyed = true;
  }

  public enum Type {
    VERTEX(GL_VERTEX_SHADER),
    FRAGMENT(GL_FRAGMENT_SHADER);

    private final int id;

    Type(int id) {
      this.id = id;
    }

    public int getId() {
      return id;
    }
  }
}
