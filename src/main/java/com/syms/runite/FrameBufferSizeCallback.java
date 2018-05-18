package com.syms.runite;

import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;

public final class FrameBufferSizeCallback implements GLFWFramebufferSizeCallbackI {

  private final Client client;

  FrameBufferSizeCallback(Client client) {
    this.client = client;
  }

  @Override
  public void invoke(long window, int width, int height) {
    client.frameBufferResized(width, height);
  }
}
