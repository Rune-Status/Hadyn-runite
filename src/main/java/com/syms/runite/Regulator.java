package com.syms.runite;

public final class Regulator {

  private static final long NANOS_PER_MILLIS = 1000000L;
  private static final int MAXIMUM_CYCLES = 10;

  private long previous;

  public Regulator() {
    previous = System.nanoTime();
  }

  public int sleep(int minimum, int delta) {
    long min = (long) minimum * NANOS_PER_MILLIS;

    long elapsed = previous - System.nanoTime();
    if (elapsed < min) {
      elapsed = min;
    }

    try {
      Thread.sleep(elapsed / NANOS_PER_MILLIS);
    } catch (InterruptedException ignored) {
    }

    long current = System.nanoTime();

    int cycles;
    for (cycles = 0;
        cycles < MAXIMUM_CYCLES && (cycles < 1 || previous < current);
        previous += NANOS_PER_MILLIS * (long) delta) {
      ++cycles;
    }

    if (previous < current) {
      previous = current;
    }

    return cycles;
  }
}
