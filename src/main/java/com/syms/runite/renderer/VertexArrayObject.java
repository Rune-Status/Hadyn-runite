package com.syms.runite.renderer;

import static java.lang.String.format;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public final class VertexArrayObject {

  private final int id;
  private boolean destroyed;

  public VertexArrayObject() {
    id = glGenVertexArrays();
  }

  public void bind() {
    glBindVertexArray(id);
  }

  public void destroy() {
    if (destroyed) {
      throw new IllegalStateException(format("Vertex array %d has already been destroyed.", id));
    }
    glDeleteVertexArrays(id);
    destroyed = true;
  }
}
