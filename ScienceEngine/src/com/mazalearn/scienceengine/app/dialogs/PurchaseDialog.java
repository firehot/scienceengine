package com.mazalearn.scienceengine.app.dialogs;

import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.billing.IBilling;
import com.mazalearn.scienceengine.billing.Inventory;
import com.mazalearn.scienceengine.billing.SkuDetails;
import com.mazalearn.scienceengine.tutor.IDoneCallback;

/**
 * A screen to buy topics
 */
public class PurchaseDialog extends Dialog {
  
  private static String getMsg(String msgId) {
    return ScienceEngine.getMsg().getString(msgId);
  }

  public PurchaseDialog(final Topic topic, Topic level, final IBilling billing, 
      final Stage stage, final Skin skin) {
    super("", skin);

    // retrieve the default table actor
    Table table = getContentTable();
    table.defaults().spaceBottom(ScreenComponent.getScaledY(30));
    table.columnDefaults(0).padRight(ScreenComponent.getScaledX(20));

    // Do an inventory queue here to get prices and description
    List<Topic> topicList = Arrays.asList(new Topic[] { topic, level});
    Inventory inventory = ScienceEngine.getPlatformAdapter().queryInventory(topicList);
    final ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.setMinCheckCount(0);
    buttonGroup.setMaxCheckCount(1);
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
      buttonGroup.add(topicCheckbox);
      table.row();
    }
    
    TextButton okButton = new TextButton("Purchase Selected Topics", skin, "body");
    getButtonTable().add(okButton).width(ScreenComponent.getScaledX(300)).height(ScreenComponent.getScaledY(60));
    okButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        Button topicButton = buttonGroup.getChecked();
        if (topicButton != null) {
          // android.test.purchased - item already owned not handled
          // android.test.canceled - BUG: either purchasedata or datasignature is null
          // android.test.item_unavailable - shows item unavailable
          Topic purchaseTopic = Topic.valueOf(topicButton.getName());
          ScienceEngine.getPlatformAdapter().launchPurchaseFlow(purchaseTopic, "inapp", billing);
        }
      }
    });

  }
}
