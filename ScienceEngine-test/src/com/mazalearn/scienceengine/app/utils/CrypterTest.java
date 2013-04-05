package com.mazalearn.scienceengine.app.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CrypterTest {

  @BeforeClass
  public static void setUp() {
  }
  
  @AfterClass
  public static void tearDown() {
  }

  @Test
  public void testSha1_length() {
    System.out.println(Crypter.saltedSha1Hash("test", "install"));
    assertEquals(40, Crypter.saltedSha1Hash("test", "install").length());
  }
  
  @Test
  public void testSha1_unique() {
    assertFalse(Crypter.saltedSha1Hash("test1", "install").equals(Crypter.saltedSha1Hash("test", "install")));
  }

  @Test
  public void testSha1_repeatable() {
    assertEquals(Crypter.saltedSha1Hash("test", "install"), Crypter.saltedSha1Hash("test", "install"));
  }

  @Test
  public void testSaltedSha1_distinct() {
    assertFalse(Crypter.saltedSha1Hash("test", "install1").equals(Crypter.saltedSha1Hash("test", "install2")));
  }

}
