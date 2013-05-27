package com.mazalearn.scienceengine;

import com.badlogic.gdx.graphics.Color;

public enum StatusType {
  INFO(Color.WHITE), WARNING(Color.YELLOW), ERROR(Color.RED), FATAL(Color.RED);
  public final Color color;

  private StatusType(Color color) {
    this.color = color;
  }
}
