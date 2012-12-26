package com.mazalearn.scienceengine.core.view;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.Controller;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.IParameter;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public class ControlPanel extends Table {
  private final IScience2DModel science2DModel;
  private final Skin skin;
  private List<Controller> controllers = new ArrayList<Controller>();
  private String experimentName;
  private Table modelControlPanel;
  private TextButton title;
  private IScience2DController science2DController;
  
  public ControlPanel(IScience2DController science2DController, String experimentName, Skin skin) {
    super(skin);
    this.skin = skin;
    this.setName("ControlPanel");
    this.science2DController = science2DController;
    this.science2DModel = science2DController.getModel();
    this.experimentName = experimentName;
    this.defaults().fill();
    this.modelControlPanel = createModelControlPanel(skin);
    this.add(modelControlPanel);
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
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
  }
  
  private IParameter asParameter(final Science2DBody body) {
    return new IParameter() {
      @Override
      public String name() {
        return body.getComponentTypeName();
      }      
    };
  }
  
  @SuppressWarnings("rawtypes")
  protected void registerModelConfigs(Table modelControlPanel) {
    this.controllers.clear();
    modelControlPanel.clear();
    final ViewControls viewControls = new ViewControls(science2DController, skin);
    // Register experiment name
    this.title = new TextButton(experimentName, 
        skin.get("body", TextButtonStyle.class));
    title.setName(experimentName); 
    title.setChecked(viewControls.isAvailable());
    title.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        viewControls.setAvailable(!viewControls.isAvailable());
      }
    });
    modelControlPanel.add(title);
    modelControlPanel.row();
    Controller c = Controller.createController(viewControls, modelControlPanel, skin);
    this.controllers.add(c);
    modelControlPanel.row();
    // Register all model controllers
    for (final Science2DBody body: science2DModel.getBodies()) {
      if (!body.isActive() || !body.allowsConfiguration()) continue;
      IModelConfig<?> bodyConfig = new AbstractModelConfig(body, asParameter(body)) {
        @Override public boolean isPossible() { return body.isActive(); }      
        @Override public boolean isAvailable() { return isPossible(); }
        @Override public void doCommand() {
          ScienceEngine.pin(body, !ScienceEngine.isPinned(body));
        }
      };
      this.controllers.add(Controller.createController(bodyConfig, modelControlPanel, skin, "body"));
      for (IModelConfig modelConfig: body.getConfigs()) {
        this.controllers.add(Controller.createController(modelConfig, modelControlPanel, skin));
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