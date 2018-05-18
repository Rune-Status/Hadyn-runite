package com.syms.runite;

import java.nio.FloatBuffer;

public final class Matrix4f {

  private float a00;
  private float a01;
  private float a02;
  private float a03;

  private float a10;
  private float a11;
  private float a12;
  private float a13;

  private float a20;
  private float a21;
  private float a22;
  private float a23;

  private float a30;
  private float a31;
  private float a32;
  private float a33;

  public Matrix4f() {
    setIdentity();
  }

  public void setIdentity() {
    a00 = a11 = a22 = a33 = 1.0f;
    a01 = a02 = a03 = a10 = a12 = a13 = a20 = a21 = a23 = a30 = a31 = a32 = 0.0f;
  }

  public void asOrthographic(float left,
                             float right,
                             float top,
                             float bottom,
                             float near,
                             float far) {
    setIdentity();

    a00 = 2.0f / (right - left);
    a11 = 2.0f / (top - bottom);
    a22 = -2.0f / (far - near);

    a03 = -(right + left) / (right - left);
    a13 = -(top + bottom) / (top - bottom);
    a23 = -(far + near) / (far - near);
    a33 = 1.0f;
  }

  public Matrix4f asTranslation(float x, float y, float z) {
    setIdentity();
    a03 = x;
    a13 = y;
    a23 = z;

    return this;
  }

  public Matrix4f translate(float x, float y, float z) {
    float _a03 = a00 * x + a01 * y + a02 * z + a03;
    float _a13 = a10 * x + a11 * y + a12 * z + a13;
    float _a23 = a20 * x + a21 * y + a22 * z + a23;

    a03 = _a03;
    a13 = _a13;
    a23 = _a23;

    return this;
  }

  public Matrix4f asScaling(float x, float y, float z) {
    setIdentity();

    a00 = x;
    a11 = y;
    a22 = z;

    return this;
  }

  public Matrix4f scale(float x, float y, float z) {
    float _a00 = a00 * x;
    float _a11 = a11 * y;
    float _a22 = a22 * z;

    a00 = _a00;
    a11 = _a11;
    a22 = _a22;

    return this;
  }

  public Matrix4f multiply(Matrix4f matrix) {
    float _a00 = a00 * matrix.a00 + a01 * matrix.a10 + a02 * matrix.a20 + a03 * matrix.a30;
    float _a01 = a00 * matrix.a01 + a01 * matrix.a11 + a02 * matrix.a21 + a03 * matrix.a31;
    float _a02 = a00 * matrix.a02 + a01 * matrix.a12 + a02 * matrix.a22 + a03 * matrix.a32;
    float _a03 = a00 * matrix.a03 + a01 * matrix.a13 + a02 * matrix.a23 + a03 * matrix.a33;

    float _a10 = a10 * matrix.a00 + a11 * matrix.a10 + a12 * matrix.a20 + a13 * matrix.a30;
    float _a11 = a10 * matrix.a01 + a11 * matrix.a11 + a12 * matrix.a21 + a13 * matrix.a31;
    float _a12 = a10 * matrix.a02 + a11 * matrix.a12 + a12 * matrix.a22 + a13 * matrix.a32;
    float _a13 = a10 * matrix.a03 + a11 * matrix.a13 + a12 * matrix.a23 + a13 * matrix.a33;

    float _a20 = a20 * matrix.a00 + a21 * matrix.a10 + a22 * matrix.a20 + a23 * matrix.a30;
    float _a21 = a20 * matrix.a01 + a21 * matrix.a11 + a22 * matrix.a21 + a23 * matrix.a31;
    float _a22 = a20 * matrix.a02 + a21 * matrix.a12 + a22 * matrix.a22 + a23 * matrix.a32;
    float _a23 = a20 * matrix.a03 + a21 * matrix.a13 + a22 * matrix.a23 + a23 * matrix.a33;

    float _a30 = a30 * matrix.a00 + a31 * matrix.a10 + a32 * matrix.a20 + a33 * matrix.a30;
    float _a31 = a30 * matrix.a01 + a31 * matrix.a11 + a32 * matrix.a21 + a33 * matrix.a31;
    float _a32 = a30 * matrix.a02 + a31 * matrix.a12 + a32 * matrix.a22 + a33 * matrix.a32;
    float _a33 = a30 * matrix.a03 + a31 * matrix.a13 + a32 * matrix.a23 + a33 * matrix.a33;

    a00 = _a00;
    a01 = _a01;
    a02 = _a02;
    a03 = _a03;

    a10 = _a10;
    a11 = _a11;
    a12 = _a12;
    a13 = _a13;

    a20 = _a20;
    a21 = _a21;
    a22 = _a22;
    a23 = _a23;

    a30 = _a30;
    a31 = _a31;
    a32 = _a32;
    a33 = _a33;

    return this;
  }

  /**
   * Stores the data in the buffer in column-major order.
   */
  public void put(FloatBuffer buffer) {
    buffer.put(a00);
    buffer.put(a10);
    buffer.put(a20);
    buffer.put(a30);

    buffer.put(a01);
    buffer.put(a11);
    buffer.put(a21);
    buffer.put(a31);

    buffer.put(a02);
    buffer.put(a12);
    buffer.put(a22);
    buffer.put(a32);

    buffer.put(a03);
    buffer.put(a13);
    buffer.put(a23);
    buffer.put(a33);
  }
}