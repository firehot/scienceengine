package com.mazalearn.scienceengine.domains.electromagnetism.probe;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.probe.AbstractScience2DProber;
import com.mazalearn.scienceengine.core.probe.ProbeImage;
import com.mazalearn.scienceengine.core.probe.ProbeManager;
import com.mazalearn.scienceengine.domains.electromagnetism.view.LightbulbActor;

public class LightProber extends AbstractScience2DProber {

  Vector2 points[] = new Vector2[] { new Vector2() };
  private Image image;
  private LightbulbActor lightbulbActor;
  private ProbeManager probeManager;
  private String[] hints = {
      "Light intensity increases when more current is induced in the coil.",
      "More current is induced in the coil if the magnetic field changes faster at the coil.",
      "Magnetic field change at the coil increases when the magnet moves faster relative to the coil.",
      "If the coil has more loops, more current will be induced.",
      "If the magnet is stronger, more current will be induced."
  };
  private IScience2DModel science2DModel;
  
  public LightProber(IScience2DModel science2DModel, ProbeManager probeManager) {
    super(probeManager);
    this.probeManager = probeManager;
    this.science2DModel = science2DModel;
    image = new ProbeImage();
    this.addActor(image);
    this.lightbulbActor = (LightbulbActor) probeManager.findStageActor("Lightbulb");
  }
  
  @Override
  public boolean isAvailable() {
    return lightbulbActor != null && lightbulbActor.isVisible();
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    if (lightbulbActor != null && lightbulbActor.withinLightRegion(image.getX(), image.getY())) {
      probeManager.done(true);
    }
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      probeManager.setupProbeConfigs(science2DModel.getAllConfigs(), true);
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

  @Override
  public String[] getHints() {
    return hints;
  }

}
