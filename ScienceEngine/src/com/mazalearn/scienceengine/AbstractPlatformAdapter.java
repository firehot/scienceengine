package com.mazalearn.scienceengine;

import java.io.File;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.billing.IBilling;
import com.mazalearn.scienceengine.billing.Inventory;
import com.mazalearn.scienceengine.billing.SkuDetails;
import com.mazalearn.scienceengine.core.controller.IScience2DController;

public abstract class AbstractPlatformAdapter implements IPlatformAdapter {

  protected IMessage messages;
  private Platform platform;

  public AbstractPlatformAdapter(Platform platform) {
    this.platform = platform;
  }
  
  @Override
  public void browseURL(String url) {
    Gdx.net.openURI(url);
  }

  @Override
  public void showExternalURL(String uri) {
    showFileUri(Gdx.files.external(uri));
  }

  private void showFileUri(FileHandle file) {
    if (file.exists()) {
      String path = file.file().getAbsolutePath();
      browseURL("file:///" + path.replace("\\", "/"));
    }
  }

  @Override
  public void showInternalURL(String uri) {
    showFileUri(Gdx.files.internal(uri));
  }

  @Override
  public boolean playVideo(File file) {
    return false;
  }

  @Override
  public Stage createLevelEditor(IScience2DController science2DController,
      AbstractScreen screen) {
    return (Stage) science2DController.getView();
  }

  @Override
  public IMessage getMsg() {
    if (messages == null) {
      messages = new BasicMessages();
    }
    return messages;
  }

  @Override
  public Platform getPlatform() {
    return platform;
  }

  @Override
  public BitmapFont loadFont(Skin skin, String language) {
    return skin.getFont("en");
  }

  @Override
  public boolean supportsLanguage() {
    return false;
  }

  @Override
  public void takeSnapshot(Stage stage, Topic topicArea, Topic level, int x, int y, int width, int height) {
  }
  
  @Override
  public void launchPurchaseFlow(final Topic sku, final IBilling billing) {
    if (ScienceEngine.DEV_MODE.isDebug()) {
      // Simulate an asynchronous purchase flow
      executeAsync(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          billing.purchaseCallback(sku);
        }
      });
      return;
    }
    throw new UnsupportedOperationException("Purchase flow not implemented");
  }

  @Override
  public void queryInventory(List<Topic> topicList, final IBilling billing) {
    if (!ScienceEngine.DEV_MODE.isDummyBilling()) {
      throw new UnsupportedOperationException("Query Inventory not implemented");
    }
    final Inventory inventory = new Inventory();
    for (Topic topic: topicList) {
      StringBuffer json = new StringBuffer();
      json.append("{");
      json.append("productId:\"" + topic.toProductId() + "\"");
      json.append(",title:\"" + topic.name() + "\"");
      json.append(",description:\"" + topic.getDescription() + "\"");
      json.append(",price:" + (topic.getChildren().length > 0 ? "\"$4.99\"" : "\"$0.99\""));
      json.append("}");
      SkuDetails skuDetails = SkuDetails.toSkuDetails("inapp", json.toString());
      inventory.addSkuDetails(skuDetails);
    }
    // Simulate an asynchronous inventory query
    executeAsync(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        billing.inventoryCallback(inventory);
      }
    });
  }
  
  @Override
  public boolean supportsSpeech() {
    return false;
  }
  
  @Override
  public void speak(String text, boolean append) {
    // Ignore - throwing exception is not useful.
  }
}
