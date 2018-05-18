package com.syms.runite;

import org.lwjgl.glfw.GLFWWindowSizeCallbackI;

public final class WindowSizeCallback implements GLFWWindowSizeCallbackI {

  private final Client client;

  WindowSizeCallback(Client client) {
    this.client = client;
  }

  @Override
  public void invoke(long window, int width, int height) {
    client.windowResized(width, height);
  }
}
