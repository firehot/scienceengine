package com.mazalearn.scienceengine.app.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.view.CommandClickListener;

public class AppRater {

  private final static String APP_TITLE = "Science Engine";
  private final static String APP_PNAME = "com.mazalearn.scienceengine";
  private final static int DAYS_UNTIL_PROMPT = 0;
  private final static int LAUNCHES_UNTIL_PROMPT = 2;

  public static void showRaterDialog(Stage stage, Skin skin, final Profile profile) {
    final Dialog dialog = new Dialog("", skin, "buydialog");
    
    // retrieve the default table actor
    Table table = dialog.getContentTable();

    // Title
    Label title = new Label(ScienceEngine.getMsg().getString("ScienceEngine.RateThisApp"), skin, "default-big");
    title.setColor(Color.BLACK);
    table.add(title).padTop(ScreenComponent.getScaledY(30));
    table.row();
    
    table.columnDefaults(0)
        .pad(ScreenComponent.getScaledX(10))
        .width(ScreenComponent.getScaledX(240))
        .center()
        .fill();

    String text = "If you enjoy using " + APP_TITLE
        + ", please take a moment to rate it. Thanks for your support!";
    Label tv = new Label(text, skin, "buy");
    tv.setWrap(true);
    table.add(tv).padTop(20);
    table.row();

    table.add(ScreenUtils.createScienceTrain(10)).width(75).height(25);
    table.row();

    Button b1 = new TextButton("Rate " + APP_TITLE, skin, "mcq");
    b1.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        ScienceEngine.getPlatformAdapter().browseURL(getPlatformStoreUrl()
            + APP_PNAME);
        dialog.hide();
      }
    });
    table.add(b1);
    table.row();

    Button b2 = new TextButton("Remind me later", skin, "mcq");
    b2.addListener(new CommandClickListener() {
      public void doCommand() {
        dialog.hide();
      }
    });
    table.add(b2);
    table.row();

    Button b3 = new TextButton("No, thanks", skin, "mcq");
    b3.addListener(new CommandClickListener() {
      public void doCommand() {
        profile.setDontShowAgain(true);
        dialog.hide();
      }
    });
    table.add(b3).padBottom(ScreenComponent.getScaledY(30));
    table.row();
    dialog.show(stage);
  }

  public static void appLaunched(Stage stage, Skin skin) {
    Profile profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    if (profile.getDontShowAgain()) {
      return;
    }

    // Increment launch counter
    long launchCount = profile.incrementLaunchCount();

    // Get date of first launch
    Long dateFirstLaunch = profile.getDateFirstLaunch();

    // Wait at least n days before opening
    if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
      if (System.currentTimeMillis() >= dateFirstLaunch
          + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
        showRaterDialog(stage, skin, profile);
      }
    }
  }

  public static String getPlatformStoreUrl() {
    switch(ScienceEngine.getPlatformAdapter().getPlatform()) {
    case IOS: return "itms-apps://ax.itunes.apple.com/WebObjects/MZStore.woa/wa/viewContentsUserReviews?type=Maza+Learn&id=";
    case AndroidEmulator:    
    case Android: 
    default:  return "https://play.google.com/store/apps/details?id=";
    }
  }

  
}
