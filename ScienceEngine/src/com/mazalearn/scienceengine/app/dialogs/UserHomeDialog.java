package com.mazalearn.scienceengine.app.dialogs;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.Message;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.app.utils.ServerConstants;
import com.mazalearn.scienceengine.core.view.CommandClickListener;
import com.mazalearn.scienceengine.core.view.DrawingActor;

public class UserHomeDialog extends Dialog {
  
  private static final int GIFT_WIDTH = 150;
  static final int GIFT_HEIGHT = 100;
  private static final int CERTIFICATE_WIDTH = 75;
  private static final int CERTIFICATE_HEIGHT = 50;
  private static final boolean ENABLE_SOCIAL = true;

  private Profile profile;
  private Skin skin;
  private TextButton myUserImage;

  public UserHomeDialog(final Skin skin, final Image userImage) {
    super("Home", skin, "dialog");
    this.skin = skin;
    profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    ScienceEngine.loadAtlas("images/social/pack.atlas"); // Unload after since not used elsewhere?
    
    Table contentTable = getContentTable();
    setTitle("Home");
    // Name and face
    addUserInfo(userImage, contentTable);
    
    addCertificatesPane(contentTable);
    // Registration information
    if (profile.isRegistered()) {
      addSocialPane(contentTable);
    } else {
      addRegistrationRequest(contentTable);
      // Sync profile so that registration will work.
      ScienceEngine.getPreferencesManager().syncProfiles(true);
    }

    contentTable.debug();

    TextButton closeButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Close"), skin, "body");
    TextButton rateButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.RateThisApp"), skin, "body");
    rateButton.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        AppRater.showRaterDialog(getStage(), skin, profile, false);
      }
    });
    this.getButtonTable()
        .add(rateButton)
        .width(ScreenComponent.getScaledX(150))
        .height(ScreenComponent.getScaledY(30))
        .left()
        .padRight(ScreenComponent.getScaledX(150));
    this.getButtonTable()
        .add(closeButton)
        .width(ScreenComponent.getScaledX(150))
        .height(ScreenComponent.getScaledY(30))
        .right();
//    Gdx.graphics.requestRendering();
  }

  private void addUserInfo(final Image userImage, Table contentTable) {
    Label name = new Label(profile.getUserName(), skin);
    name.setAlignment(Align.center, Align.center);
    contentTable.add(name).fill().height(DrawingActor.FACE_HEIGHT).pad(ScreenComponent.getScaledY(10));
    myUserImage = ScreenUtils.createImageButton(ScienceEngine.getTextureRegion(ScienceEngine.USER), skin, "body");
    contentTable.add(myUserImage).height(DrawingActor.FACE_HEIGHT).width(DrawingActor.FACE_WIDTH).fill();
    myUserImage.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        new ChangeFaceDialog(skin, userImage, UserHomeDialog.this).show(getStage());
      }
    });
    contentTable.row();
  }

  public void setUserImage(Drawable drawable) {
    myUserImage.setBackground(drawable);
  }
  
  private void addRegistrationRequest(Table contentTable) {
    Label registration = new Label("", skin);
    registration.setWrap(true);
    registration.setText(ScienceEngine.getMsg().getString("ScienceEngine.RegistrationInfo"));
    contentTable.add(registration)
        .width(ScreenComponent.getScaledX(300))
        .pad(ScreenComponent.getScaledY(10));
    TextButton registerButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Register"), skin, "body");
    contentTable.add(registerButton).width(ScreenComponent.getScaledX(150)).center();
    contentTable.row();
    registerButton.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        // Hide this dialog
        UserHomeDialog.this.hide();
        // Bring up registration form
        ScienceEngine.getPlatformAdapter().browseURL("http://" + ScienceEngine.getHostPort() + 
            ServerConstants.REGISTRATION_SERVLET + "?" + 
            ProfileData.INSTALL_ID + "=" + profile.getInstallationId() + "&" +
            ProfileData.USER_EMAIL + "=" + profile.getUserEmail());
        // Indicate that sync is required after 120s
        ScienceEngine.getPreferencesManager().setSyncAfterSeconds(120);
      }
    });
  }

  private void addCertificatesPane(Table contentTable) {
    List<TextButton> list = new ArrayList<TextButton>();
    for (final String itemName: profile.getCertificates()) {
      TextButton item = createItem(skin, CERTIFICATE_WIDTH, CERTIFICATE_HEIGHT, itemName);
      item.addListener(new CommandClickListener() {
        @Override
        public void doCommand() {
          if (profile.getUserEmail().length() > 0) {
            ScienceEngine.getPlatformAdapter().browseURL("http://" + ScienceEngine.getHostPort() + 
                ServerConstants.CERTIFICATE_SERVLET + "?" +  
                ProfileData.USER_ID + "=" + profile.getUserEmail() + "&" +
                ServerConstants.TOPIC + "=" + itemName);
          } else {
            new MessageDialog(skin, "To view your ceritificate, you must be registered.").show(getStage());
          }
        }        
      });
      list.add(item);
    }    
    String[] dummyCertificates = {"certificate", "award", "award", "achievement", "certificate"};
    for (int i = profile.getCertificates().size(); i < dummyCertificates.length; i++) {
      TextButton item = createItem(skin, CERTIFICATE_WIDTH, CERTIFICATE_HEIGHT, dummyCertificates[i]);
      list.add(item);
    }
    contentTable.add(createImagesPane(skin, list)).colspan(2).height(ScreenComponent.getScaledY(CERTIFICATE_HEIGHT));
    contentTable.row();
  }

  private void addSocialPane(Table contentTable) {
    if (!ENABLE_SOCIAL) return;
    
    contentTable.add("Gifts Waiting for You");
    contentTable.add("Send Gifts to Friends");
    contentTable.row();
    
    Actor waitingGiftsPane = createWaitingGiftsPane(this, profile.getInbox(), true, skin);
    float giftWidth = ScreenComponent.getScaledX(GIFT_WIDTH);
    float giftHeight = ScreenComponent.getScaledX(GIFT_HEIGHT);
    contentTable.add(waitingGiftsPane).width(giftWidth * 2.5f).height(giftHeight).padLeft(30);
    contentTable.add(createGiftingPane()).width(giftWidth * 1.5f).height(giftHeight * 1.5f).padRight(30);
    contentTable.row();
  }

  public static Actor createWaitingGiftsPane(final Dialog parentDialog, 
      List<Message> giftBox, final boolean allowAccept, final Skin skin) {
    List<TextButton> list = new ArrayList<TextButton>();
    for (final Message gift: giftBox) {
      final TextButton item = createItem(skin, GIFT_WIDTH, GIFT_HEIGHT, "gift" + gift.giftType);
      if (item == null) continue;
      list.add(item);
      item.addListener(new CommandClickListener() {
        @Override
        public void doCommand() {
          new ShowGiftDialog(skin, gift, item, allowAccept, parentDialog).show(parentDialog.getStage());
        }

      });
    }
    return createImagesPane(skin, list);
  }

  private Actor createGiftingPane() {
    Image image = new Image(ScienceEngine.getTextureRegion("opengift"));
    ScreenComponent.scaleSize(image, image.getWidth(), image.getHeight());
    image.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
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
      table.add(itemTable).pad(ScreenComponent.getScaledY(5));
    }
    return flickScrollPane;
  }

  private static TextButton createItem(Skin skin, int itemWidth, int itemHeight, String itemName) {
    TextureRegion itemTexture = ScienceEngine.getTextureRegion(itemName);
    if (itemTexture == null) {
      Gdx.app.error(ScienceEngine.LOG, "Unknown item texture: " + itemName);
      return null;
    }
    
    TextButton item = 
        ScreenUtils.createImageButton(itemTexture, skin, "clear");
    ScreenComponent.scaleSize(item, itemWidth, itemHeight);
    return item;
  }
}