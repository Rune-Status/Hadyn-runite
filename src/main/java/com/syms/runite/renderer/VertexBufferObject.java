package com.syms.runite.renderer;

import static java.lang.String.format;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryStack;

public final class VertexBufferObject {

  private final int id;
  private boolean destroyed;

  public VertexBufferObject() {
    id = glGenBuffers();
  }

  public void bind() {
    glBindBuffer(GL_ARRAY_BUFFER, id);
  }

  public void set(float[] floats, int usage) {
    try (MemoryStack stack = stackPush()) {
      FloatBuffer buffer = stack.mallocFloat(18);
      buffer.put(floats);
      buffer.flip();

      glBindBuffer(GL_ARRAY_BUFFER, id);
      glBufferData(GL_ARRAY_BUFFER, buffer, usage);
    }
  }

  public void set(FloatBuffer buffer, int usage) {
    glBindBuffer(GL_ARRAY_BUFFER, id);
    glBufferData(GL_ARRAY_BUFFER, buffer, usage);
  }

  public void destroy() {
    if (destroyed) {
      throw new IllegalStateException(
          format("Vertex buffer object %d has already been destroyed.", id));
    }
    glDeleteBuffers(id);
    destroyed = true;
  }
}
