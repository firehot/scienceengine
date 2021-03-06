package com.mazalearn.scienceengine.domains.electromagnetism.tutor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;
import com.mazalearn.scienceengine.domains.electromagnetism.view.LightbulbActor;
import com.mazalearn.scienceengine.tutor.AbstractScience2DProber;
import com.mazalearn.scienceengine.tutor.ITutor;
import com.mazalearn.scienceengine.tutor.ProbeImage;

public class LightProber extends AbstractScience2DProber {

  Vector2 points[] = new Vector2[] { new Vector2() };
  private Image image;
  private LightbulbActor lightbulbActor;
  
  public LightProber(IScience2DController science2DController, ITutorType tutorType, ITutor parent,
      Topic topic, String goal, String name, Array<?> components, Array<?> configs,
      String[] hints, String[] explanation, String[] refs) {
    super(science2DController, tutorType, topic, parent, goal, name, components, configs, 
        hints, explanation, refs);
/*            "Light intensity increases when more current is induced in the coil.",
            "More current is induced in the coil if the magnetic field changes faster at the coil.",
            "Magnetic field change at the coil increases when the magnet moves faster relative to the coil.",
            "If the coil has more loops, more current will be induced.",
            "If the magnet is stronger, more current will be induced." */
    image = new ProbeImage();
    this.addActor(image);
    this.lightbulbActor = (LightbulbActor) science2DController.getView().findActor(ComponentType.Lightbulb.name());
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    if (lightbulbActor != null && lightbulbActor.withinLightRegion(image.getX(), image.getY())) {
      tutorHelper.showCorrect(getSuccessPoints());
      systemReadyToFinish(true);
    }
  }
  
  @Override
  public void teach() {
    super.teach();
    science2DController.getGuru().setupProbeConfigs(
        science2DController.getModel().getAllConfigs().values(), true);
    generateProbePoints(points);
    image.setX(points[0].x - image.getWidth()/2);
    image.setY(points[0].y - image.getHeight()/2);
  }
}
