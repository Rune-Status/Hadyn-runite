package com.syms.runite.renderer;

import static org.lwjgl.opengl.GL20.glUniform3fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.system.MemoryStack.stackPush;

import com.syms.runite.Matrix4f;
import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryStack;

public final class Uniform {

  private final int location;

  Uniform(int location) {
    this.location = location;
  }

  public void setMatrix4f(Matrix4f matrix) {
    try (MemoryStack stack = stackPush()) {
      FloatBuffer buffer = stack.mallocFloat(16);
      matrix.put(buffer);
      buffer.flip();

      glUniformMatrix4fv(location, false, buffer);
    }
  }

  public void setVector3f(float v0, float v1, float v2) {
    try (MemoryStack stack = stackPush()) {
      FloatBuffer buffer = stack.mallocFloat(3);
      buffer.put(v0);
      buffer.put(v1);
      buffer.put(v2);
      buffer.flip();

      glUniform3fv(location, buffer);
    }
  }
}
