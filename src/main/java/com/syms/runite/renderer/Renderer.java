package com.syms.runite.renderer;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.system.MemoryUtil.NULL;

import com.syms.runite.Matrix4f;
import com.syms.runite.renderer.Shader.Type;
import java.io.IOException;

public final class Renderer {

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
      vertexShader = Shader.from(Type.VERTEX, "shader/shape.vert");
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    try {
      fragmentShader = Shader.from(Type.FRAGMENT, "shader/shape.frag");
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    shapeProgram = Program.link(vertexShader, fragmentShader);
    shapeProjection = shapeProgram.getUniform("projection");
    shapeModel = shapeProgram.getUniform("model");
    shapeColor = shapeProgram.getUniform("color");

    vertexShader.destroy();
    fragmentShader.destroy();

    square = new VertexArrayObject();
    square.bind();

    squareBuffer = new VertexBufferObject();
    squareBuffer.set(UNIT_SQUARE, GL_STATIC_DRAW);

    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, NULL);
    glBindVertexArray(0);
  }

  public void updateDimensions(int width, int height) {
    this.width = width;
    this.height = height;
    this.reshape = true;
  }

  private void beginShape(float x, float y,
                          float width, float height,
                          float red, float green, float blue) {
    reshape();

    // Set up the projection to be orthographic. This centers the origin to being at the upper left
    // of the screen.
    projection.asOrthographic(0, this.width, 0.0f, this.height, -1f, 1f);

    model.asTranslation(x, y, 0.0f)
        .scale(width, height, 0.0f);

    shapeProgram.use();
    shapeProjection.set(projection);
    shapeModel.set(model);
    shapeColor.set(red, green, blue);
  }

  public void fillQuad(int x, int y, int width, int height, int color) {
    beginShape(x, y,
               width, height,
               (color >> 16 & 0xff) / 255.0f,
               (color >> 8  & 0xff) / 255.0f,
               (color       & 0xff) / 255.0f);

    square.bind();
    glDrawArrays(GL_TRIANGLES, 0, 6);
    glBindVertexArray(0);
  }

  private void reshape() {
    if (reshape) {
      glViewport(0, 0, width, height);
      reshape = false;
    }
  }
}
