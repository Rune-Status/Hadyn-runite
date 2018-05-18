package com.syms.runite.renderer;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glUseProgram;

public final class Program {

  private final int id;

  private Program(int id) {
    this.id = id;
  }

  public static Program link(Shader... shaders) {
    int id = glCreateProgram();

    for (Shader shader : shaders) {
      glAttachShader(id, shader.getId());
    }

    glLinkProgram(id);

    if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
      throw new ProgramException("Failed to link program: ", glGetProgramInfoLog(id));
    }

    for (Shader shader : shaders) {
      glDetachShader(id, shader.getId());
    }

    return new Program(id);
  }

  public void use() {
    glUseProgram(id);
  }

  public Uniform getUniform(String name) {
    use();
    return new Uniform(glGetUniformLocation(id, name));
  }
}
