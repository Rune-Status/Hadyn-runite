package com.syms.runite.renderer;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;

public final class Program {

  private final Renderer renderer;
  private final int id;

  Program(Renderer renderer, int id) {
    this.renderer = renderer;
    this.id = id;
  }

  public void use() {
    renderer.useProgram(id);
  }

  public Uniform getUniform(String name) {
    use();
    return new Uniform(glGetUniformLocation(id, name));
  }
}
