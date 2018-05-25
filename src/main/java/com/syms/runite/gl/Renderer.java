package com.syms.runite.gl;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.glBindBuffer;
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

import com.syms.runite.math.Matrix4f;
import com.syms.runite.gl.BufferObject.Usage;
import com.syms.runite.gl.Shader.Type;
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

  private static final String SHAPE_VERTEX_SHADER = ""
      + "#version 330 core\n"
      + "\n"
      + "uniform mat4 projection;\n"
      + "uniform mat4 model;\n"
      + "uniform vec3 color;\n"
      + "\n"
      + "layout(location = 0) in vec3 pos;\n"
      + "\n"
      + "out vec3 fragment_color;\n"
      + "\n"
      + "void main(){\n"
      + "  gl_Position = projection * model * vec4(pos, 1.0);\n"
      + "  fragment_color = color;\n"
      + "}";

  private static final String SHAPE_FRAGMENT_SHADER = ""
      + "#version 330 core\n"
      + "\n"
      + "in vec3 fragment_color;\n"
      + "\n"
      + "out vec3 color;\n"
      + "\n"
      + "void main(){\n"
      + "  color = fragment_color;\n"
      + "}";

  private int width;
  private int height;
  private boolean reshape;

  private int programId;
  private int vertexArrayId;
  private int vertexBufferId;

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

    vertexShader = createShader(Type.VERTEX, SHAPE_VERTEX_SHADER);
    fragmentShader = createShader(Type.FRAGMENT, SHAPE_FRAGMENT_SHADER);

    shapeProgram = createProgram(vertexShader, fragmentShader);
    shapeProjection = shapeProgram.getUniform("projection");
    shapeModel = shapeProgram.getUniform("model");
    shapeColor = shapeProgram.getUniform("color");

    vertexShader.destroy();
    fragmentShader.destroy();

    square = createVertexArray();
    square.bind();

    squareBuffer = createVertexBuffer(Usage.STATIC_DRAW);
    squareBuffer.setFloatv(UNIT_SQUARE);

    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, NULL);
  }

  public void updateDimensions(int width, int height) {
    this.width = width;
    this.height = height;
    this.reshape = true;
  }

  private void reshape() {
    if (reshape) {
      glViewport(0, 0, width, height);
      reshape = false;
    }
  }

  public Shader createShader(Type type, String source) {
    int id = glCreateShader(type.getId());

    glShaderSource(id, source);
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

  public void bind(Program program) {
    if (programId != program.getId()) {
      logger.info("Bound program; id: {}", program.getId());
      glUseProgram(program.getId());
      programId = program.getId();
    }
  }

  public VertexArrayObject createVertexArray() {
    return new VertexArrayObject(this);
  }

  public void bind(VertexArrayObject array) {
    if (vertexArrayId != array.getId()) {
      logger.info("Bound vertex array; id: {}", array.getId());
      glBindVertexArray(array.getId());
      vertexArrayId = array.getId();
    }
  }

  public VertexBufferObject createVertexBuffer(Usage usage) {
    return new VertexBufferObject(this, usage);
  }

  public void bind(VertexBufferObject buffer) {
    if (vertexBufferId != buffer.getId()) {
      logger.info("Bound vertex buffer; id: {}", buffer.getId());
      glBindBuffer(buffer.getTarget(), buffer.getId());
      vertexBufferId = buffer.getId();
    }
  }

  private void beginShape(float x, float y,
                          float width, float height,
                          float red, float green, float blue) {
    reshape();

    projection.asOrthographic(0, this.width, 0.0f, this.height, -1f, 1f);

    model.asIdentity();
    model.translate(x, y, 0.0f);
    model.scale(width, height, 0.0f);

    bind(shapeProgram);
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

    bind(square);
    glDrawArrays(GL_TRIANGLES, 0, 6);
  }

  public void destroy() {
    square.destroy();
    squareBuffer.destroy();
    shapeProgram.destroy();
  }
}
