package com.mazalearn.scienceengine.app.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.view.CommandClickListener;

public class AboutDialog extends Dialog {
  
  private static final String COPYRIGHTS_FILE = "data/copyrights.json";

  public AboutDialog(Skin skin) {
    super(ScienceEngine.getMsg().getString("ScienceEngine.About"), skin, "dialog");
    
    Label name = new Label(ScienceEngine.getMsg().getString("ScienceEngine.Name"), skin);
    name.setWidth(ScreenComponent.getScaledX(100));
    name.setAlignment(Align.center, Align.center);
    getContentTable().add(name).uniformX().fillY();

    getContentTable().add(ScreenUtils.createScienceTrain(20))
        .width(ScreenComponent.getScaledX(100))
        .uniformX()
        .height(ScreenComponent.getScaledY(45))
        .left()
        .padBottom(ScreenComponent.getScaledY(15))
        .top();
    getContentTable().row();

    Label description = new Label(ScienceEngine.getMsg().getString("ScienceEngine.Maza"), skin);
    description.setWidth(ScreenComponent.getScaledX(600));
    description.setAlignment(Align.left, Align.left);
    description.setWrap(true);
    
    final Table copyrights = createCopyrightsTable(skin);
    ScrollPane copyrightsPane = new ScrollPane(copyrights, skin);

    getContentTable().add(description).fill().width(ScreenComponent.getScaledX(600)).colspan(2);
    getContentTable().row();
    getContentTable().add(copyrightsPane)
        .colspan(2)
        .pad(ScreenComponent.getScaledX(10)).fill()
        .width(ScreenComponent.getScaledX(600))
        .height(ScreenComponent.getScaledY(150));

    Button b = new TextButton("OK", skin);
    this.getButtonTable().add(b).width(ScreenComponent.getScaledX(200));
  }

  public Table createCopyrightsTable(final Skin skin) {
    Table table = new Table(skin);
    
    final Label header = 
        new Label(ScienceEngine.getMsg().getString("ScienceEngine.Copyrights"), skin);
    header.setWidth(ScreenComponent.getScaledX(600));
    header.setAlignment(Align.left, Align.left);
    header.setWrap(true);
    table.add(header).colspan(3).fill().center().padBottom(ScreenComponent.getScaledX(10));
    table.row();
    
    final Platform thisPlatform = ScienceEngine.getPlatformAdapter().getPlatform();
    Copyright[] copyrights = loadCopyrights();
    for (final Copyright copyright: copyrights) {
      if (!copyright.platform.equals(thisPlatform) && !"All".equals(copyright.platform)) continue;
      
      TextButton name = new TextButton(copyright.name, skin, "clear");
      name.getLabel().setAlignment(Align.left);
      name.addListener(new CommandClickListener() {
        @Override
        public void doCommand() {
          ScienceEngine.getPlatformAdapter().browseURL(copyright.home);
        }       
      });
      
      TextButton license = new TextButton("", skin, "clear");
      license.getLabel().setAlignment(Align.left);
      if (copyright.license == null) {
        license.setText(copyright.attribution);
      } else if (copyright.license.startsWith("http")) {
        license.setText("License");
        license.getLabel().setColor(Color.BLUE);
      } else {
        license.setText(copyright.license);
      }
      
      license.addListener(new CommandClickListener() {
        @Override
        public void doCommand() {
          if (copyright.license.startsWith("http")) {
            ScienceEngine.getPlatformAdapter().browseURL(copyright.license);
          }
        }       
      });
      
      Label attribution = new Label(copyright.attribution, skin);
      attribution.setWrap(true);
      attribution.setAlignment(Align.left, Align.left);
      attribution.addListener(new CommandClickListener() {
        @Override
        public void doCommand() {
          ScienceEngine.getPlatformAdapter().browseURL(copyright.home);
        }       
      });
      
      
      table.add(name).fill().left();
      table.add(attribution).fill().left().width(ScreenComponent.getScaledX(300));
      table.add(license).fill().left();
      table.row();
      
      table.add("").padBottom(ScreenComponent.getScaledX(15)).colspan(3);
      table.row();
    }
    return table;
  }

    public static class Copyright {
      String name;
      String year;
      String home;
      String license;
      String attribution;
      String platform;
      
      String toString(String thisPlatform) {        
        String copyrightStr = name;
        if (year != null && year.length() > 0) {
          copyrightStr += "\nCopyright (C) " + year;
        }
        if (attribution != null && attribution.length() > 0) {
          copyrightStr += "\nAttribution: " + attribution;
        }
        return copyrightStr;
      }
    }
  
  private Copyright[] loadCopyrights() {
    FileHandle file = Gdx.files.internal(COPYRIGHTS_FILE);
    if (file == null) {
      Gdx.app.error(ScienceEngine.LOG, "Could not open copyrights file");
      return null;
    }
    return new Json().fromJson(Copyright[].class, file);
  }
  
}