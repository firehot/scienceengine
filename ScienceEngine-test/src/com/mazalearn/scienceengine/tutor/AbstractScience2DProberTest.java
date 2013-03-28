package com.mazalearn.scienceengine.tutor;


import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mazalearn.scienceengine.PlatformAdapterImpl;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
import com.mazalearn.scienceengine.domains.electromagnetism.tutor.FieldMagnitudeProber;
import com.mazalearn.scienceengine.domains.electromagnetism.tutor.TutorType;

public class AbstractScience2DProberTest {

  private static ScienceEngine scienceEngine;
  private static LwjglApplication app;
  private Vector2 localPoint = new Vector2();
  private AbstractScience2DProber prober;

  public AbstractScience2DProberTest() {
    prober = new FieldMagnitudeProber(new DummyController(),
      TutorType.FieldMagnitudeProber, null, "goal", "id", null, null,
      0, 0, new String[] {});
  }
  
  @BeforeClass
  public static void setUp() {
    LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
    cfg.title = "ScienceEngine";
    cfg.useGL20 = true;
    cfg.width = 1024; // 800;
    cfg.height = 768; // 480;
    
    scienceEngine = new ScienceEngine("");
    IPlatformAdapter platformAdapter = new PlatformAdapterImpl(Platform.Desktop);
    scienceEngine.setPlatformAdapter(platformAdapter);
    ScienceEngine.DEV_MODE = DevMode.DEBUG;
    app = new LwjglApplication(scienceEngine, cfg);
    app.postRunnable(new Runnable() {
      @Override
      public void run() {
        ScienceEngine.loadAtlas("images/guru/pack.atlas");
      }    
    });
    ScienceEngine.getAssetManager().finishLoading();
    Profile profile = ScienceEngine.getPreferencesManager().getProfile();
    profile.setCurrentActivity(Topic.BarMagnet);
  }

  @AfterClass
  public static void tearDown() {
    // Teardown for data used by the unit tests
  }

  @Test
  public void testIsInsideExcludedActorSimple() {
    Image image = new Image();
    image.setPosition(0, 0);
    image.setSize(100, 100);
    Actor[] actors = new Actor[] { image };
    Assert.assertTrue(prober.isInsideExcludedActor(localPoint.set(0, 0), Arrays.asList(actors)));
    Assert.assertFalse(prober.isInsideExcludedActor(localPoint.set(200, 50), Arrays.asList(actors)));
  }

  @Test
  public void testIsInsideExcludedActorTable() {
    Image image = new Image();
    image.setSize(100, 100);
    
    Table table = new Table(scienceEngine.getSkin());
    table.setPosition(100, 100);
    table.add(image);
    image.invalidateHierarchy();
    table.validate();
    Actor[] actors = new Actor[] { table };
    Assert.assertTrue(prober.isInsideExcludedActor(localPoint.set(60, 60), Arrays.asList(actors)));
    Assert.assertFalse(prober.isInsideExcludedActor(localPoint.set(10, 10), Arrays.asList(actors)));
  }
}