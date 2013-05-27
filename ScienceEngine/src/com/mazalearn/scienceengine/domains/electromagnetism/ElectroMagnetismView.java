package com.mazalearn.scienceengine.domains.electromagnetism;

import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.StatusType;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.AbstractScience2DModel;
import com.mazalearn.scienceengine.core.model.CoreComponentType;
import com.mazalearn.scienceengine.core.model.ICurrent.CircuitElement;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;
import com.mazalearn.scienceengine.core.view.CoachDrawingActor;
import com.mazalearn.scienceengine.core.view.ScienceTrainActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Dynamo;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Magnet;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CircuitActor;

public class ElectroMagnetismView extends AbstractScience2DView {

  private Dynamo dynamo;
  private Magnet magnet;
  private Lightbulb lightbulb;

  public ElectroMagnetismView(float width, float height,
      final AbstractScience2DModel emModel, Skin skin, IScience2DController controller) {
    super(emModel, width, height, skin, controller);
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    // TODO: Make below part of scripting language - only in level 9
    if (dynamo != null && magnet != null) {
      // Clearance of 1 unit between magnet and dynamo is enforced
      magnet.setMaxWidth(dynamo.getWidth() - 1f);
      dynamo.setMinWidth(magnet.getWidth() + 1f);
      float strength = magnet.getStrength();
      float scale = (float) Math.pow(dynamo.getWidth() - magnet.getWidth(), 2);
      dynamo.setMagnetFlux(strength / scale);
    }
  }
  
  @Override
  public void prepareView() {
    // Below are only in science train level
    dynamo = (Dynamo) science2DModel.findBody(ComponentType.Dynamo);
    magnet = (Magnet) science2DModel.findBody(ComponentType.Magnet);
    lightbulb = (Lightbulb) science2DModel.findBody(ComponentType.Lightbulb);
    ScienceTrainActor scienceTrainActor = 
        (ScienceTrainActor) findActor(CoreComponentType.ScienceTrain.name());
    if (scienceTrainActor != null) {
      scienceTrainActor.reset();
    }
    // above only in science train level


    Group activityGroup = (Group) findActor(ScreenComponent.ACTIVITY_GROUP);
    for (List<CircuitElement> circuit: science2DModel.getCircuits()) {
      activityGroup.addActor(new CircuitActor(circuit));
    }
    super.prepareView();
  }
  
  @Override
  public void initializeCommands(List<IModelConfig<?>> commands) {
    super.initializeCommands(commands);
    commands.add(new AbstractModelConfig<String>("Upload") { //$NON-NLS-1$ //$NON-NLS-2$
      public void doCommand() { saveInProfile(); }
      public boolean isPossible() { return true; }
    });
  }
  
  private void saveInProfile() {
    float current = Math.round(dynamo.getMaxCurrent());
    Color color = lightbulb.getColor();

    // Set light to display on coach drawing
    CoachDrawingActor coachDrawingActor = 
        (CoachDrawingActor) findActor(CoreComponentType.Drawing.name());
    coachDrawingActor.getCoach().setLight(current, lightbulb.getColor());
    
    // Show only the main actors - ScienceTrain and control related
    List<String> actorsToBeHidden = 
        Arrays.asList(new String[]{ComponentType.Dynamo.name(),  ComponentType.Magnet.name(), 
            CircuitActor.COMPONENT_TYPE, CoreComponentType.Drawing.name(), 
            ComponentType.Lightbulb.name()});
    ScienceTrainActor scienceTrain = (ScienceTrainActor) findActor(CoreComponentType.ScienceTrain.name());
    scienceTrain.animate();
    for (String actorName: actorsToBeHidden) {
      Actor actor = findActor(actorName);
      if (actor != null) {
        actor.setVisible(false);
      }
    }
    
    // Save drawing png in profile
    Profile profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    // Save drawing, current and color in profile
    profile.setCoachPixmap(coachDrawingActor.getPixmap(), current, rgba(color));
    ScienceEngine.displayStatusMessage(this, StatusType.INFO, "Uploading to MazaLearn - See www.mazalearn.com/train.html");    
  }

  private String rgba(Color color) {
    return "rgba(" + (int) (color.r * 255) + "," + (int)(color.g * 255) + "," +
        (int)(color.b * 255) + ", 0.5)";
  }

}
