package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
import com.mazalearn.scienceengine.StatusType;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.Message;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.services.loaders.Trivia;

public class GiveGiftDialog extends Dialog {
  
  private Profile profile;
  private Trivia trivia = new Trivia();
  private Dialog parentDialog;
  private Message gift = new Message();
  private Button sendButton;
  private TextButton makeGift;
  
  public GiveGiftDialog(final Skin skin, final Dialog parentDialog) {
    super("", skin);
    this.parentDialog = parentDialog;
    
    parentDialog.setVisible(false);
    final Table contentTable = getContentTable();
    
    profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    Label title = new Label("Send Gifts to Your Friends", skin);
    title.setWidth(600);
    title.setAlignment(Align.center, Align.center);


    contentTable.debug();
    contentTable.add(title).pad(10).center().colspan(2);
    contentTable.row();
    contentTable.add("Choose Friend");
    contentTable.add("Choose Maza coins");
    contentTable.row();
    contentTable.add(createFriendChooser(skin));
    contentTable.add(createCoinsChooser(skin, parentDialog));
    contentTable.row();
    final Image giftImage = new Image(ScienceEngine.getTextureRegion("opengift"));
    makeGift = new TextButton("Pack the Gift", skin);
    contentTable.add(makeGift).right().spaceRight(10);
    makeGift.setDisabled(true);
    makeGift.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        if (makeGift.isDisabled()) return; 
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        gift.giftType = MathUtils.random(1, 3);
        giftImage.setDrawable(new TextureRegionDrawable(ScienceEngine.getTextureRegion("gift" + gift.giftType)));
        int i = MathUtils.random(trivia.getNumTrivia() - 1);
        gift.text = trivia.getTriviumPart(i, Trivia.Part.text);
        gift.image = trivia.getTriviumPart(i, Trivia.Part.image);
        enableGifting(gift);
      }
    });
    contentTable.add(giftImage).width(100).height(75).left();
    contentTable.row();
    giftImage.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        if (makeGift.isDisabled()) return;
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        new ShowGiftDialog(skin, gift, giftImage, false, GiveGiftDialog.this).show(getStage());
      }
    });

    TextButton cancelButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Cancel"), skin, "body");
    this.getButtonTable().add(cancelButton).width(150).center();
    
    sendButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.SendGift"), skin, "body");
    sendButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        if (sendButton.isDisabled()) return; 
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        profile.sendGift(gift);
      }
    });
    sendButton.setDisabled(true);
    this.getButtonTable().add(sendButton).width(150).center();
    trivia.load();
  }

  private Actor createCoinsChooser(final Skin skin, final Dialog parentDialog) {
    Table coinsTable = new Table(skin);
    final List pointsList = new List(new Integer[] {100, 200, 500, 1000}, skin);
    pointsList.setSelection("500");
    coinsTable.add(pointsList);
    pointsList.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        gift.points = Integer.parseInt(pointsList.getSelection());
        if (gift.points > profile.getPoints()) {
          ScienceEngine.displayStatusMessage(parentDialog.getStage(), 
              StatusType.ERROR, "Not enough points");
          return;
        }
        enableGifting(gift);
      }
    });
    coinsTable.row();
    return coinsTable;
  }
  
  private void enableGifting(Message gift) {
    boolean isDisabled = gift.email == null || gift.giftType == 0 || gift.image == null || 
        gift.points == 0 || gift.points > profile.getPoints();
    sendButton.setDisabled(isDisabled);
    makeGift.setDisabled(gift.points == 0 || gift.points > profile.getPoints() || gift.email == null);
  }

  private Actor createFriendChooser(final Skin skin) {    
    final List friendsList = new List(profile.getFriends().toArray(new String[0]), skin);
    TextButton addFriend = new TextButton("Add a Friend", skin);
    addFriend.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        // Onscreen keyboard does not show in IOS - this is a workaround.
        Gdx.input.getTextInput(new TextInputListener() {
          @Override
          public void input(String email) {
            email = email.toLowerCase();
            ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
            if (!email.contains("@")) {
              ScienceEngine.displayStatusMessage(getStage(), StatusType.ERROR, "Invalid email address");
              return;
            } else if (email.equals(profile.getUserEmail())) {
              ScienceEngine.displayStatusMessage(getStage(), StatusType.ERROR, "Cannot add self as friend.");
              return;
            } else if (profile.getFriends().contains(email)) {
              ScienceEngine.displayStatusMessage(getStage(), StatusType.ERROR, "Friend with this email already exists");
              return;
            }
            profile.addFriend(email);
            ScienceEngine.displayStatusMessage(getStage(), StatusType.INFO, "Friend added");
            gift.email = email;
            friendsList.setItems(profile.getFriends().toArray(new String[0]));
          }
          
          @Override
          public void canceled() {
            ScienceEngine.displayStatusMessage(getStage(), StatusType.WARNING, "Add Friend: Canceled");            
          }
        }, "Enter friend's email address", "");
      }
    });
    friendsList.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        gift.email = friendsList.getSelection();
      }
    });
    
    Table chooserTable = new Table(skin);
    chooserTable.add(friendsList);
    chooserTable.add(addFriend).spaceLeft(30);
    return chooserTable;
  }
  
  @Override
  protected void result(Object object) {
    parentDialog.setVisible(true);
  }
}