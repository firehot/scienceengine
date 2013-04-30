package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.tutor.ITutor;

public class Scoreboard extends Table {
  private Label scoreLabel;
  private ITutor tutor;
  private Profile profile;

  public Scoreboard(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      debug();
    }
    this.setName(ScreenComponent.Scoreboard.name());

    scoreLabel = new Label("0", skin);   
    this.add(new Image(ScienceEngine.getTextureRegion("goldcoins"))).width(40).height(30).top();
    this.row();
    this.add(scoreLabel).width(40).fill().top();
    profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
  }

  @Override
  public void act(float delta) {
    int tutorPoints = Math.round(profile.getPoints());
    int giftPoints = profile.getGiftPoints();
    scoreLabel.setText(String.valueOf(tutorPoints + giftPoints));
  }
  
  public void setTutor(ITutor tutor) {
    this.tutor = tutor;
  }
}