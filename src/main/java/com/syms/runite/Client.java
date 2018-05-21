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
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import com.syms.runite.asset.Cache;
import com.syms.runite.gl.Renderer;
import com.syms.runite.io.BufferedFile;
import com.syms.runite.io.FileOnDisk;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Client {

  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  private static final String VERSION = "0.1.0";

  private static final int TYPE_COUNT = 17;

  private final Regulator regulator = new Regulator();
  private final int minimum = 1;
  private final int delta = 20;

  private final Renderer renderer = new Renderer();

  private long window = NULL;
  private int width = 500;
  private int height = 500;
  private boolean resized = true;

  private BufferedFile blocksFile;
  private BufferedFile metaIndex;
  private BufferedFile[] indexFiles = new BufferedFile[TYPE_COUNT];

  private int frameBufferWidth = -1;
  private int frameBufferHeight = -1;
  private boolean frameBufferResized;

  private boolean cleanShutdown = true;

  Client() {}

  public static void main(String... args) {
    new Client().start();
  }

  public void init() {
    logger.info("Starting up Runite; version: {}", VERSION);

    glfwSetWindowSizeCallback(window, new WindowSizeCallback());
    glfwSetFramebufferSizeCallback(window, new FrameBufferSizeCallback());

    try {
      blocksFile = new BufferedFile(
          new FileOnDisk(getCacheFile("main_file_cache.dat2"), "rw"), Cache.BLOCK_LENGTH * 10, 0);

      metaIndex = new BufferedFile(
          new FileOnDisk(getCacheFile("main_file_cache.idx255"), "rw"),
            Cache.REFERENCE_LENGTH * 1000, 0);

      for (int i = 0; i < indexFiles.length; i++) {
        indexFiles[i] = new BufferedFile(
            new FileOnDisk(getCacheFile("main_file_cache.idx" + i), "rw"),
              Cache.REFERENCE_LENGTH * 1000, 0);
      }
    } catch (IOException reason) {
      panic("Failed to find cache files.", reason);
      return;
    }

    logger.info("Initializing the renderer");

    try (MemoryStack stack = stackPush()) {
      IntBuffer width = stack.mallocInt(1);
      IntBuffer height = stack.mallocInt(1);
      glfwGetFramebufferSize(window, width, height);
      frameBufferWidth = width.get();
      frameBufferHeight = height.get();
    }

    renderer.init(frameBufferWidth, frameBufferHeight);
  }

  private File getCacheFile(String path) {
    return new File("assets/cache/" + path);
  }

  private void beginUpdate() {
    try {
      update();
    } catch (Throwable throwable) {
      panic("Uncaught exception caught while updating.", throwable);
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
      panic("Uncaught exception caught while drawing.", throwable);
    }
  }

  public void draw() {
    if (frameBufferResized) {
      renderer.updateDimensions(frameBufferWidth, frameBufferHeight);
      frameBufferResized = false;
    }

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    renderer.fillQuad(25, 25, 50, 50, 0xffffff);
  }

  public void panic(String message, Throwable throwable) {
    logger.error(message, throwable);
    shutdown(false);
  }

  public void shutdown(boolean clean) {
    glfwSetWindowShouldClose(window, true);
    cleanShutdown = clean;
  }

  public void destroy() {
    logger.info("Closing files.");

    try {
      blocksFile.close();
      metaIndex.close();

      for (BufferedFile file : indexFiles) {
        file.close();
      }
    } catch (IOException ignored) {
    }

    logger.info("Freeing renderer resources.");
    renderer.destroy();

    logger.info("Freeing GLFW resources.");
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

    logger.info("Shutting down; clean: {}", cleanShutdown);

    destroy();
  }

  public void windowResized(int width, int height) {
    logger.info("Window has been resized; width: {}, height: {}", width, height);
    this.width = width;
    this.height = height;
    this.resized = true;
  }

  private void frameBufferResized(int width, int height) {
    logger.info("Frame buffer has been resized; width: {}, height: {}", width, height);
    frameBufferWidth = width;
    frameBufferHeight = height;
    frameBufferResized = true;
  }

  private final class FrameBufferSizeCallback implements GLFWFramebufferSizeCallbackI {

    @Override
    public void invoke(long window, int width, int height) {
      frameBufferResized(width, height);
    }
  }

  private final class WindowSizeCallback implements GLFWWindowSizeCallbackI {

    @Override
    public void invoke(long window, int width, int height) {
      windowResized(width, height);
    }
  }
}
