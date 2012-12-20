package com.mazalearn.scienceengine.domains.electromagnetism.probe;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.ControlPanel;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.guru.AbstractScience2DProber;
import com.mazalearn.scienceengine.guru.ProbeImage;

public class VariablesProber extends AbstractScience2DProber {

  Vector2 points[] = new Vector2[] { new Vector2() };
  private Image image;
  private Table configTable;
  private Actor modelControls;
  private ControlPanel controlPanel;
  
  public VariablesProber(final IScience2DModel science2DModel, final IScience2DView science2DView,
      Skin skin, 
      Actor modelControls, ControlPanel controlPanel, int deltaSuccessScore, int deltaFailureScore) {
    super(science2DModel, science2DView, deltaSuccessScore, deltaFailureScore);
    this.configTable = createConfigTable(science2DModel, skin);
    this.modelControls = modelControls;
    this.controlPanel = controlPanel;
    configTable.setPosition(100, science2DView.getGuru().getHeight() - 100);
    this.addActor(configTable);

    // TODO: a lot of hardcoding here - need to abstract better.
    final Set<String> correctVariables1 = new HashSet<String>();
    for (String configName: new String[] {"BarMagnet Strength", "BarMagnet MovementMode", "PickupCoil Coil Loops"}) {
      correctVariables1.add(configName);
    }
    final Set<String> correctVariables2 = new HashSet<String>();
    for (String configName: new String[] {"PickupCoil Coil Loops",
        "ElectroMagnet Coil Loops", "CurrentSource Max", "CurrentSource Type"}) {
      correctVariables2.add(configName);
    }

    image = new ProbeImage();
    this.addActor(image);
    image.setPosition(650, science2DView.getGuru().getHeight() - 100);
    image.addListener(new ClickListener() {
      @Override
      public void clicked (InputEvent event, float x, float y) {
        Set<String> chosenVariables = new HashSet<String>();
        for (final IModelConfig<?> config: science2DModel.getAllConfigs()) {
          if (config.isPossible() && config.isPermitted()) {
            chosenVariables.add(config.getName());
          }
        }
        science2DView.getGuru().done(correctVariables1.equals(chosenVariables) || correctVariables2.equals(chosenVariables));
      }
    });
  }
  
  private Table createConfigTable(IScience2DModel science2DModel, Skin skin) {
    Table configTable = new Table(skin);
    configTable.setName("Configs");
    refreshConfigsTable(science2DModel, skin, configTable);
    return configTable;
  }

  private void refreshConfigsTable(IScience2DModel science2DModel, Skin skin,
      Table configTable) {
    configTable.clear();
    configTable.add("Choose Variables");
    configTable.row();
    for (final IModelConfig<?> config: science2DModel.getAllConfigs()) {
      if (config.isPossible()) {
        final CheckBox configCheckbox = new CheckBox(config.getName(), skin);
        configTable.add(configCheckbox).left();
        configCheckbox.setChecked(false);
        configCheckbox.addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            config.setPermitted(configCheckbox.isChecked());
            controlPanel.refresh();
          }
        });
        configTable.row();
      }
    }
  }

  @Override
  public void activate(boolean activate) {
    if (activate) {
      // Clear all permitted configs
      for (IModelConfig<?> modelConfig: science2DModel.getAllConfigs()) {
        modelConfig.setPermitted(false);
      }
      controlPanel.refresh();
      image.getColor().a = 0f;
      image.addAction(Actions.fadeIn(60));
    }
    this.setVisible(activate);
    if (modelControls != null) {
      modelControls.setVisible(activate);
    }
  }

  @Override
  public String getGoal() {
    return "Identify variables affecting induced current. Click ? when sure.";
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

}
