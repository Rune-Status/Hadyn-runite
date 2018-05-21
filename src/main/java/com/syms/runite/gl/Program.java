package com.syms.runite.gl;

import static java.lang.String.format;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;

public final class Program {

  private final Renderer renderer;
  private final int id;
  private boolean destroyed;

  Program(Renderer renderer, int id) {
    this.renderer = renderer;
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void bind() {
    renderer.bind(this);
  }

  public Uniform getUniform(String name) {
    bind();
    return new Uniform(glGetUniformLocation(id, name));
  }

  public void destroy() {
    if (destroyed) {
      throw new IllegalStateException(format("Program {} has already been destroyed.", id));
    }
    glDeleteProgram(id);
    destroyed = true;
  }
}
