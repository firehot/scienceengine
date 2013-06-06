package com.mazalearn.scienceengine.app.screens;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.StatusType;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.screens.HelpTour.IHelpComponent;
import com.mazalearn.scienceengine.app.services.loaders.AsyncLevelLoader;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.view.CommandClickListener;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.core.view.Science2DGestureDetector;
import com.mazalearn.scienceengine.domains.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.domains.statesofmatter.StatesOfMatterController;
import com.mazalearn.scienceengine.domains.waves.WaveController;
import com.mazalearn.scienceengine.tutor.ITutor;
import com.mazalearn.scienceengine.tutor.TimeTracker;

/**
 * Activity screen corresponding to one level.
 */
public class ActivityScreen extends AbstractScreen {

  private IScience2DController science2DController;
  private Topic topic;
  @SuppressWarnings("unused")
  private Topic activityLevel;
  private ITutor tutorUnderRevision;
  private IScience2DView science2DView;
  private Actor helpActor;
 
  public ActivityScreen(ScienceEngine scienceEngine, Topic topic, Topic level) {
    super(scienceEngine, null);
    this.topic = topic;
    this.activityLevel = level;
    String fileName = LevelUtil.getLevelFilename(topic, level, ".json");
    if (ScienceEngine.getAssetManager().isLoaded(fileName)) {
      ScienceEngine.getAssetManager().unload(fileName);
    }
    this.science2DController = 
        createTopicController(topic, level, ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    science2DView = science2DController.getView();
    getProfile().setCurrentActivity(level);
    if (ScienceEngine.DEV_MODE.isDesign()) {
      Stage levelEditor = 
          ScienceEngine.getPlatformAdapter().createLevelEditor(science2DController, this);
      this.setStage(levelEditor);
    } else {
      this.setStage((Stage) science2DView);
    }
    stage.addListener(new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Keys.BACK) {
          goBack();
          return true;
        }
        return super.keyDown(event, keycode);
      }      
    });
    setTitle(science2DController.getTitle());
    if (ScienceEngine.getPlatformAdapter().getPlatform() != IPlatformAdapter.Platform.GWT) {
      Gdx.graphics.setContinuousRendering(true);
    }
  }
  
  public void enterRevisionMode(ITutor tutorUnderRevision) {
    setTitle("Revision: " + science2DController.getTitle());
    this.tutorUnderRevision = tutorUnderRevision;
    showHelp();
  }
  
  @Override
  protected Group setupCoreGroup(Stage stage) {
    // We create activityviewcontrols first so that viewcontrols will get overridden
    ((Stage) science2DView).addActor(science2DController.getViewControls());
    Group coreGroup = super.setupCoreGroup(stage);
    
    helpActor = createHelpActor();
    coreGroup.addActor(helpActor);
    // Add TimeTracker
    Actor timeTracker = new TimeTracker("0", getSkin());
    coreGroup.addActor(timeTracker);
    
    // If GWT, display a disclaimer about experiencing on a Tablet
    if (ScienceEngine.getPlatformAdapter().getPlatform() == Platform.GWT) {
      ScienceEngine.displayStatusMessage(stage, StatusType.WARNING, 
          "Partial Demo only. Best experienced on Android/iPad Tablet.");
    }
    return coreGroup;
  }

  private Actor createHelpActor() {
    Image helpImage = new Image(ScienceEngine.getTextureRegion("help"));
    helpImage.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        showHelp();
      }
    });
    ScreenComponent sc = ScreenComponent.Help;
    helpImage.setPosition(sc.getX(), sc.getY());
    helpImage.setSize(sc.getWidth(), sc.getHeight());
    helpImage.setName(ScreenComponent.Help.name());
    return helpImage;
  }
  
  private void showHelp() {
    Actor helpTour = science2DView.findActor(ScreenComponent.HELP_TOUR);
    if (helpTour != null) return;
    String description = null;
    if (tutorUnderRevision != null) {
      description = "Revision for: " + tutorUnderRevision.getGoal();
    } else {
      description = science2DController.getTitle() + "\n\n" + 
        getMsg(science2DController.getTopic() + "." + 
            science2DController.getLevel() + ".Begin");
    }
    List<IHelpComponent> helpComponents = new ArrayList<IHelpComponent>();
    Group activityGroup = (Group) stage.getRoot().findActor(ScreenComponent.ACTIVITY_GROUP);
    for (Actor actor: activityGroup.getChildren()) {
      if ((actor instanceof Science2DActor)) {
        helpComponents.add((IHelpComponent) actor);
      }
    }
    for (ScreenComponent screenComponent: ScreenComponent.values()) {
      if (screenComponent.showInHelpTour()) {
        helpComponents.add(screenComponent);
      }
    }
    new HelpTour(getStage(), getSkin(), description, helpComponents);
  }
    
  @Override 
  public void show() {
    super.show();
    IScience2DView science2DView = science2DController.getView();
    InputProcessor gestureListener = new Science2DGestureDetector((Stage) science2DView);
    Gdx.input.setInputProcessor(new InputMultiplexer(gestureListener, (Stage) science2DView));
    Gdx.app.log(ScienceEngine.LOG, "Set gesture detector");
    science2DView.tutoring(true);
    // If progress on this level is 0, then show help
    if (science2DController.getGuru().getStats()[ITutor.PERCENT_PROGRESS] == 0) {
      showHelp();
    }
  }
  
  @Override
  protected void goBack() {
    // Stop tutoring if it was in progress
    science2DController.getView().tutoring(false);
    if (tutorUnderRevision != null) {
      this.tutorUnderRevision = 
          science2DController.getGuru().getTutorHelper().popRevisionMode();
      return;
    }
    TopicHomeScreen topicHomeScreen = 
        new TopicHomeScreen(scienceEngine, topic);
    getProfile().setCurrentActivity(null);
    scienceEngine.setScreen(new LoadingScreen(scienceEngine, topicHomeScreen));
  }
  
  public IScience2DController createTopicController(
      Topic topic, Topic level, int width, int height) {
    switch (topic) {
    case StatesOfMatter: 
      return new StatesOfMatterController(level, width, height, getSkin());
    case Waves:
      return  new WaveController(level, width, height, getSkin());
    case Electromagnetism:
      return new ElectroMagnetismController(level, width, height, getSkin());
    default:
      throw new IllegalArgumentException("Unknown controller: " + topic);
    }
  }
  
  @Override
  public void addAssets() {
    super.addAssets();
    String fileName = LevelUtil.getLevelFilename(topic, 
        science2DController.getLevel(), ".json");
    if (ScienceEngine.getAssetManager().isLoaded(fileName)) {
      return;
    }
    // Guru resources
    ScienceEngine.loadAtlas("images/guru/pack.atlas");
    AsyncLevelLoader.LevelLoaderParameter parameter = new AsyncLevelLoader.LevelLoaderParameter();
    parameter.science2DController = science2DController;
    ScienceEngine.getAssetManager().load(fileName, IScience2DController.class, parameter);
  }

}
