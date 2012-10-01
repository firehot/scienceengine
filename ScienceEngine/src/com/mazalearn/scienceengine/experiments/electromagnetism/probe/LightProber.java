package com.mazalearn.scienceengine.experiments.electromagnetism.probe;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.core.probe.AbstractScience2DProber;
import com.mazalearn.scienceengine.core.probe.ProbeImage;
import com.mazalearn.scienceengine.core.probe.ProbeManager;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.LightbulbActor;

public class LightProber extends AbstractScience2DProber {

  Vector2 points[] = new Vector2[] { new Vector2() };
  private Image image;
  private LightbulbActor lightbulbActor;
  private ProbeManager probeManager;
  
  public LightProber(final ProbeManager probeManager) {
    super(probeManager);
    this.probeManager = probeManager;
    image = new ProbeImage();
    this.addActor(image);
    this.lightbulbActor = (LightbulbActor) probeManager.findActorByName("Lightbulb");
  }
  
  @Override
  public boolean isAvailable() {
    return lightbulbActor.isVisible();
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    if (lightbulbActor.withinLightRegion(image.getX(), image.getY())) {
      probeManager.done(true);
    }
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      generateProbePoints(points);
      image.setX(points[0].x - image.getWidth()/2);
      image.setY(points[0].y - image.getHeight()/2);
    }
    this.setVisible(activate);
  }

  @Override
  public String getTitle() {
    return "Make the light reach the blinking point.";
  }

}
