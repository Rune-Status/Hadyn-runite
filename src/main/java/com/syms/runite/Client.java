package com.syms.runite;

import static java.lang.String.format;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;

import com.syms.runite.renderer.Renderer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Client {

  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  private static final String VERSION = "0.1.0";

  private long window = NULL;
  private int width = 500;
  private int height = 500;
  private boolean resized = true;

  private final Regulator regulator = new Regulator();
  private final int minimum = 1;
  private final int delta = 20;

  private int frameBufferWidth = -1;
  private int frameBufferHeight = -1;
  private boolean frameBufferResized;

  private final Renderer renderer = new Renderer();

  private boolean clean = true;

  Client() {}

  public static void main(String... args) {
    new Client().start();
  }

  public void init() {
    logger.info("Starting up Runite; version: {}", VERSION);

    glfwSetWindowSizeCallback(window, new WindowSizeCallback(this));
    glfwSetFramebufferSizeCallback(window, new FrameBufferSizeCallback(this));

    logger.info("Initializing the renderer");

    try (MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer width = stack.mallocInt(1);
      IntBuffer height = stack.mallocInt(1);
      glfwGetFramebufferSize(window, width, height);
      frameBufferWidth = width.get();
      frameBufferHeight = height.get();
    }

    renderer.init(frameBufferWidth, frameBufferHeight);
  }

  private void beginUpdate() {
    try {
      update();
    } catch (Throwable throwable) {
      logger.error("Uncaught exception caught while updating.", throwable);
      shutdown(false);
    }
  }

  public void update() {
    if (resized) {
      resized = false;
    }
  }

  private void beginDraw() {
    try {
      draw();
    } catch (Throwable throwable) {
      logger.error("Uncaught exception caught while drawing.", throwable);
      shutdown(false);
    }
  }

  private int a = 0;

  public void draw() {
    if (frameBufferResized) {
      renderer.updateDimensions(frameBufferWidth, frameBufferHeight);
      frameBufferResized = false;
    }

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    renderer.fillQuad(0, 0, 50, 50, 0xffff);
  }

  public void shutdown(boolean clean) {
    glfwSetWindowShouldClose(window, true);
    this.clean = clean;
  }

  public void destroy() {
    glfwFreeCallbacks(window);
    glfwDestroyWindow(window);
    window = NULL;

    glfwTerminate();
  }

  public void start() {
    if (!glfwInit()) {
      throw new IllegalStateException("Failed to initialize GLFW.");
    }

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

    window = glfwCreateWindow(width, height, format("Runite %s", VERSION), NULL, NULL);
    if (window == NULL) {
      throw new IllegalStateException("Failed to create window.");
    }

    glfwMakeContextCurrent(window);

    GL.createCapabilities();
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    init();

    glfwShowWindow(window);

    while (!glfwWindowShouldClose(window)) {
      int cycles = regulator.sleep(minimum, delta);
      for (int i = 0; i < cycles; i++) {
        glfwPollEvents();
        beginUpdate();
      }

      beginDraw();

      glfwSwapBuffers(window);
    }

    logger.info("Shutting down; clean: {}", clean);

    destroy();
  }

  void windowResized(int width, int height) {
    logger.info("Window has been resized; width: {}, height: {}", width, height);
    this.width = width;
    this.height = height;
    this.resized = true;
  }

  void frameBufferResized(int width, int height) {
    logger.info("Frame buffer has been resized; width: {}, height: {}", width, height);
    frameBufferWidth = width;
    frameBufferHeight = height;
    frameBufferResized = true;
  }
}
