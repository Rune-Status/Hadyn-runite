package com.syms.runite.renderer;

import static java.lang.String.format;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glShaderSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Shader {

  private final int id;
  private boolean destroyed;

  private Shader(int id) {
    this.id = id;
  }

  public static Shader from(Type type, String path) throws IOException {
    return from(type, Paths.get(path));
  }

  public static Shader from(Type type, Path path) throws IOException {
    return compile(type, new String(Files.readAllBytes(path), Charset.forName("UTF-8")));
  }

  public static Shader compile(Type type, String src) throws ShaderException {
    int id = glCreateShader(type.getId());

    glShaderSource(id, src);
    glCompileShader(id);

    if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
      throw new ShaderException("Failed to compile shader: ", glGetShaderInfoLog(id));
    }

    return new Shader(id);
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
