package com.syms.runite.renderer;

import static java.lang.String.format;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public final class VertexArrayObject {

  private final Renderer renderer;
  private final int id;
  private boolean destroyed;

  public VertexArrayObject(Renderer renderer) {
    this.renderer = renderer;
    id = glGenVertexArrays();
  }

  public void bind() {
    renderer.bindVertexArray(id);
  }

  public void destroy() {
    if (destroyed) {
      throw new IllegalStateException(format("Vertex array %d has already been destroyed.", id));
    }
    glDeleteVertexArrays(id);
    destroyed = true;
  }
}
