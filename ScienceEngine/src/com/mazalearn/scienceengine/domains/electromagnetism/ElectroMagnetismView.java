package com.mazalearn.scienceengine.domains.electromagnetism;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.utils.Net;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.AbstractScience2DModel;
import com.mazalearn.scienceengine.core.model.ICurrent.CircuitElement;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Dynamo;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Magnet;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CircuitActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.DrawingActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.ScienceTrainActor;

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
    // TODO: Make below part of scripting language
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
    dynamo = (Dynamo) science2DModel.findBody(ComponentType.Dynamo);
    magnet = (Magnet) science2DModel.findBody(ComponentType.Magnet);
    lightbulb = (Lightbulb) science2DModel.findBody(ComponentType.Lightbulb);
    for (List<CircuitElement> circuit: science2DModel.getCircuits()) {
      this.addActor(new CircuitActor(circuit));
    }
    super.prepareView();
  }
  
  @Override
  public void initializeCommands(List<IModelConfig<?>> commands) {
    super.initializeCommands(commands);
    commands.add(new AbstractModelConfig<String>("Upload") { //$NON-NLS-1$ //$NON-NLS-2$
      public void doCommand() { uploadToServer(); }
      public boolean isPossible() { return true; }
    });
  }
  
  private void uploadToServer() {
    float current = Math.round(dynamo.getMaxCurrent());
    Color color = lightbulb.getColor();

    // Set light to display on coach drawing
    DrawingActor drawingActor = (DrawingActor) findActor(ComponentType.Drawing.name());
    drawingActor.getCoach().setLight(current, lightbulb.getColor());
    
    // Post drawing, color and current to server
    Map<String, String> postParams = new HashMap<String, String>();
    postParams.put("User", ScienceEngine.getUserEmail());
    postParams.put("UserName", ScienceEngine.getUserName());
    postParams.put("Current", String.valueOf(current));
    postParams.put("Color", color.toString());
    try {
      Net.httpPost("/upload", "application/octet-stream", postParams, drawingActor.getDrawingPng());
      ScienceEngine.displayStatusMessage(this, "Uploaded to MazaLearn - See www.mazalearn.com/train.html");
    } catch(GdxRuntimeException e) {
      e.printStackTrace();
      ScienceEngine.displayStatusMessage(this, "Network Problem: Failed to upload to MazaLearn");
    }
    
    // Show only the main actors - ScienceTrain and control related
    List<String> actorsToBeHidden = 
        Arrays.asList(new String[]{ComponentType.Dynamo.name(),  ComponentType.Magnet.name(), 
            CircuitActor.COMPONENT_TYPE, ComponentType.Drawing.name(), 
            ComponentType.Lightbulb.name()});
    for (Actor actor: getActors()) {
      String name = actor.getName();
      if (actorsToBeHidden.contains(name)) {
        actor.setVisible(false);
      }
      if (ComponentType.ScienceTrain.name().equals(name)) {
        ((ScienceTrainActor) actor).animate();
      }
    }
  }

}
