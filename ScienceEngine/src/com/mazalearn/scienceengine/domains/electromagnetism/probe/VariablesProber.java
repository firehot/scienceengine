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
import com.mazalearn.scienceengine.core.probe.AbstractScience2DProber;
import com.mazalearn.scienceengine.core.probe.ProbeImage;
import com.mazalearn.scienceengine.core.probe.ProbeManager;
import com.mazalearn.scienceengine.core.view.ControlPanel;
import com.mazalearn.scienceengine.domains.electromagnetism.view.LightbulbActor;

public class VariablesProber extends AbstractScience2DProber {

  Vector2 points[] = new Vector2[] { new Vector2() };
  private Image image;
  private LightbulbActor lightbulbActor;
  private String[] hints = {
      "Light intensity increases when more current is induced in the coil.",
      "More current is induced in the coil if the magnetic field changes faster at the coil.",
      "Magnetic field change at the coil increases when the magnet moves faster relative to the coil.",
      "If the coil has more loops, more current will be induced.",
      "If the magnet is stronger, more current will be induced."
  };
  private Table configTable;
  private Actor modelControls;
  private ControlPanel controlPanel;
  private IScience2DModel science2DModel;
  
  public VariablesProber(final ProbeManager probeManager, final IScience2DModel science2DModel, Skin skin, 
      Actor modelControls, ControlPanel controlPanel) {
    super(probeManager);
    this.configTable = createConfigTable(science2DModel, skin);
    this.modelControls = modelControls;
    this.controlPanel = controlPanel;
    this.science2DModel = science2DModel;
    configTable.setPosition(100, probeManager.getHeight() - 100);
    this.addActor(configTable);

    final Set<String> correctVariables1 = new HashSet<String>();
    for (String configName: new String[] {"BarMagnet Strength", "BarMagnet Mode", "PickupCoil Coil Loops"}) {
      correctVariables1.add(configName);
    }
    final Set<String> correctVariables2 = new HashSet<String>();
    for (String configName: new String[] {"PickupCoil Coil Loops",
        "ElectroMagnet Coil Loops", "CurrentSource Max", "CurrentSource Type"}) {
      correctVariables2.add(configName);
    }

    image = new ProbeImage();
    this.addActor(image);
    image.setPosition(650, probeManager.getHeight() - 100);
    image.addListener(new ClickListener() {
      @Override
      public void clicked (InputEvent event, float x, float y) {
        Set<String> chosenVariables = new HashSet<String>();
        for (final IModelConfig<?> config: science2DModel.getAllConfigs()) {
          if (config.isPossible() && config.isPermitted()) {
            chosenVariables.add(config.getName());
          }
        }
        probeManager.done(correctVariables1.equals(chosenVariables) || correctVariables2.equals(chosenVariables));
      }
    });
    this.lightbulbActor = (LightbulbActor) probeManager.findStageActor("Lightbulb");
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
  public boolean isAvailable() {
    return lightbulbActor != null && lightbulbActor.isVisible();
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
  public String getTitle() {
    return "Identify variables affecting induced current. Click ? when sure.";
  }

  @Override
  public String[] getHints() {
    return hints;
  }

  public int getDeltaSuccessScore() {
    return 1000;
  }
  
  public int getSubsequentDeltaSuccessScore() {
    return 1000;
  }
  
  public int getDeltaFailureScore() {
    return -10;
  }
}
