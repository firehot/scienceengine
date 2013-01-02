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
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Parameter;
import com.mazalearn.scienceengine.core.view.ControlPanel;
import com.mazalearn.scienceengine.core.view.IScience2DView;

/**
 * Cycles through the eligible registeredTutors - probing the user with each one.
 * 
 * @author sridhar
 * 
 */
public class Guru extends Group implements IDoneCallback {
  private static final int WIN_THRESHOLD = 4000;
  private static final int LOSS_THRESHOLD = -1000;
  
  int tutorIndex = -1;
  ITutor currentTutor;
  protected Dashboard dashboard;
  private List<ITutor> registeredTutors = new ArrayList<ITutor>();
  private List<Actor> excludedActors = new ArrayList<Actor>();
  private final IScience2DView science2DView;
  private final ControlPanel controlPanel;
  private final ConfigGenerator configGenerator;
  private final SoundManager soundManager;
  private final ScoreImage successImage, failureImage;
  private Hinter hinter;
  private int deltaSuccessScore;
  private int deltaFailureScore;
  private float windowWidth;
  private float windowHeight;
  private IScience2DModel science2DModel;

  public Guru(final Skin skin, float width, float height,
      IScience2DView science2DView, IScience2DModel science2dModel, 
      ControlPanel controlPanel) {
    super();
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    dashboard.setPosition(0, 0);
    this.science2DView = science2DView;
    this.science2DModel = science2DModel;
    this.soundManager = ScienceEngine.getSoundManager();
    this.configGenerator = new ConfigGenerator();
    this.controlPanel = controlPanel;
    this.windowWidth = width;
    this.windowHeight = height;
    this.setPosition(0, 0);
    this.setSize(width, height);
     
    this.successImage = new ScoreImage(new Texture("images/greenballoon.png"), skin, true);
    this.failureImage = new ScoreImage(new Texture("images/redballoon.png"), skin, false);
    ((Stage)science2DView).addActor(successImage);
    ((Stage)science2DView).addActor(failureImage);
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
    for (Actor actor: science2DView.getActors()) {
      if (actor.isVisible() && actor != this) {
        excludedActors.add(actor);
      }
    }
    
    if (registeredTutors.size() == 0) { // No guides available
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

    // Turn on access to parts of control panel
    controlPanel.enableControls(true);
    science2DView.done(false);
    ScienceEngine.setProbeMode(false);
    this.setVisible(false);
    // Clear event log
    ScienceEngine.getEventLog().clear();
    // Reload the level.
    // controlPanel.reload();
  }
  
  public List<Actor> getExcludedActors() {
    return this.excludedActors;
  }

  /**
   * IDoneCallback interface implementation - probe is completed
   */
  public void done(boolean success) {
    if (success) {
      if (currentTutor.hasSucceeded()) {
        this.setSize(windowWidth,  windowHeight);
        this.setPosition(0, 0);
        currentTutor.activate(false);
        currentTutor.reinitialize(getX(), getY(), getWidth(), getHeight(), false);
      }
      soundManager.play(ScienceEngineSound.SUCCESS);
      dashboard.addScore(deltaSuccessScore);
      successImage.show(deltaSuccessScore);
      hinter.clearHint();
    } else {
      soundManager.play(ScienceEngineSound.FAILURE);
      // Equate success and failure scores so that 0 progress after second try
      deltaSuccessScore = deltaFailureScore;
      dashboard.addScore(-deltaFailureScore);
      failureImage.show(-deltaFailureScore);
    }
    // Win
    if (dashboard.getScore() >= WIN_THRESHOLD || tutorIndex >= registeredTutors.size()) {
      soundManager.play(ScienceEngineSound.CELEBRATE);
      science2DView.done(true);
      this.setVisible(false);
      return;
    }
    // Loss
    if (dashboard.getScore() <= LOSS_THRESHOLD || currentTutor.hasFailed()) {
      science2DView.done(false);
      this.setVisible(false);
      return;
    }
    if (success) {
      runTutor();
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
    dashboard.setStatus(currentTutor.getGoal());
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

  public void setTitle(String text) {
    dashboard.setStatus(text);
  }

  public void checkProgress() {
    if (currentTutor == null) return;
    currentTutor.checkProgress();
  }
}