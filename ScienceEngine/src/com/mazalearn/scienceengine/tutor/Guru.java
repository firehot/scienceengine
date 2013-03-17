package com.mazalearn.scienceengine.tutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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
import com.mazalearn.scienceengine.core.view.Scoreboard;
import com.mazalearn.scienceengine.core.view.ViewControls;
/**
 * Root of the tutor hierarchy, handles all the tutors under management of a root tutor.
 * Provides various services to the tutors - showing success, failure, correct, wrong
 * group animations for challenge, rapidfire.
 * Provides a TutorNavigator service for random access to tutors.
 * 
 * @author sridhar
 * 
 */
public class Guru extends Group implements ITutor {
  public static final String ID = "Guru";
  public static final String ROOT_ID = "Root";
  protected Scoreboard scoreboard;
  private TimeTracker timeTracker;
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
  private TutorGroup rootTutor;
  private TutorNavigator tutorNavigator;
  private McqActor mcqActor;
  private TutorStats stats;
  
  public Guru(final Skin skin, IScience2DController science2DController, String goal) {
    super();
    this.setName(ScreenComponent.TUTOR_GROUP);
    this.science2DController = science2DController;
    this.goal = goal;
    this.skin = skin;
    this.setPosition(0, 0);
    // Guru has no direct user interaction - hence 0 size
    this.setSize(0, 0);
    
    this.stats = new TutorStats(ID);
    
    this.soundManager = ScienceEngine.getSoundManager();
    this.configGenerator = new ConfigGenerator();
    this.modelControls = science2DController.getModelControls();
    this.viewControls = science2DController.getViewControls();
    
    this.successImage = new SuccessFailureImage(ScienceEngine.getTextureRegion("greenballoon"), skin, true);
    this.failureImage = new SuccessFailureImage(ScienceEngine.getTextureRegion("redballoon"), skin, false);
    this.correctImage = new ScoreImage(ScienceEngine.getTextureRegion("check"), skin);
    this.wrongImage = new ScoreImage(ScienceEngine.getTextureRegion("cross"), skin);
    
    this.addActor(successImage);
    this.addActor(failureImage);
    this.addActor(correctImage);
    this.addActor(wrongImage);
    
    hinter = new Hinter(skin);
    this.addActor(hinter);
    
    this.scoreboard = (Scoreboard) science2DController.getView().findActor(ScreenComponent.Scoreboard.name());;
    timeTracker = (TimeTracker) science2DController.getView().findActor(ScreenComponent.TimeTracker.name());
    this.setVisible(false);
    activeTutor = this;
    timeTracker.setActiveTutor(this);
  }
  
  public ITutor getActiveTutor() {
    return activeTutor;
  }

  public ITutor getRootTutor() {
    if (rootTutor == null) {
      this.rootTutor = new TutorGroup(science2DController, TutorType.Guide, this, goal, ROOT_ID,
          null, null, 0, 0, null);
      this.addActor(rootTutor);      
    }
    return rootTutor;
  }
  
  public void initialize(List<ITutor> childTutors) {
    rootTutor.initialize(childTutors, null);
    List<ITutor> tutors = new ArrayList<ITutor>();
    Set<String> tutorIds = new HashSet<String>();
    collectLeafTutors(rootTutor, tutors, tutorIds);
    
    tutorNavigator = new TutorNavigator(tutors, this, skin);
    this.addActor(tutorNavigator);    
  }
  
  private void collectLeafTutors(ITutor tutor, List<ITutor> tutors, Set<String> tutorIds) {
    if (tutor.getChildTutors() == null) { 
      if (tutorIds.contains(tutor.getId())) {
        Gdx.app.error(ScienceEngine.LOG, "Duplicate Tutor ID: " + tutor.getId());
      }
      tutors.add(tutor);
      tutorIds.add(tutor.getId());
      return;
    }
    for (ITutor child: tutor.getChildTutors()) {
      collectLeafTutors(child, tutors, tutorIds);
    }
  }
  
  public void beginTutoring() {
    Gdx.app.log(ScienceEngine.LOG, "Start Tutoring: " + getId());
    // Mark start of Tutoring in event log
    ScienceEngine.getEventLog().logEvent(ComponentType.Global.name(), 
        Parameter.Tutoring.name());
    scoreboard.resetScore();
    // bring Guru to top
    Group root = getStage().getRoot();
    root.addActorBefore(root.findActor(ScreenComponent.CORE_GROUP), this);

    // Collect visible actors to be excluded from probe points.
    excludedActors.clear();
    excludedActors.add(scoreboard);
    for (Actor actor: science2DController.getView().getActors()) {
      // actor is visible and does not span entire screen
      if (actor.isVisible() && (actor.hit(0, 0, true) == null || 
          actor.hit(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT, true) == null)) {
        excludedActors.add(actor);
      }
    }
    
    if (rootTutor.getChildTutors().size() == 0) { // No activeTutors available
      endTutoring();
      return;
    }
    
    prepareToTeach(null);
    teach();
  }
  
  public void endTutoring() {
    Gdx.app.log(ScienceEngine.LOG, "End Tutoring: " + getId());
    // Reinitialize current prober, if any
    activeTutor.finish();
 
    setActiveTutor(this);
    
    ScienceEngine.setProbeMode(false);
    // Clear event log
    ScienceEngine.getEventLog().clear();
  }
  
  public List<Actor> getExcludedActors() {
    return this.excludedActors;
  }

  public void showWrong(int score) {
    addActor(wrongImage); // Bring to top
    soundManager.play(ScienceEngineSound.FAILURE);
    scoreboard.addScore(-score);
    wrongImage.show(String.valueOf(-score));
  }
  
  public void showFailure(int score) {
    addActor(failureImage); // bring to top
    soundManager.play(ScienceEngineSound.FAILURE);
    //scoreboard.addScore(-score);
    failureImage.show("Oops!");
  }

  public void showCorrect(int score) {
    addActor(correctImage); // bring to top
    soundManager.play(ScienceEngineSound.SUCCESS);
    scoreboard.addScore(score);
    correctImage.show(String.valueOf(score));
    hinter.clearHint();
  }
  
  public void showSuccess(int score) {
    addActor(successImage); // bring to top
    soundManager.play(ScienceEngineSound.SUCCESS);
    scoreboard.addScore(score);
    successImage.show(String.valueOf(score));
    hinter.clearHint();
  }
  
  public void setActiveTutor(ITutor activeTutor) {
    this.activeTutor = activeTutor;
    tutorNavigator.setActiveTutor(activeTutor);
    timeTracker.setActiveTutor(activeTutor);
    hinter.clearHint();
  }
  
  @Override
  public void act(float dt) {
    super.act(dt);
    if (Math.round(ScienceEngine.getTime()) % 10 != 0) return;
    if (activeTutor != null && !hinter.hasHint()) {
      hinter.setHint(activeTutor.getHint());
    }
  }
  
  @Override
  public String getGoal() {
    return goal;
  }

  @Override
  public void finish() {
    hinter.setHint(null);
    recordStats();
    if (!rootTutor.isSuccess()) {
      science2DController.getView().done(false);
      this.setVisible(false);
      return;
    }
    soundManager.play(ScienceEngineSound.CELEBRATE);
    science2DController.getView().done(true);
    tutorNavigator.clearActiveTutor();
    this.setVisible(false);
  }

  private void recordStats() {
    // Update all stats
    stats.timeSpent = getTimeSpent();
    stats.numAttempts = getNumAttempts();
    stats.numSuccesses = getNumSuccesses();
    stats.failureTracker = getFailureTracker();
    stats.percentProgress = getPercentProgress();
    
    stats.save();
  }

  // Prerequisite: childTutors.size() >= 1
  @Override
  public void teach() {
    this.setVisible(true);
    rootTutor.teach();
  }
  
  public void doChallengeAnimation(final ITutor tutor) {
    final Image challenge = new Image(ScienceEngine.getTextureRegion("challenge"));
    challenge.setPosition(ScreenComponent.VIEWPORT_WIDTH / 2,
        ScreenComponent.VIEWPORT_HEIGHT - 60);
    challenge.setSize(256, 256);
    soundManager.play(ScienceEngineSound.CHALLENGE);
    // TODO: Why does sizeTo animation not work below? Once it works, remove initial sizing to 256, 256
    this.addActor(challenge);
    challenge.addAction(
        Actions.sequence(
            // Actions.sizeTo(256, 256),
            Actions.parallel(
                Actions.moveTo(ScreenComponent.VIEWPORT_WIDTH / 2 - challenge.getWidth() / 2, 
                    ScreenComponent.VIEWPORT_HEIGHT / 2 - challenge.getHeight() / 2, 3),
                Actions.sizeTo(256, 256, 3),
                Actions.rotateBy(360, 3)),
            Actions.delay(1),
            new Action() {
              @Override
              public boolean act(float delta) {
                Guru.this.removeActor(challenge);
                tutor.teach();
                return true;
              }
            }));
  }

  public void doRapidFireAnimation(final ITutor tutor) {
    final LabelStyle large = new LabelStyle(skin.get(LabelStyle.class));
    BitmapFont font = skin.getFont(ScreenComponent.getFont(2.5f));
    large.font = font;
    final TextButton start = new TextButton("RapidFire", skin);
    start.setSize(250, 70);
    start.setColor(Color.YELLOW);
    start.getLabel().setStyle(large);
    start.setPosition(ScreenComponent.VIEWPORT_WIDTH / 2 - start.getWidth() / 2,
        ScreenComponent.VIEWPORT_HEIGHT / 2 - start.getHeight() / 2);
    soundManager.play(ScienceEngineSound.RAPID_FIRE);
    this.addActor(start);
    start.addAction(
        Actions.sequence(
            Actions.alpha(0, 1),
            new Action() {
              @Override
              public boolean act(float delta) {
                start.setText("3");
                start.setSize(70, 70);
                start.setPosition(ScreenComponent.VIEWPORT_WIDTH / 2 - start.getWidth() / 2,
                    ScreenComponent.VIEWPORT_HEIGHT / 2 - start.getHeight() / 2);
                return true;
              }
            },
            Actions.repeat(3, 
                Actions.sequence(
                    Actions.alpha(1),
                    Actions.alpha(0.5f, 0.5f),
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

  @Override
  public void prepareToTeach(ITutor childTutor) {
    this.setVisible(true);
    rootTutor.prepareToTeach(null);
  }

  public void setupProbeConfigs(Collection<IModelConfig<?>> configs, boolean enableControls) {
    configGenerator.generateConfig(configs);
    modelControls.syncWithModel(); // Force sync with model
    modelControls.refresh();
    // Turn off access to parts of control panel
    modelControls.enableControls(enableControls);
    viewControls.enableControls(enableControls);
  }

  public void checkProgress() {
    if (activeTutor == this) return;
    activeTutor.checkProgress();
  }

  @Override
  public String getHint() {
    return null;
  }
  
  @Override
  public String getId() {
    return ID;
  }
  
  @Override
  public TutorType getType() {
    return TutorType.Root;
  }
  
  @Override
  public List<ITutor> getChildTutors() {
    return Arrays.asList(new ITutor[] { rootTutor});
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

  public void goTo(ITutor tutor) {
    activeTutor.prepareToTeach(null); // Disable active Tutor
    ScienceEngine.setProbeMode(false);
    setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), true);
    // ??? why above series ??? 
    prepareTutors(tutor);
    tutor.prepareToTeach(null);
    teach();
  }

  private void prepareTutors(ITutor tutor) {
    if (tutor.getParentTutor() != null) {
      prepareTutors(tutor.getParentTutor());
      tutor.getParentTutor().prepareToTeach(tutor);
    }
  }
  
  @Override
  public ITutor getParentTutor() {
    return null;
  }

  @Override
  public void addTimeSpent(float timeTaken) {
  }

  @Override
  public float getTimeSpent() {
    return rootTutor.getTimeSpent();
  }

  @Override
  public float getNumAttempts() {
    return rootTutor.getNumAttempts();
  }

  @Override
  public void systemReadyToFinish(boolean success) {
  }

  public void showNextButton(boolean show) {
    tutorNavigator.showNextButton(show);
  }

  public McqActor getMcqActor() {
    if (mcqActor == null) {
      mcqActor = new McqActor(skin);
    }
    return mcqActor;
  }

  @Override
  public void userReadyToFinish() {
  }

  @Override
  public float getPercentProgress() {
    return rootTutor.getPercentProgress();
  }
  
  @Override
  public State getState() {
    return rootTutor.getState();
  }

  @Override
  public float getNumSuccesses() {
    return rootTutor.getNumSuccesses();
  }
  
  @Override
  public float getFailureTracker() {
    return 0;
  }

  public Skin getSkin() {
    return skin;
  }
}