package com.mazalearn.scienceengine;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.billing.IBilling;
import com.mazalearn.scienceengine.billing.Inventory;
import com.mazalearn.scienceengine.billing.SkuDetails;

public class PlatformAdapterImpl extends NonWebPlatformAdapter {
  
  public PlatformAdapterImpl(Platform platform) {
    super(platform);
  }

  public IMessage getMsg() {
    if (messages == null) {
      messages = new Messages(getPlatform());
    }
    return messages;
  }
  
  @Override
	public void browseURL(String url) {
    if(java.awt.Desktop.isDesktopSupported() ) {
      java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
      
      if(desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
        try {
          java.net.URI uri = new java.net.URI(url);
          desktop.browse(uri);
        }
        catch ( Exception e ) {
          System.err.println( e.getMessage() );
        }
      }
    }
	}

  @Override
  public boolean playVideo(File file) {
    if(java.awt.Desktop.isDesktopSupported() ) {
      java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
      
      if(desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
        try {
          desktop.open(file);
          return true;
        }
        catch ( Exception e ) {
          System.err.println( e.getMessage() );
          try {
            Runtime.getRuntime().exec("cmd.exe /C \"" + file.getAbsolutePath() + "\"");
            return true;
          } catch (IOException e2) {
            System.err.println( e2.getMessage() );
          }
        }
      }
    }
    return false;
  }

  /*
  @Override
  public BitmapFont getScaledFont(int pointSize) {
    FileHandle fontFileHandle = Gdx.files.internal("skin/Roboto-Regular.ttf");
    StringBuilder characters = new StringBuilder();
    for (char c = 0; c <= 127; c++) {
      characters.append(c);
    }
    FreeTypeFontGenerator generator = 
        new FreeTypeFontGenerator(fontFileHandle);
    BitmapFont font = generator.generateFont(pointSize, characters.toString(), false);
    generator.dispose();
    return font;
  } */

  private static final String HINDI_TTF = "Lohit-Devanagari.ttf"; // "aksharhindi.ttf";
  private static final String KANNADA_TTF = "Lohit-Kannada.ttf"; // "aksharkannada.ttf";
  @Override
  public BitmapFont loadFont(Skin skin, String language) {
    BitmapFont font;
    String fontFileName = null;
    char beginChar = 0, endChar = 0;
    if (language.equals("ka")) {
      fontFileName = KANNADA_TTF; // unicode: 0C80-0CFF
      beginChar = '\u0c80'; endChar = '\u0cff';
    } else if (language.equals("hi")) {
      fontFileName =  HINDI_TTF; // unicode: 0900-097F
      beginChar = '\u0900'; endChar = '\u097f';
    } else if (language.equals("en")) {
      fontFileName =  "Arial.ttf"; // unicode: 0020-00FF
      beginChar = '\u0020'; endChar = '\u00ff';
    }
    BitmapFontCache.setFallbackFont(skin.getFont("en"));
    FileHandle fontFileHandle = Gdx.files.internal("skin/" + fontFileName);
    FreeTypeFontGenerator generator = 
        new FreeTypeFontGenerator(fontFileHandle);
    StringBuilder characters = new StringBuilder();
    for (char c = beginChar; c <= endChar; c++) {
      characters.append(c);
    }
    font = generator.generateFont(16, characters.toString(), false);
    generator.dispose();      

    return font;
  }

  @Override
  public boolean supportsLanguage() {
    return true;
  }

  @Override
  public void launchPurchaseFlow(Topic sku, String itemType,
      IBilling billing, String extraData) {
    if (ScienceEngine.DEV_MODE == DevMode.DEBUG) {
      billing.purchaseCallback(sku);
      return;
    }
    throw new UnsupportedOperationException("Purchase flow not implemented");
  }

  @Override
  public Inventory queryInventory(List<Topic> topicList) {
    if (ScienceEngine.DEV_MODE == DevMode.DEBUG) {
      Inventory inventory = new Inventory();
      for (Topic topic: topicList) {
        SkuDetails skuDetails = new SkuDetails(topic.name(), topic.name(), topic.getChildren().length > 0 ? "$4.99" : "0.99");
        inventory.addSkuDetails(skuDetails);
      }
      return inventory;
    }
    throw new UnsupportedOperationException("Query Inventory not implemented");
  }
}