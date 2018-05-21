package com.syms.runite.gl;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;

public final class VertexBufferObject extends BufferObject {

  public VertexBufferObject(Renderer renderer, Usage usage) {
    super(renderer, GL_ARRAY_BUFFER, usage);
  }

  @Override
  public void bind() {
    getRenderer().bind(this);
  }
}