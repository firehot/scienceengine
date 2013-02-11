package com.mazalearn.scienceengine.guru;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.Parameter;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.core.view.ViewControls;

/**
 * Cycles through the eligible registeredTutors - probing the user with each one.
 * This is the root of the tutor hierarchy.
 * 
 * @author sridhar
 * 
 */
public class Guru extends Group implements ITutor {
  int tutorIndex = -1;
  ITutor currentTutor;
  protected Dashboard dashboard;
  private List<ITutor> registeredTutors = new ArrayList<ITutor>();
  private List<Actor> excludedActors = new ArrayList<Actor>();
  private final ModelControls modelControls;
  private final ConfigGenerator configGenerator;
  private final SoundManager soundManager;
  private final ScoreImage successImage, failureImage;
  private Hinter hinter;
  private IScience2DController science2DController;
  private ViewControls viewControls;
  private List<String> goals = new ArrayList<String>();
  private final String goal;
  
  public Guru(final Skin skin, IScience2DController science2DController, String goal) {
    super();
    this.science2DController = science2DController;
    this.goal = goal;
    goals.add(goal);
    this.setPosition(0, 0);
    // Guru has no direct user interaction - hence 0 size
    this.setSize(0, 0);
    
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    
    this.soundManager = ScienceEngine.getSoundManager();
    this.configGenerator = new ConfigGenerator();
    this.modelControls = science2DController.getModelControls();
    this.viewControls = science2DController.getViewControls();
     
    this.successImage = new ScoreImage(ScienceEngine.assetManager.get("images/greenballoon.png", Texture.class), skin, true);
    this.failureImage = new ScoreImage(ScienceEngine.assetManager.get("images/redballoon.png", Texture.class), skin, false);
    ((Stage)science2DController.getView()).addActor(successImage);
    ((Stage)science2DController.getView()).addActor(failureImage);
    hinter = new Hinter(skin);
    this.addActor(hinter);
    this.setVisible(false);
  }

  public void registerTutor(AbstractTutor tutor) {
    registeredTutors.add(tutor);
    this.addActor(tutor);
    tutor.activate(false);
  }

  public void startChallenge() {
    // Mark start of challenge in event log
    ScienceEngine.getEventLog().logEvent(ComponentType.Global.name(), 
        Parameter.Challenge.name());
    // Reset scores and bring dashboard to top
    dashboard.resetScore();
    getStage().addActor(this); // ???

    // Collect actors to be excluded from probe points.
    // These are the visible actors.
    excludedActors.clear();
    excludedActors.add(dashboard);
    for (Actor actor: science2DController.getView().getActors()) {
      if (actor.isVisible() && actor != this && !ScreenComponent.Background.name().equals(actor.getName())) {
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
      currentTutor.activate(false);
      currentTutor.reinitialize(false);
      currentTutor = null;
      // Remove all except the activity goal
      goals.clear();
      goals.add(goal);
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

  public void done(boolean success) {
    if (success) {      
      hinter.setHint(null);
      
      // Success and no more tutors == WIN
      if (tutorIndex >= registeredTutors.size() - 1) {
        soundManager.play(ScienceEngineSound.CELEBRATE);
        science2DController.getView().done(true);
        dashboard.clearGoals();
        this.setVisible(false);
        return;
      }
      runTutor();
    } else {
      science2DController.getView().done(false);
      this.setVisible(false);
      return;
    }
  }

  public void doFailure(int score) {
    soundManager.play(ScienceEngineSound.FAILURE);
    dashboard.addScore(-score);
    failureImage.show(-score);
  }

  public void doSuccess(int score) {
    soundManager.play(ScienceEngineSound.SUCCESS);
    dashboard.addScore(score);
    successImage.show(score);
    hinter.clearHint();
  }
  
  @Override
  public void act(float dt) {
    super.act(dt);
    if (Math.round(ScienceEngine.getTime()) % 2 != 0) return;
    if (currentTutor != null) {
      if (!hinter.hasHint()) {
        hinter.setHint(currentTutor.getHint());
      }
    }
  }
  
  @Override
  public String getGoal() {
    return goals.get(goals.size() - 1);
  }

  public void pushGoal(String goal) {
    goals.add(goal);
  }
  
  public void popGoal(String goal) {
    if (goals.size() > 0 && goals.get(goals.size() - 1).equals(goal)) {
      goals.remove(goals.size() - 1);
    }
  }
  
  // Prerequisite: registeredTutors.size() >= 1
  private void runTutor() {
    // Move on to next tutor
    tutorIndex++;
    if (tutorIndex == registeredTutors.size()) {
      done(true);
      return;
    }
    currentTutor = registeredTutors.get(tutorIndex);
    currentTutor.reinitialize(true);
    currentTutor.activate(true);
    dashboard.setGoals(goals);
  }
  
  public void setupProbeConfigs(List<IModelConfig<?>> configs, boolean enableControls) {
    configGenerator.generateConfig(configs);
    modelControls.syncWithModel(); // Force sync with model
    modelControls.refresh();
    // Turn off access to parts of control panel
    modelControls.enableControls(enableControls);
    viewControls.enableControls(enableControls);
  }

  public void checkProgress() {
    if (currentTutor == null) return;
    currentTutor.checkProgress();
  }

  public void reset() {
    if (currentTutor != null) {
      currentTutor.reset();
    }
  }

  @Override
  public void activate(boolean activate) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void reinitialize(boolean probeMode) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getHint() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getSuccessScore() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getFailureScore() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void doSuccessActions() {
    // TODO Auto-generated method stub
    
  }
}