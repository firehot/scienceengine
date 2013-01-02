package com.mazalearn.scienceengine.domains.electromagnetism.probe;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.domains.electromagnetism.view.LightbulbActor;
import com.mazalearn.scienceengine.guru.AbstractScience2DProber;
import com.mazalearn.scienceengine.guru.ProbeImage;

public class LightProber extends AbstractScience2DProber {

  Vector2 points[] = new Vector2[] { new Vector2() };
  private Image image;
  private LightbulbActor lightbulbActor;
  private IScience2DModel science2DModel;
  
  public LightProber(IScience2DModel science2DModel, IScience2DView science2DView, 
      String goal, Array<?> components, Array<?> configs, int deltaSuccessScore, int deltaFailureScore) {
    super(science2DModel, science2DView, goal, components, configs, deltaSuccessScore, deltaFailureScore);
    this.science2DModel = science2DModel;
    this.hints = new String[] {
        "Light intensity increases when more current is induced in the coil.",
        "More current is induced in the coil if the magnetic field changes faster at the coil.",
        "Magnetic field change at the coil increases when the magnet moves faster relative to the coil.",
        "If the coil has more loops, more current will be induced.",
        "If the magnet is stronger, more current will be induced."
    };
    image = new ProbeImage();
    this.addActor(image);
    this.lightbulbActor = (LightbulbActor) science2DView.findActor("Lightbulb");
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    if (lightbulbActor != null && lightbulbActor.withinLightRegion(image.getX(), image.getY())) {
      science2DView.getGuru().done(true);
    }
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      science2DView.getGuru().setupProbeConfigs(science2DModel.getAllConfigs(), true);
      generateProbePoints(points);
      image.setX(points[0].x - image.getWidth()/2);
      image.setY(points[0].y - image.getHeight()/2);
    }
    this.setVisible(activate);
  }

  @Override
  public boolean hasSucceeded() {
    return true;
  }  

  @Override
  public boolean hasFailed() {
    return false; // Allow learner to keep trying forever
  }  
}
