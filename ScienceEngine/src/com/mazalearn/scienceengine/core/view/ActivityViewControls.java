package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.CommandButtonControl;
import com.mazalearn.scienceengine.core.controller.IControl;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.controller.ToggleButtonControl;
import com.mazalearn.scienceengine.core.model.Parameter;

public class ActivityViewControls extends ViewControls {
  private final IScience2DView science2DView;
  private IControl suspendControl;
  private IControl resetControl;
  private IScience2DController science2DController;
  
  public ActivityViewControls(IScience2DController science2DController, Skin skin) {
    super(skin);
    this.science2DController = science2DController;
    this.science2DView = science2DController.getView();
    addActivityControls();
  }
  
  private IMessage getMsg() {
    return messages;
  }
  
  private void addActivityControls() {
    // Add pause/resume functionality for the activity
    AbstractModelConfig<Boolean> pauseResumeModelConfig = 
        new AbstractModelConfig<Boolean>(null, Parameter.PauseResume) { //$NON-NLS-1$ //$NON-NLS-2$
          public void setValue(Boolean value) { setActivated(false); science2DView.suspend(value); }
          public Boolean getValue() { return science2DView.isSuspended(); }
          public boolean isPossible() { return !ScienceEngine.isProbeMode(); }
    };
    suspendControl = new ToggleButtonControl(pauseResumeModelConfig, skin, "body") {
      public void syncWithModel() {
        super.syncWithModel();
        toggleButton.setText(science2DView.isSuspended() ? 
            getMsg().getString("ViewControls.Resume") : 
              getMsg().getString("ViewControls.Pause")); //$NON-NLS-1$ //$NON-NLS-2$
      }
    };

    // Add reset functionality for the activity
    AbstractModelConfig<String> resetModelConfig = 
        new AbstractModelConfig<String>(null, Parameter.Reset) { //$NON-NLS-1$ //$NON-NLS-2$
          public void doCommand() { setActivated(false); science2DController.reset(); }
          public boolean isPossible() { return !ScienceEngine.isProbeMode(); }
    };
    resetControl = new CommandButtonControl(resetModelConfig, skin, "body");

    // Add snapshot functionality
    Button snapshotButton = new TextButton(
        getMsg().getString("ScienceEngine.Snapshot"), skin, "body");
    snapshotButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x1, float y1) {
        setActivated(false);
        takeSnapshot();
      }
    });

    viewControlPanel.add(resetControl.getActor());
    viewControlPanel.row();
    viewControlPanel.add(suspendControl.getActor());
    viewControlPanel.row();
    if (ScienceEngine.DEV_MODE != ScienceEngine.DevMode.PRODUCTION) {
      viewControlPanel.add(snapshotButton);
      viewControlPanel.row();
    }
    viewControlPanel.row();
    syncWithModel();
  }

  private void takeSnapshot() {
    final Actor snapshotter = new Snapshotter();
    snapshotter.addListener(new DragListener() {
      public void drag (InputEvent event, float x, float y, int pointer) {
        snapshotter.setPosition(Math.min(getTouchDownX(), event.getStageX()),
            Math.min(getTouchDownY(), event.getStageY()));
        snapshotter.setSize(Math.abs(getTouchDownX() - event.getStageX()), 
            Math.abs(getTouchDownY() - event.getStageY()));
      }

      public void dragStop (InputEvent event, float x, float y, int pointer) {
        getStage().getRoot().removeActor(snapshotter);
        ScienceEngine.getPlatformAdapter().takeSnapshot(getStage(), 
            science2DController.getTopic(), science2DController.getLevel(),
            (int)snapshotter.getX(), (int)snapshotter.getY(), 
            (int)snapshotter.getWidth(), (int)snapshotter.getHeight());
      }
    });
    getStage().addActor(snapshotter);
  }
  public void syncWithModel() {
    super.syncWithModel();
    suspendControl.syncWithModel();
  }
  
  public void enableControls(boolean enable) {
    suspendControl.getActor().setVisible(enable);
    resetControl.getActor().setVisible(enable);
    super.enableControls(enable);
  }

  @Override
  public Actor getActor() {
    return this;
  }
}