package com.mazalearn.scienceengine.guru;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.AggregatorFunction;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.lang.IFunction;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.Parameter;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.core.view.ViewControls;

/**
 * Cycles through the eligible childTutors - probing the user with each one.
 * This is the root of the tutor hierarchy.
 * 
 * @author sridhar
 * 
 */
public class Guru extends Group implements ITutor {
  int tutorIndex = -1;
  ITutor currentTutor;
  protected Dashboard dashboard;
  private TimeTracker activeTimer;
  private List<ITutor> childTutors = new ArrayList<ITutor>();
  private List<Actor> excludedActors = new ArrayList<Actor>();
  private final ModelControls modelControls;
  private final ConfigGenerator configGenerator;
  private final SoundManager soundManager;
  private final ScoreImage correctImage, wrongImage;
  SuccessFailureImage successImage, failureImage;
  private Hinter hinter;
  private IScience2DController science2DController;
  private ViewControls viewControls;
  private final String goal;
  private Skin skin;
  private ITutor activeTutor;
  
  public Guru(final Skin skin, IScience2DController science2DController, String goal) {
    super();
    this.science2DController = science2DController;
    this.goal = goal;
    this.skin = skin;
    this.setPosition(0, 0);
    // Guru has no direct user interaction - hence 0 size
    this.setSize(0, 0);
    
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    
    this.soundManager = ScienceEngine.getSoundManager();
    this.configGenerator = new ConfigGenerator();
    this.modelControls = science2DController.getModelControls();
    this.viewControls = science2DController.getViewControls();
     
    this.successImage = new SuccessFailureImage(ScienceEngine.assetManager.get("images/greenballoon.png", Texture.class), skin, true);
    this.failureImage = new SuccessFailureImage(ScienceEngine.assetManager.get("images/redballoon.png", Texture.class), skin, false);
    this.correctImage = new ScoreImage(ScienceEngine.assetManager.get("images/check.png", Texture.class), skin);
    this.wrongImage = new ScoreImage(ScienceEngine.assetManager.get("images/cross.png", Texture.class), skin);
    
    ((Stage)science2DController.getView()).addActor(successImage);
    ((Stage)science2DController.getView()).addActor(failureImage);
    ((Stage)science2DController.getView()).addActor(correctImage);
    ((Stage)science2DController.getView()).addActor(wrongImage);
    
    hinter = new Hinter(skin);
    this.addActor(hinter);
    
    activeTimer = new TimeTracker(this, "0", skin);
    activeTimer.setPosition(5, 5);
    this.addActor(activeTimer);

    this.setVisible(false);
    activeTutor = this;
  }
  
  public ITutor getActiveTutor() {
    return activeTutor;
  }

  public float getActiveTime() {
    return activeTimer.getActiveTime();
  }

  public void initialize(List<ITutor> childTutors) {
    this.childTutors = childTutors;
    for (ITutor childTutor: childTutors) {
      this.addActor((AbstractTutor) childTutor);
    }
    List<ITutor> subgoals = new ArrayList<ITutor>();
    collectSubgoals(this, subgoals);
    
    SubgoalNavigator subgoalNavigator = new SubgoalNavigator(subgoals, this, skin);
    this.getStage().addActor(subgoalNavigator);    
    dashboard.setSubgoalNavigator(subgoalNavigator);
    
    this.tutorIndex = 0;
    this.currentTutor = childTutors.get(0);
  }
  
  private void collectSubgoals(ITutor tutor, List<ITutor> subgoals) {
    if (tutor.getGroupType() == GroupType.None) { 
      subgoals.add(tutor);
      return;
    }
    for (ITutor child: tutor.getChildTutors()) {
      collectSubgoals(child, subgoals);
    }
  }
  
  public void startChallenge() {
    // Mark start of challenge in event log
    ScienceEngine.getEventLog().logEvent(ComponentType.Global.name(), 
        Parameter.Challenge.name());
    // Reset scores and bring dashboard to top
    dashboard.resetScore();
    getStage().addActor(this); // bring Guru to top

    // Collect actors to be excluded from probe points.
    // These are the visible actors.
    excludedActors.clear();
    excludedActors.add(dashboard);
    for (Actor actor: science2DController.getView().getActors()) {
      if (actor.isVisible() && actor != this && !ScreenComponent.Background.name().equals(actor.getName())) {
        excludedActors.add(actor);
      }
    }
    
    if (childTutors.size() == 0) { // No activeTutors available
      endChallenge();
      return;
    }
    
    prepareToTeach(null);
    teach();
  }
  
  public void endChallenge() {
    // Reinitialize current prober, if any
    if (currentTutor != null) {
      currentTutor = null;
      setActiveTutor(this);
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

  public void showWrong(int score) {
    soundManager.play(ScienceEngineSound.FAILURE);
    dashboard.addScore(-score);
    wrongImage.show(String.valueOf(-score));
  }
  
  public void showFailure(String message) {
    soundManager.play(ScienceEngineSound.FAILURE);
    failureImage.show(message);
  }

  public void showCorrect(int score) {
    soundManager.play(ScienceEngineSound.SUCCESS);
    dashboard.addScore(score);
    correctImage.show(String.valueOf(score));
    hinter.clearHint();
  }
  
  public void showSuccess(String message) {
    soundManager.play(ScienceEngineSound.SUCCESS);
    successImage.show(message);
    hinter.clearHint();
  }
  
  public void setActiveTutor(ITutor activeTutor) {
    this.activeTutor = activeTutor;
    dashboard.setActiveTutor(activeTutor);
    hinter.clearHint();
  }
  
  @Override
  public void act(float dt) {
    super.act(dt);
    if (Math.round(ScienceEngine.getTime()) % 2 != 0) return;
    if (currentTutor != null) {
      if (!hinter.hasHint()) {
        hinter.setHint(activeTutor.getHint());
      }
    }
  }
  
  @Override
  public String getGoal() {
    return goal;
  }

  // TODO: Guide and Guru both handle many children tutors - abstract this out
  @Override
  public void done(boolean success) {
    hinter.setHint(null);
    if (!success) {
      science2DController.getView().done(false);
      this.setVisible(false);
      return;
    }
    // Success and no more childTutors == WIN
    if (++tutorIndex == childTutors.size()) {
      soundManager.play(ScienceEngineSound.CELEBRATE);
      science2DController.getView().done(true);
      dashboard.clearActiveTutor();
      this.setVisible(false);
      return;
    }
    currentTutor = childTutors.get(tutorIndex);
    teach();
  }

  // Prerequisite: childTutors.size() >= 1
  @Override
  public void teach() {
    this.setVisible(true);
    currentTutor.prepareToTeach(null);
    if (currentTutor.getGroupType() == GroupType.Challenge) {
      doChallengeAnimation(currentTutor);
    } else {
      currentTutor.teach();
    }
  }
  
  private void doChallengeAnimation(final ITutor tutor) {
    final LabelStyle large = new LabelStyle(skin.get(LabelStyle.class));
    BitmapFont font = skin.getFont("font26");
    font.setScale(4);
    large.font = font;
    final TextButton start = new TextButton("Challenge\nRound", skin);
    start.setSize(500, 300);
    start.setColor(Color.YELLOW);
    start.getLabel().setStyle(large);
    start.setPosition(ScreenComponent.VIEWPORT_WIDTH / 2 - start.getWidth() / 2,
        ScreenComponent.VIEWPORT_HEIGHT / 2 - start.getHeight() / 2);
    this.addActor(start);
    start.addListener(new ClickListener() {
      @Override
      public void clicked (InputEvent event, float x, float y) {
        start.setText("3");
        start.setSize(100, 100);
        start.setPosition(ScreenComponent.VIEWPORT_WIDTH / 2 - start.getWidth() / 2,
            ScreenComponent.VIEWPORT_HEIGHT / 2 - start.getHeight() / 2);
        start.addAction(
            Actions.sequence(
                Actions.repeat(3, 
                    Actions.sequence(
                        Actions.alpha(1),
                        Actions.alpha(0, 1),
                        new Action() {
                          @Override
                          public boolean act(float delta) {
                            start.setText(String.valueOf(Integer.parseInt(start.getText().toString()) - 1));
                            return true;
                          }
                        }
                  )),
                  new Action() {
                    @Override
                    public boolean act(float delta) {
                      Guru.this.removeActor(start);
                      tutor.teach();
                      return true;
                    }
                  }));
      }
    });
  }

  @Override
  public void prepareToTeach(ITutor childTutor) {
    this.setVisible(false);
    if (childTutor != null) {
      tutorIndex = childTutors.indexOf(childTutor);
    }
    if (tutorIndex < 0 || tutorIndex >= childTutors.size()) {
      tutorIndex = 0;
    }
    currentTutor = childTutors.get(tutorIndex);
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

  @Override
  public String getHint() {
    return null;
  }
  
  @Override
  public String getId() {
    return "Guru";
  }
  
  @Override
  public GroupType getGroupType() {
    return GroupType.Root;
  }
  
  @Override
  public List<ITutor> getChildTutors() {
    return childTutors;
  }
  
  public Parser createParser() {
    Parser parser = new Parser();
    Map<String, IFunction.A0> functions0 = new HashMap<String, IFunction.A0>();
    Map<String, IFunction.A1> functions1 = new HashMap<String, IFunction.A1>();
    Map<String, IFunction.A2> functions2 = new HashMap<String, IFunction.A2>();

    for (AggregatorFunction aggregatorFunction: AggregatorFunction.values()) {
      functions1.put(aggregatorFunction.name(), aggregatorFunction);
    }

    for (final IModelConfig<?> command: science2DController.getView().getCommands()) {
      functions0.put(command.getName(), new IFunction.A0() {
         @Override
         public float eval() { command.doCommand(); return 0; }
      });
    }

    parser.allowFunctions(functions0, functions1, functions2);
    return parser;
  }

  public void goTo(ITutor subgoal) {
    activeTutor.prepareToTeach(null); // Disable active Tutor
    ScienceEngine.setProbeMode(false);
    setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), true);
    // ??? why above series ??? 
    prepareTutors(subgoal);
    subgoal.prepareToTeach(null);
    teach();
  }

  private void prepareTutors(ITutor subgoal) {
    if (subgoal.getParentTutor() != null) {
      prepareTutors(subgoal.getParentTutor());
      subgoal.getParentTutor().prepareToTeach(subgoal);
    }
  }
  
  @Override
  public ITutor getParentTutor() {
    return null;
  }
}