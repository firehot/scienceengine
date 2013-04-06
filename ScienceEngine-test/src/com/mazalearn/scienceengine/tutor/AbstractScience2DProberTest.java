package com.mazalearn.scienceengine.tutor;


import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mazalearn.scienceengine.PlatformAdapterImpl;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.domains.electromagnetism.tutor.FieldMagnitudeProber;
import com.mazalearn.scienceengine.domains.electromagnetism.tutor.TutorType;

public class AbstractScience2DProberTest {

  private static ScienceEngine scienceEngine;
  private static IScience2DView science2DView;
  private static IScience2DController science2DController;
  private static LwjglApplication app;
  private Vector2 localPoint = new Vector2();
  private AbstractScience2DProber prober;

  public AbstractScience2DProberTest() {
    prober = new FieldMagnitudeProber(science2DController,
      TutorType.FieldMagnitudeProber, null, "goal", "id", null, null,
      0, 0, new String[] {}, null);
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
        science2DController = new DummyController(scienceEngine.getSkin());
        science2DView = science2DController.getView();
      }    
    });
    ScienceEngine.getAssetManager().finishLoading();
    Profile profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    profile.setCurrentTopic(Topic.Electromagnetism);
    profile.setCurrentActivity(Topic.BarMagnet);
  }

  @AfterClass
  public static void tearDown() {
    // Teardown for data used by the unit tests
  }

  @Test
  public void testIsInsideExcludedActor_simple() {
    /**
     *       100,100
     * |~~~~|
     * |    |
     * 0,0~~
     */
    Image image = new Image();
    image.setPosition(0, 0);
    image.setSize(100, 100);
    List<Actor> actors =  Arrays.asList(new Actor[] { image });
    for (Actor actor: actors) {
      ((Stage) science2DView).addActor(actor);
    }
    Assert.assertTrue(prober.isInsideExcludedActor(localPoint.set(0, 0), actors));
    Assert.assertFalse(prober.isInsideExcludedActor(localPoint.set(200, 50), actors));
  }

  @Test
  public void testIsInsideExcludedActor_2Actors() {
    /**
     *     100,100        300,100
     * X~~~~X~~~~~~~~~~X~~~~X
     * |    |          |    | 
     * X~~~~X~~~~~~~~~~X~~~~X
     * 0,0            200,0
     */
    Image image1 = new Image();
    image1.setPosition(0, 0);
    image1.setSize(100, 100);
    Image image2 = new Image();
    image2.setPosition(200, 0);
    image2.setSize(100, 100);
    List<Actor> actors =  Arrays.asList(new Actor[] { image1, image2 });
    for (Actor actor: actors) {
      ((Stage) science2DView).addActor(actor);
    }
    Assert.assertTrue(prober.isInsideExcludedActor(localPoint.set(50, 50), actors));
    Assert.assertTrue(prober.isInsideExcludedActor(localPoint.set(250, 50), actors));
    Assert.assertFalse(prober.isInsideExcludedActor(localPoint.set(150, 50), actors));
  }

  @Test
  public void testIsInsideExcludedActor_table() {
    /**
     *     150,150
     * X~~~~X
     * |  x | 
     * X~~~~X
     * 50,50
     */
    Image image = new Image();
    image.setSize(100, 100);
    ((Stage) science2DView).addActor(image);
    
    Table table = new Table(scienceEngine.getSkin());
    table.setPosition(100, 100);
    table.add(image).width(100).height(100).center();
    List<Actor> actors =  Arrays.asList(new Actor[] { table });
    for (Actor actor: actors) {
      ((Stage) science2DView).addActor(actor);
    }
    //image.invalidateHierarchy();
    table.validate();
    Assert.assertTrue(prober.isInsideExcludedActor(localPoint.set(60, 60), actors));
    Assert.assertFalse(prober.isInsideExcludedActor(localPoint.set(10, 10),  actors));
    image.setSize(10, 10); // Much smaller than table cell now
    table.validate();
    Assert.assertTrue(prober.isInsideExcludedActor(localPoint.set(60, 60), actors));
  }
}