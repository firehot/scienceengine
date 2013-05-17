package com.mazalearn.scienceengine;

public enum Device {
  Android(800,480),
  Nexus7(1280,800),
  IPad(1024,768),
  IPhone(480,320),
  IPhone4(960,640),
  IPhone5(1136, 640), 
  Desktop(1280,1024),
  GalaxyY(320, 240),
  GalaxyS3(1280,720),
  GalaxyS4(1920,1080);
  
  public final int height;
  public final int width;

  private Device(int width, int height) {
    this.width = width;
    this.height = height;
  }
}