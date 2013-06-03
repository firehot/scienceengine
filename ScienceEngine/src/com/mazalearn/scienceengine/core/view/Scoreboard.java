package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.Profile;

public class Scoreboard extends Table {
  private Label scoreLabel;
  private Profile profile;

  public Scoreboard(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE.isDebug()) {
      debug();
    }
    this.setName(ScreenComponent.Scoreboard.name());

    LabelStyle style = new LabelStyle(skin.get(LabelStyle.class));
    style.font = skin.getFont("default-small");
    scoreLabel = new Label("0", style);
    scoreLabel.setAlignment(Align.center, Align.center);
    Image coins = new Image(ScienceEngine.getTextureRegion("goldcoins"));
    coins.setSize(ScreenComponent.Scoreboard.getWidth(), ScreenComponent.Scoreboard.getHeight());
    
    this.add(coins).width(coins.getWidth()).height(coins.getHeight()).top();
    this.add(scoreLabel).width(coins.getWidth()).fill().top();
    profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    int tutorPoints = Math.round(profile.getPoints());
    int giftPoints = profile.getGiftPoints();
    scoreLabel.setText(String.valueOf(tutorPoints + giftPoints));
  }
}