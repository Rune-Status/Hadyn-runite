package com.syms.runite.renderer;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.system.MemoryUtil.NULL;

import com.syms.runite.Matrix4f;
import com.syms.runite.renderer.Shader.Type;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Renderer {

  private static final Logger logger = LoggerFactory.getLogger(Renderer.class);

  /**
   * Points which describe a unit square. The square is broken into two triangles.
   */
  private static final float[] UNIT_SQUARE = new float[]{
      1.0f, 1.0f, 0.0f,
      1.0f, 0.0f, 0.0f,
      0.0f, 1.0f, 0.0f,

      0.0f, 1.0f, 0.0f,
      1.0f, 0.0f, 0.0f,
      0.0f, 0.0f, 0.0f,
  };

  private int width;
  private int height;
  private boolean reshape;

  private int programId;
  private int vertexArrayId;

  private VertexArrayObject square;
  private VertexBufferObject squareBuffer;

  private Program shapeProgram;
  private Uniform shapeProjection;
  private Uniform shapeModel;
  private Uniform shapeColor;

  private final Matrix4f projection = new Matrix4f();
  private final Matrix4f model = new Matrix4f();

  public void init(int width, int height) {
    this.width = width;
    this.height = height;

    Shader vertexShader;
    Shader fragmentShader;

    try {
      vertexShader = compileShader(Type.VERTEX, "shader/shape.vert");
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    try {
      fragmentShader = compileShader(Type.FRAGMENT, "shader/shape.frag");
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    shapeProgram = createProgram(vertexShader, fragmentShader);
    shapeProjection = shapeProgram.getUniform("projection");
    shapeModel = shapeProgram.getUniform("model");
    shapeColor = shapeProgram.getUniform("color");

    vertexShader.destroy();
    fragmentShader.destroy();

    square = createVertexArray();
    square.bind();

    squareBuffer = new VertexBufferObject();
    squareBuffer.set(UNIT_SQUARE, GL_STATIC_DRAW);

    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, NULL);
  }

  public void updateDimensions(int width, int height) {
    this.width = width;
    this.height = height;
    this.reshape = true;
  }

  public Shader compileShader(Type type, String path) throws IOException {
    return compileShader(type, Paths.get(path));
  }

  public Shader compileShader(Type type, Path path) throws IOException {
    return createShader(type, new String(Files.readAllBytes(path), Charset.forName("UTF-8")));
  }

  public Shader createShader(Type type, String src) {
    int id = glCreateShader(type.getId());

    glShaderSource(id, src);
    glCompileShader(id);

    if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
      throw new ShaderException("Failed to compile shader: ", glGetShaderInfoLog(id));
    }

    return new Shader(id);
  }

  public Program createProgram(Shader... shaders) {
    int id = glCreateProgram();

    for (Shader shader : shaders) {
      glAttachShader(id, shader.getId());
    }

    glLinkProgram(id);

    if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
      for (Shader shader : shaders) {
        glDetachShader(id, shader.getId());
      }
      throw new ProgramException("Failed to link program: ", glGetProgramInfoLog(id));
    }

    for (Shader shader : shaders) {
      glDetachShader(id, shader.getId());
    }

    return new Program(this, id);
  }

  public void useProgram(int id) {
    if (programId != id) {
      logger.info("Using program {}", id);
      glUseProgram(id);
      programId = id;
    }
  }

  public VertexArrayObject createVertexArray() {
    return new VertexArrayObject(this);
  }

  public void bindVertexArray(int id) {
    if (vertexArrayId != id) {
      logger.info("Bound vertex array {}.", id);
      glBindVertexArray(id);
      vertexArrayId = id;
    }
  }

  private void beginShape(float x, float y,
                          float width, float height,
                          float red, float green, float blue) {
    reshape();

    projection.asOrthographic(0, this.width, 0.0f, this.height, -1f, 1f);

    model.asTranslation(x, y, 0.0f)
        .scale(width, height, 0.0f);

    shapeProgram.use();
    shapeProjection.setMatrix4f(projection);
    shapeModel.setMatrix4f(model);
    shapeColor.setVector3f(red, green, blue);
  }

  public void fillQuad(int x, int y, int width, int height, int color) {
    beginShape(x, y,
               width, height,
               (color >> 16 & 0xff) / 255.0f,
               (color >> 8  & 0xff) / 255.0f,
               (color       & 0xff) / 255.0f);

    square.bind();
    glDrawArrays(GL_TRIANGLES, 0, UNIT_SQUARE.length / 3);
  }

  private void reshape() {
    if (reshape) {
      glViewport(0, 0, width, height);
      reshape = false;
    }
  }
}
