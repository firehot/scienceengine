package com.mazalearn.scienceengine.app.screens;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.Message;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.view.DrawingActor;

public class UserHomeDialog extends Dialog {
  
  private static final int GIFT_WIDTH = 150;
  static final int GIFT_HEIGHT = 100;
  private static final int CERTIFICATE_WIDTH = 75;
  private static final int CERTIFICATE_HEIGHT = 50;
  private static final boolean ENABLE_SOCIAL = true;

  private Profile profile;
  private Skin skin;

  public UserHomeDialog(final Skin skin, final Image userImage) {
    super("", skin);
    this.skin = skin;
    profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    ScienceEngine.loadAtlas("images/social/pack.atlas"); // Unload after since not used elsewhere?

    Table contentTable = getContentTable();
    
    // Title
    Label title = new Label("Home", skin);
    title.setAlignment(Align.center, Align.center);
    contentTable.add(title).fill().width(800).pad(10).height(40).colspan(2);
    contentTable.row();
    
    // Name and face
    addUserInfo(userImage, contentTable);
    
    // Registration information
    if (profile.isRegistered()) {
      addCertificatesPane(contentTable);
      addSocialPane(contentTable);
    } else {
      addRegistrationRequest(contentTable);
      // Sync profile so that registration will work.
      ScienceEngine.getPreferencesManager().syncProfiles();
    }

    contentTable.debug();

    TextButton closeButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Close"), skin, "body");
    this.getButtonTable().add(closeButton).width(150).center();
  }

  private void addUserInfo(final Image userImage, Table contentTable) {
    Label name = new Label(profile.getUserName(), skin);
    name.setAlignment(Align.center, Align.center);
    contentTable.add(name).fill().width(400).pad(10);
    Image image = new Image(ScienceEngine.getTextureRegion(ScienceEngine.USER));
    contentTable.add(image).height(DrawingActor.FACE_HEIGHT).width(DrawingActor.FACE_WIDTH).fill();
    image.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        new ChangeFaceDialog(skin, userImage).show(getStage());
      }
    });
    contentTable.row();
  }

  private void addRegistrationRequest(Table contentTable) {
    Label registration = new Label("", skin);
    registration.setWidth(800);
    registration.setWrap(true);
    registration.setText(ScienceEngine.getMsg().getString("ScienceEngine.RegistrationInfo"));
    contentTable.add(registration).width(400).pad(10);
    TextButton registerButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Register"), skin, "body");
    contentTable.add(registerButton).width(150).center();      
    contentTable.row();
    registerButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        // Bring up registration form
        ScienceEngine.getPlatformAdapter().browseURL("http://" + ScienceEngine.getHostPort() + "/registration.jsp?" + 
            ProfileData.INSTALL_ID + "=" + profile.getInstallationId().toLowerCase());
      }
    });
  }

  private void addCertificatesPane(Table contentTable) {
    // TODO: Once certificate granted, show image here.
    List<TextButton> list = new ArrayList<TextButton>();
    for (String itemName: profile.getCertificates()) {
      TextButton item = createItem(skin, CERTIFICATE_WIDTH, CERTIFICATE_HEIGHT, itemName);
      list.add(item);
    }    
    for (String itemName: new String[] {"certificate", "award", "award", "achievement", "certificate"}) {
      TextButton item = createItem(skin, CERTIFICATE_WIDTH, CERTIFICATE_HEIGHT, itemName);
      list.add(item);
    }
    contentTable.add(createImagesPane(skin, list)).colspan(2);
    contentTable.row();
  }

  private void addSocialPane(Table contentTable) {
    if (!ENABLE_SOCIAL) return;
    
    contentTable.add("Gifts Waiting for You");
    contentTable.add("Send Gifts to Friends");
    contentTable.row();
    
    Actor waitingGiftsPane = createWaitingGiftsPane(this, profile.getInbox(), skin);
    contentTable.add(waitingGiftsPane).width(400).height(GIFT_HEIGHT);
    contentTable.add(createGiftingPane()).width(GIFT_WIDTH * 1.5f).height(GIFT_HEIGHT * 1.5f);
    contentTable.row();
  }

  public static Actor createWaitingGiftsPane(final Dialog parentDialog, 
      List<Message> giftBox, final Skin skin) {
    List<TextButton> list = new ArrayList<TextButton>();
    for (final Message gift: giftBox) {
      final TextButton item = createItem(skin, GIFT_WIDTH, GIFT_HEIGHT, "gift" + gift.giftType);
      list.add(item);
      item.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          new ShowGiftDialog(skin, gift, item, parentDialog).show(parentDialog.getStage());
        }

      });
    }
    return createImagesPane(skin, list);
  }

  private Actor createGiftingPane() {
    Image image = new Image(ScienceEngine.getTextureRegion("opengift"));
    image.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        new GiveGiftDialog(skin, UserHomeDialog.this).show(getStage());      }
    });
    return image;
  }

  private static Actor createImagesPane(Skin skin, List<TextButton> items) {
    Table table = new Table(skin);
    ScrollPane flickScrollPane = new ScrollPane(table, skin, "thumbs");
    flickScrollPane.setScrollingDisabled(false, true);
    table.setFillParent(false);
    table.defaults().fill();
    for (TextButton item: items) {
      Table itemTable = new Table(skin);
      itemTable.setName("Item");
      //giftTable.add(itemName);
      itemTable.row();
      itemTable.add(item)
          .width(item.getWidth())
          .height(item.getHeight());
      table.add(itemTable).pad(5);
    }
    return flickScrollPane;
  }

  private static TextButton createItem(Skin skin, int itemWidth, int itemHeight, String itemName) {
    TextureRegion itemTexture = ScienceEngine.getTextureRegion(itemName);
    TextButton item = 
        ScreenUtils.createImageButton(itemTexture, skin, "clear");
    ScreenComponent.scaleSize(item, itemWidth, itemHeight);
    return item;
  }
  
}