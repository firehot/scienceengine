package com.mazalearn.scienceengine.guru;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.Parameter;
import com.mazalearn.scienceengine.core.view.ControlPanel;

/**
 * Cycles through the eligible registeredTutors - probing the user with each one.
 * 
 * @author sridhar
 * 
 */
public class Guru extends Group implements IDoneCallback {  
  int tutorIndex = -1;
  ITutor currentTutor;
  protected Dashboard dashboard;
  private List<ITutor> registeredTutors = new ArrayList<ITutor>();
  private List<Actor> excludedActors = new ArrayList<Actor>();
  private final ControlPanel controlPanel;
  private final ConfigGenerator configGenerator;
  private final SoundManager soundManager;
  private final ScoreImage successImage, failureImage;
  private Hinter hinter;
  private int deltaSuccessScore;
  private int deltaFailureScore;
  private float windowWidth;
  private float windowHeight;
  private IScience2DController science2DController;
  
  public Guru(final Skin skin, float width, float height,
      IScience2DController science2DController, 
      ControlPanel controlPanel) {
    super();
    this.science2DController = science2DController;
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    dashboard.setPosition(0, 0);
    this.soundManager = ScienceEngine.getSoundManager();
    this.configGenerator = new ConfigGenerator();
    this.controlPanel = controlPanel;
    this.windowWidth = width;
    this.windowHeight = height;
    this.setPosition(0, 0);
    this.setSize(width, height);
     
    this.successImage = new ScoreImage(new Texture("images/greenballoon.png"), skin, true);
    this.failureImage = new ScoreImage(new Texture("images/redballoon.png"), skin, false);
    ((Stage)science2DController.getView()).addActor(successImage);
    ((Stage)science2DController.getView()).addActor(failureImage);
    hinter = new Hinter(skin);
    this.setVisible(false);
  }

  public void registerTutor(AbstractTutor tutor) {
    registeredTutors.add(tutor);
    this.addActor(tutor);
    // Move hinter to top
    this.addActor(hinter);
    tutor.activate(false);
  }

  public void startChallenge() {
    // Mark start of challenge in event log
    ScienceEngine.getEventLog().logEvent(ComponentType.Global.name(), 
        Parameter.Challenge.name());
    // Reset scores
    dashboard.resetScore();
    
    // Collect actors to be excluded from probe points.
    // These are the visible actors.
    excludedActors.clear();
    excludedActors.add(dashboard);
    for (Actor actor: science2DController.getView().getActors()) {
      if (actor.isVisible() && actor != this) {
        excludedActors.add(actor);
      }
    }
    
    if (registeredTutors.size() == 0) { // No tutors available
      endChallenge();
      return;
    }
    
    this.setVisible(true);
    tutorIndex = -1;
    runTutor();
  }
  
  public void endChallenge() {
    // Reinitialize current prober, if any
    if (currentTutor != null) {
      currentTutor.reinitialize(getX(), getY(), windowWidth, windowHeight, false);
    }

    science2DController.getView().done(false);
    ScienceEngine.setProbeMode(false);
    this.setVisible(false);
    // Clear event log
    ScienceEngine.getEventLog().clear();
  }
  
  public List<Actor> getExcludedActors() {
    return this.excludedActors;
  }

  /**
   * IDoneCallback interface implementation - probe is completed
   */
  public void done(boolean success) {
    if (success) {
      soundManager.play(ScienceEngineSound.SUCCESS);
      dashboard.addScore(deltaSuccessScore);
      successImage.show(deltaSuccessScore);
      hinter.clearHint();
      
      if (currentTutor.hasSucceeded()) {
        this.setSize(windowWidth,  windowHeight);
        this.setPosition(0, 0);
        currentTutor.doSuccessActions();
        currentTutor.activate(false);
        currentTutor.reinitialize(getX(), getY(), getWidth(), getHeight(), false);
        
        // Success and no more tutors == WIN
        if (tutorIndex >= registeredTutors.size() - 1) {
          soundManager.play(ScienceEngineSound.CELEBRATE);
          science2DController.getView().done(true);
          this.setVisible(false);
          return;
        }
      }
      runTutor();
    } else {
      soundManager.play(ScienceEngineSound.FAILURE);
      // Equate success and failure scores so that 0 progress after second try
      deltaSuccessScore = deltaFailureScore;
      dashboard.addScore(-deltaFailureScore);
      failureImage.show(-deltaFailureScore);
      // Loss
      if (currentTutor.hasFailed()) {
        science2DController.getView().done(false);
        this.setVisible(false);
        return;
      }
    }
  }
  
  @Override
  public void act(float dt) {
    super.act(dt);
    if (Math.round(ScienceEngine.getTime()) % 2 != 0) return;
    if (currentTutor != null) {
      // Place hinter to right of dashboard above the controls
      hinter.setPosition(controlPanel.getX(), windowHeight - getY() - 50);
      if (!hinter.hasHint()) {
        hinter.setHint(currentTutor.getHint());
      }
    }
  }
  
  // Prerequisite: registeredTutors.size() >= 1
  private void runTutor() {
    // If a valid tutor is already running, let it continue
    if (currentTutor != null && !currentTutor.hasSucceeded()) {
      currentTutor.activate(true);
      return;
    }
    
    // Move on to next tutor
    tutorIndex++;
    if (tutorIndex == registeredTutors.size()) {
      done(true);
      return;
    }
    currentTutor = registeredTutors.get(tutorIndex);
    currentTutor.reinitialize(getX(), getY(), windowWidth, windowHeight, true);
    currentTutor.activate(true);
    dashboard.setGoal(currentTutor.getGoal());
    // Set up initial success and failure scores
    deltaSuccessScore = currentTutor.getSuccessScore();
    deltaFailureScore = currentTutor.getFailureScore();
  }
  
  public void setupProbeConfigs(List<IModelConfig<?>> configs, boolean enableControls) {
    configGenerator.generateConfig(configs);
    controlPanel.syncWithModel(); // Force sync with model
    controlPanel.refresh();
    // Turn off access to parts of control panel
    controlPanel.enableControls(enableControls);
  }

  public void setGoal(String text) {
    dashboard.setGoal(text);
  }

  public void checkProgress() {
    if (currentTutor == null) return;
    currentTutor.checkProgress();
  }
}