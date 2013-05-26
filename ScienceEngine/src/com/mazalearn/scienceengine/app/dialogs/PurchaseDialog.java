package com.mazalearn.scienceengine.app.dialogs;

import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.screens.LoadingScienceTrain;
import com.mazalearn.scienceengine.app.screens.TopicHomeScreen;
import com.mazalearn.scienceengine.app.services.InstallProfile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.billing.IBilling;
import com.mazalearn.scienceengine.billing.Inventory;
import com.mazalearn.scienceengine.billing.SkuDetails;

/**
 * A screen to buy topics
 */
public class PurchaseDialog extends Dialog {
  
  private Skin skin;
  private List<Topic> topicList;
  private ButtonGroup purchasableItems;
  private ScienceEngine scienceEngine;
  private Topic topic;
  private IBilling billing;

  public PurchaseDialog(final Topic topic, Topic level,
      final Stage stage, final Skin skin, final ScienceEngine scienceEngine) {
    super("", skin, "dialog");
    
    this.skin = skin;
    this.scienceEngine = scienceEngine;
    this.topic = topic;

    // retrieve the default table actor
    Table table = getContentTable();
    table.defaults().spaceBottom(ScreenComponent.getScaledY(30));
    table.columnDefaults(0).padRight(ScreenComponent.getScaledX(20));
    table.add("Loading...Please wait...");

    purchasableItems = new ButtonGroup();
    purchasableItems.setMinCheckCount(0);
    purchasableItems.setMaxCheckCount(1);

    // Do an inventory query to get prices and description
    topicList = Arrays.asList(new Topic[] { topic, level});
    final InstallProfile installProfile = ScienceEngine.getPreferencesManager().getInstallProfile();
    billing = new IBilling() {
      @Override
      public void purchaseCallback(Topic purchasedTopic) {
        if (purchasedTopic == null) return;
        // Allow access after marking in install profile
        installProfile.addAsAvailableTopic(purchasedTopic);
        for (Topic child: purchasedTopic.getChildren()) {
          installProfile.addAsAvailableTopic(child);
        }
        installProfile.save();
        LoadingScienceTrain.setWaitForBackend(false);                  
      }

      @Override
      public void inventoryCallback(Inventory inventory) {
        showItemsForPurchase(inventory);
      }
    };
    ScienceEngine.getPlatformAdapter().queryInventory(topicList, billing);
    
  }

  private void showItemsForPurchase(Inventory inventory) {
    Table table = getContentTable();
    table.clear();
    for (Topic item: topicList) {
      SkuDetails skuDetails = inventory.getSkuDetails(item.toProductId());
      final TextButton topicCheckbox = ScreenUtils.createCheckBox(skuDetails.getDescription(), 0f, 0f, 300f, 30f, 
          skin.get("mcq-check", CheckBoxStyle.class)); //$NON-NLS-1$
      topicCheckbox.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        }
      });
      table.add(topicCheckbox).left().height(ScreenComponent.getScaledY(60)).pad(ScreenComponent.getScaledX(10));
      table.add(skuDetails.getPrice()).pad(ScreenComponent.getScaledX(10));
      topicCheckbox.setName(item.name());
      purchasableItems.add(topicCheckbox);
      table.row();
    }
    // Add a purchase button
    TextButton purchaseButton = new TextButton("Purchase Selected Topics", skin, "body");
    getButtonTable().add(purchaseButton)
        .width(ScreenComponent.getScaledX(300))
        .height(ScreenComponent.getScaledY(60));
    purchaseButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        Button topicButton = purchasableItems.getChecked();
        if (topicButton != null) {
          // android.test.purchased - item already owned not handled
          // android.test.canceled - BUG: either purchasedata or datasignature is null
          // android.test.item_unavailable - shows item unavailable
          Topic purchaseTopic = Topic.valueOf(topicButton.getName());
          LoadingScienceTrain.setWaitForBackend(true);
          TopicHomeScreen topicHomeScreen = new TopicHomeScreen(scienceEngine, topic);
          scienceEngine.setScreen(new LoadingScienceTrain(scienceEngine, topicHomeScreen));                  
          ScienceEngine.getPlatformAdapter().launchPurchaseFlow(purchaseTopic, billing);
        }
      } 
    });

    this.show(getStage());
  }
}
