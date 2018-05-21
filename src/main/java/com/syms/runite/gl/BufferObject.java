package com.syms.runite.gl;

import static java.lang.String.format;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryStack;

public abstract class BufferObject {

  private final Renderer renderer;
  private final int id;
  private final int target;
  private final Usage usage;
  private boolean destroyed;

  BufferObject(Renderer renderer, int target, Usage usage) {
    id = glGenBuffers();
    this.renderer = renderer;
    this.target = target;
    this.usage = usage;
  }

  protected Renderer getRenderer() {
    return renderer;
  }

  public int getId() {
    return id;
  }

  public int getTarget() {
    return target;
  }

  public abstract void bind();

  public void setFloatv(float[] array) {
    try (MemoryStack stack = stackPush()) {
      FloatBuffer buffer = stack.mallocFloat(array.length);
      buffer.put(array);
      buffer.flip();

      setFloatv(buffer);
    }
  }

  public void setFloatv(FloatBuffer buffer) {
    bind();
    glBufferData(target, buffer, usage.getValue());
  }

  public void destroy() {
    if (destroyed) {
      throw new IllegalStateException(
          format("Buffer object %d has already been destroyed.", id));
    }
    glDeleteBuffers(id);
    destroyed = true;
  }

  public enum Usage {
    STATIC_DRAW(GL_STATIC_DRAW);

    private final int value;

    Usage(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }
}
