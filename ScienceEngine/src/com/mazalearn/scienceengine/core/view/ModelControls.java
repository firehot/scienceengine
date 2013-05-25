package com.mazalearn.scienceengine.core.view;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.Controller;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.IParameter;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public class ModelControls extends Table {
  private final IScience2DModel science2DModel;
  private final Skin skin;
  private List<Controller> controllers = new ArrayList<Controller>();
  private Table modelControlPanel;
  private TextButton title;
  
  public ModelControls(IScience2DModel science2DModel, Skin skin) {
    super(skin);
    this.skin = skin;
    this.setName("ModelControls");
    this.science2DModel = science2DModel;
    this.defaults().fill();
    this.modelControlPanel = createModelControlPanel(skin);
    this.add(modelControlPanel);
    if ((ScienceEngine.DEV_MODE & DevMode.DEBUG) != 0) {
      debug();
    }
  }
  
  public Table createModelControlPanel(Skin skin) {
    Table modelControlPanel = new Table(skin);
    modelControlPanel.setName("ModelControls");
    modelControlPanel.defaults().fill();
    registerModelConfigs(modelControlPanel);
    return modelControlPanel;
  }
  
  public void refresh() {
    registerModelConfigs(modelControlPanel);
    this.invalidate();
    this.validate();
  }
  
  private IParameter asParameter(final Science2DBody body) {
    return new IParameter() {
      @Override
      public String name() {
        return body.name();
      }
      @Override
      public String toString() {
        return body.toString();
      }
    };
  }
  
  @Override
  public void validate() {
    super.validate();
    // Periodically reposition
    if (ScienceEngine.getLogicalTime() % 10 != 0) return;
    ScreenComponent sc = ScreenComponent.ModelControls; // Gadzooks
    setPosition(sc.getX(getPrefWidth()) + getPrefWidth() / 2,
        sc.getY(getPrefHeight()) + getPrefHeight() / 2);
  }
  
  @SuppressWarnings("rawtypes")
  protected void registerModelConfigs(Table modelControlPanel) {
    this.controllers.clear();
    modelControlPanel.clear();
    // Register all model controllers
    for (final Science2DBody body: science2DModel.getBodies()) {
      if (body.allowsConfiguration()) {
        // Only include bodies which have at least 1 permitted config.
        boolean bodyHasConfigs = false;
        for (IModelConfig modelConfig: body.getConfigs()) {
          // Meters should not be connected to GUI controls automatically
          if (!modelConfig.isMeter() && modelConfig.isPermitted()) {
            bodyHasConfigs = true;
            break;
          }
        }
        if (!bodyHasConfigs) continue;
        
        IModelConfig<?> bodyConfig = new AbstractModelConfig<Boolean>(body, asParameter(body), false) {
          @Override public boolean isPossible() { return body.isActive(); }      
          @Override public boolean isAvailable() { return isPossible() && (!ScienceEngine.isProbeMode() || ScienceEngine.isPinned(body)); }
          @Override public Boolean getValue() { return ScienceEngine.isPinned(body); }
          @Override public void setValue(Boolean value) { ScienceEngine.pin(body, value); }
        };
        Controller bodyController = Controller.createController(bodyConfig, modelControlPanel, skin, "body");
        this.controllers.add(bodyController);
        // Indent controls for body a little to the right.
        Table bodyControlPanel = new Table(skin);
        bodyControlPanel.setName(body.name());
        modelControlPanel.add(bodyControlPanel).padLeft(15);
        modelControlPanel.row();
        for (IModelConfig modelConfig: body.getConfigs()) {
          if (!modelConfig.isMeter()) { // Meters should not be connected to GUI controls automatically
            this.controllers.add(Controller.createController(modelConfig, bodyControlPanel, skin));
          }
        }
      }
    }
  }

  public Actor getTitle() {
    return this.title;
  }
  
  @Override
  public void act(float delta) {
    syncWithModel();
    super.act(delta);
    this.invalidate();
    this.validate();
  }

  public void syncWithModel() {
    for (Controller controller: controllers) {
       controller.validate();
    }
  }
  
  public void enableControls(boolean enable) {
    modelControlPanel.setTouchable(enable ? Touchable.enabled : Touchable.disabled);
    this.invalidate();
  }
}