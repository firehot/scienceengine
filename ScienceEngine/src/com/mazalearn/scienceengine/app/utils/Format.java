package com.mazalearn.scienceengine.app.utils;

public class Format {

  public static String formatTime(float time) {
    String seconds = String.valueOf(Math.round(time) % 60);
    return Math.round(time) / 60 + ":" + "0".substring(0, 2 - seconds.length()) + seconds + "s";
  }

}
