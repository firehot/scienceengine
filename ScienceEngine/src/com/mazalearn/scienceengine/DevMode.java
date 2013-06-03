package com.mazalearn.scienceengine;

// mode of development
public class DevMode {
  static int DEBUG = 1;
  private static int DESIGN = 2;
  private static int BILLING_DUMMY = 4;
  private int DEV_MODE;
  private boolean isCheckMode(int mode) {
    return (DEV_MODE & mode) != 0; 
  }
  private void setMode(int mode, boolean on) {
    if (on) {
      DEV_MODE |= mode;
    } else {
      DEV_MODE &= ~mode;
    }
  }
  public void setDebug(boolean on) {
    setMode(DEBUG, on);
  }
  public void setDesign(boolean on) {
    setMode(DESIGN, on);
  }
  public void setDummyBilling(boolean on) {
    setMode(BILLING_DUMMY, on);
  }
  public boolean isDebug() {
    return isCheckMode(DEBUG);
  }
  public boolean isDummyBilling() {
    return isCheckMode(BILLING_DUMMY);
  }
  public boolean isDesign() {
    return isCheckMode(DESIGN);
  }
}