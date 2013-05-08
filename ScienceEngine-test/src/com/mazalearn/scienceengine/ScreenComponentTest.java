package com.mazalearn.scienceengine;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ScreenComponentTest {
  
  @BeforeClass
  public static void setUp() {
  }
  
  @AfterClass
  public static void tearDown() {
  }

  @Test
  public void testSelectFontSize() {
    assertEquals(16, ScreenComponent.selectFontSize(16));
    assertEquals(12, ScreenComponent.selectFontSize(13));
    assertEquals(15, ScreenComponent.selectFontSize(14));
    assertEquals(20, ScreenComponent.selectFontSize(19));
  }
  
}
