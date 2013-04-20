package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.services.loaders.Trivia;

public class GiveGiftDialog extends Dialog {
  
  private Profile profile;
  private Trivia trivia = new Trivia();
  private Dialog parentDialog;
  private String giftText;
  private String giftImage;
  private int giftPoints;
  private int giftType;

  public GiveGiftDialog(final Skin skin, Dialog parentDialog) {
    super("", skin);
    this.parentDialog = parentDialog;
    
    parentDialog.hide();
    final Table contentTable = getContentTable();
    
    profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    Label title = new Label("Send Gifts to Your Friends", skin);
    title.setWidth(600);
    title.setAlignment(Align.center, Align.center);


    contentTable.debug();
    contentTable.add(title).width(800).pad(10).center().colspan(2);
    contentTable.row();
    contentTable.add("Choose Friends");
    contentTable.add(new List(profile.getFriends(), skin));
    contentTable.row();
    contentTable.add("Your Points contribution");
    final List pointsList = new List(new Integer[] {100, 200, 500, 1000}, skin);
    pointsList.setSelection("500");
    contentTable.add(pointsList);
    pointsList.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        giftPoints = Integer.parseInt(pointsList.getSelection());
      }
    });
    contentTable.row();
    final Image gift = new Image();
    TextButton makeGift = new TextButton("Make Gift", skin, "body");
    contentTable.add(makeGift);
    makeGift.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        giftType = MathUtils.random(1, 3);
        gift.setDrawable(new TextureRegionDrawable(ScienceEngine.getTextureRegion("gift" + giftType)));
        int i = MathUtils.random(trivia.getNumTrivia() - 1);
        giftText = trivia.getTriviumPart(i, Trivia.Part.text);
        giftImage = trivia.getTriviumPart(i, Trivia.Part.image);
      }
    });
    contentTable.add(gift).width(100).height(75);
    contentTable.row();
    gift.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        new ShowGiftDialog(skin, giftPoints, giftText, giftImage, GiveGiftDialog.this).show(getStage());
      }
    });

    TextButton cancelButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Cancel"), skin, "body");
    this.getButtonTable().add(cancelButton).width(150).center();
    
    Button sendButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.SendGift"), skin, "body");
    sendButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        profile.postMessage("test@mazalearn.com", giftType, giftText, giftImage, giftPoints);
      }
    });
    this.getButtonTable().add(sendButton).width(150).center();
    trivia.load();
  }
  
  @Override
  protected void result(Object object) {
    parentDialog.show(getStage());
  }
}