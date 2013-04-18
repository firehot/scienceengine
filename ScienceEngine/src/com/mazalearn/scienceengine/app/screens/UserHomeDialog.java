package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.view.DrawingActor;

public class UserHomeDialog extends Dialog {
  
  private static final int GIFT_WIDTH = 150;
  private static final int GIFT_HEIGHT = 100;

  private Profile profile;
  private Skin skin;

  public UserHomeDialog(final Skin skin, final Image userImage) {
    super("", skin);
    this.skin = skin;
    profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();

    // Title
    Label title = new Label("Home", skin);
    title.setAlignment(Align.center, Align.center);
    getContentTable().add(title).fill().width(800).pad(10).height(40).colspan(2);
    getContentTable().row();
    
    // Name and face
    Label name = new Label(profile.getUserName(), skin);
    name.setAlignment(Align.center, Align.center);
    getContentTable().add(name).fill().width(400).pad(10);
    Image image = new Image(ScienceEngine.getTextureRegion(ScienceEngine.USER));
    getContentTable().add(image).height(DrawingActor.FACE_HEIGHT).width(DrawingActor.FACE_WIDTH).fill();
    image.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        new ChangeFaceDialog(skin, userImage).show(getStage());
      }
    });
    getContentTable().row();

    // Installation Info
    getContentTable().add(ScienceEngine.getMsg().getString("ScienceEngine.Name"));
    Label installation = new Label("\nID:" + profile.getInstallationId(), skin);
    installation.setWrap(true);
    LabelStyle small = new LabelStyle(installation.getStyle());
    small.font = skin.getFont("font12");
    installation.setStyle(small);
    getContentTable().add(installation).left().fill();
    getContentTable().row();

    // Registration information
    final Label registration = new Label("", skin);
    registration.setWidth(800);
    registration.setWrap(true);
    final boolean alreadyRegistered = profile.getUserEmail().length() > 0;
    Button registerButton = null;
    if (alreadyRegistered) {
      registration.setText(ScienceEngine.getMsg().getString("ScienceEngine.Registered"));
      getContentTable().add(registration).width(400).pad(10);
      getContentTable().add(profile.getUserEmail());
      getContentTable().row();
      getContentTable().add("Gifts Waiting for You");
      getContentTable().add("Create and Send Gifts");
      getContentTable().row();
      getContentTable().add(createGiftsWaitingPane()).width(400);
      getContentTable().add(createGiftingPane()).width(400);
      getContentTable().row();
    } else {
      registration.setText(ScienceEngine.getMsg().getString("ScienceEngine.RegistrationInfo"));
      getContentTable().add(registration).width(400).pad(10);
      registerButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Register"), skin, "body");
      getContentTable().add(registerButton).width(150).center();      
      getContentTable().row();
      registerButton.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          // Bring up registration form
          ScienceEngine.getPlatformAdapter().browseURL("http://" + ScienceEngine.getHostPort() + "/registration.jsp?" + 
              Profile.INSTALL_ID + "=" + profile.getInstallationId().toLowerCase());
        }
      });
      // Sync profile so that registration will work.
      ScienceEngine.getPreferencesManager().syncProfiles();
    }

    getContentTable().debug();

    TextButton closeButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Close"), skin, "body");
    this.getButtonTable().add(closeButton).width(150).center();
  }

  private Actor createGiftingPane() {
    return null;
  }

  private Actor createGiftsWaitingPane() {
    ScienceEngine.loadAtlas("images/social/pack.atlas");
    Table table = new Table(skin);
    table.setName("Gifts Waiting");
    ScrollPane flickScrollPane = new ScrollPane(table, skin, "thumbs");
    flickScrollPane.setScrollingDisabled(false, true);
    table.setFillParent(false);
    table.defaults().fill();
    for (String giftName: new String[] {"gift1", "gift2", "gift3"}) {
      TextureRegion giftTexture = ScienceEngine.getTextureRegion(giftName);
      TextButton gift = 
          ScreenUtils.createImageButton(giftTexture, skin, "clear");
      ScreenComponent.scaleSize(gift, GIFT_WIDTH, GIFT_HEIGHT);
      gift.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
        }

      });
      Table giftTable = new Table(skin);
      giftTable.setName("Gift");
      giftTable.add(giftName);
      giftTable.row();
      giftTable.add(gift)
          .width(gift.getWidth())
          .height(gift.getHeight());
      table.add(giftTable).pad(5);
    }
    return flickScrollPane;
  }
  
}