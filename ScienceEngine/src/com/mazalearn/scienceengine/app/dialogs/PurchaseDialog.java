package com.mazalearn.scienceengine.app.dialogs;

import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.StatusType;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.screens.LoadingScienceTrain;
import com.mazalearn.scienceengine.app.screens.TopicHomeScreen;
import com.mazalearn.scienceengine.app.services.InstallProfile;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.billing.IBilling;
import com.mazalearn.scienceengine.billing.Inventory;
import com.mazalearn.scienceengine.billing.SkuDetails;
import com.mazalearn.scienceengine.core.view.CommandClickListener;

/**
 * A screen to buy topics
 */
public class PurchaseDialog extends Dialog {
  
  private static final String PURCHASE_FLOW = "PurchaseFlow";
  private final Skin skin;
  private final List<Topic> topicList;
  private final ButtonGroup purchasableItems;
  private ScienceEngine scienceEngine;
  private final Topic topic;
  private final IBilling billing;
  private final Table waitActor;
  // flag for communication across purchase thread and UI thread
  // null => status unknown, false => failure, true => success
  private Boolean purchaseDone = null;
  private final long timeStart = System.currentTimeMillis();
  private float[] stats = new float[State.values().length];
  private Profile profile;
  private final String purchaseFlowId;
  
  public enum State {
    Initiated(0), InventoryQuery(1), InventoryFailure(2), InventoryDisplay(3),
    PurchaseRequest(5), PurchaseFailure(6), PurchaseDone(7), PurchaseCanceled(8);
    
    public final int index;

    private State(int index) {
      this.index = index;
    }
  }
  
  
  public PurchaseDialog(final Topic topic, Topic level,
      final Stage stage, final Skin skin, final ScienceEngine scienceEngine) {
    super("", skin, "buydialog");
    
    this.skin = skin;
    this.scienceEngine = scienceEngine;
    this.topic = topic;
    this.purchaseFlowId = level.getTopicId() + "$" + PURCHASE_FLOW;
    profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    profile.setCurrentActivity(level);
    profile.getStats(level, purchaseFlowId, State.values().length);
    log(State.Initiated); 

    // retrieve the default table actor
    Table table = getContentTable();
    table.defaults().spaceBottom(ScreenComponent.getScaledY(10));
    table.columnDefaults(0).padRight(ScreenComponent.getScaledX(20));
    waitActor = createWaitActor(skin);
    table.add(waitActor).width(waitActor.getPrefWidth()).height(waitActor.getPrefHeight());

    purchasableItems = new ButtonGroup();
    purchasableItems.setMinCheckCount(1);
    purchasableItems.setMaxCheckCount(1);

    // Do an inventory query to get prices and description
    topicList = Arrays.asList(new Topic[] { topic, level});
    final InstallProfile installProfile = ScienceEngine.getPreferencesManager().getInstallProfile();
    billing = new IBilling() {
      @Override
      public void purchaseCallback(Topic purchasedTopic) {
        if (purchasedTopic != null) {
          log(State.PurchaseDone); 
          // Allow access after marking in install profile
          installProfile.addAsAvailableTopic(purchasedTopic);
          for (Topic child: purchasedTopic.getChildren()) {
            installProfile.addAsAvailableTopic(child);
          }
          installProfile.save();
          ScienceEngine.getSoundManager().play(ScienceEngineSound.SUCCESS);
          ScienceEngine.displayStatusMessage(getStage(), StatusType.INFO, "Purchase completed successfully");
          purchaseDone = true;
        } else {
          log(State.PurchaseFailure); 
          ScienceEngine.displayStatusMessage(getStage(), StatusType.ERROR, "Purchase could not be completed");
          ScienceEngine.getSoundManager().play(ScienceEngineSound.FAILURE);
          purchaseDone = false;
        }
      }

      @Override
      public void inventoryCallback(Inventory inventory) {
        showItemsForPurchase(inventory);
      }
    };
    log(State.InventoryQuery);
    ScienceEngine.getPlatformAdapter().queryInventory(topicList, billing);
  }

  private void log(State state) {
    stats[state.index] += (float) (System.currentTimeMillis() - timeStart) / 1000.0f;
  }
  
  private Table createWaitActor(final Skin skin) {
    Table waitActor = new Table(skin);
    Label wait1 = new Label("Processing...Please wait...", skin, "buy");
    waitActor.add(wait1).padBottom(ScreenComponent.getScaledY(30));
    waitActor.row();
    waitActor.add(ScreenUtils.createScienceTrain(10)).width(75).height(25);
    waitActor.row();
    Label wait2 = new Label("Processing...Please wait...", skin, "buy");
    waitActor.add(wait2).padTop(ScreenComponent.getScaledY(30));
    return waitActor;
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    if (purchaseDone == null) return;
    profile.saveStats(stats, purchaseFlowId);
    profile.save();
    if (purchaseDone) {
      TopicHomeScreen topicHomeScreen = new TopicHomeScreen(scienceEngine, topic);
      scienceEngine.setScreen(new LoadingScienceTrain(scienceEngine, topicHomeScreen));
    }
    purchaseDone = null; // So that it will not come back in here.
    hide();    
  }

  private void showItemsForPurchase(Inventory inventory) {
    Table table = getContentTable();
    waitActor.remove();
    table.clear();
    boolean buyDisabled = inventory == null;
    if (buyDisabled) {
      log(State.InventoryFailure);
      // Not able to query items 
      Label notAvailable = new Label("Sorry, Store is unavailable now.\nIs billing setup properly? Is the network working?", skin, "buy");
      notAvailable.setAlignment(Align.center, Align.center);
      table.add(notAvailable);
    } else {
      log(State.InventoryDisplay);
      addPurchasableItems(inventory, table);
    }
    table.row();
    Table buttonTable = createButtons(buyDisabled);
    table.add(buttonTable).colspan(2);

    this.show(getStage());
  }

  public void addPurchasableItems(Inventory inventory, Table table) {
    for (Topic item: topicList) {
      SkuDetails skuDetails = inventory.getSkuDetails(item.toProductId());
      final TextButton topicCheckbox = ScreenUtils.createCheckBox(skuDetails.getDescription(), 0f, 0f, 300f, 30f, 
          skin.get("mcq-buy", CheckBoxStyle.class)); //$NON-NLS-1$
      topicCheckbox.getLabel().setAlignment(Align.center, Align.center);
      topicCheckbox.addListener(new CommandClickListener() {
        @Override
        public void doCommand() {
        }
      });
      table.add(topicCheckbox).left().height(ScreenComponent.getScaledY(60));
      Label price = new Label(skuDetails.getPrice(), skin, "buy");
      table.add(price)
          .padRight(ScreenComponent.getScaledX(10));
      topicCheckbox.setName(item.name());
      purchasableItems.add(topicCheckbox);
      table.row();
    }
  }

  public Table createButtons(boolean buyDisabled) {
    final Table buttonTable = new Table(skin);
    // Add a cancel and purchase button
    TextButton cancelButton = new TextButton("Cancel", skin, "toggle");
    cancelButton.setChecked(true);
    buttonTable.add(cancelButton)
        .width(ScreenComponent.getScaledX(60))
        .height(ScreenComponent.getScaledY(30))
        .left()
        .padRight(ScreenComponent.getScaledX(60));
    cancelButton.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        log(State.PurchaseCanceled);
        purchaseDone = false;
      }
    });
    
    TextButtonStyle buyStyle = skin.get("body", TextButtonStyle.class);
    buyStyle.font = skin.getFont(ScreenComponent.getFont(2));
    final TextButton purchaseButton = new TextButton("Buy", skin, "body");
    purchaseButton.setDisabled(buyDisabled);
    buttonTable.add(purchaseButton)
        .width(ScreenComponent.getScaledX(100))
        .height(ScreenComponent.getScaledY(60))
        .padLeft(ScreenComponent.getScaledX(60))
        .right();
    purchaseButton.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        if (purchaseButton.isDisabled()) return;
        Button topicButton = purchasableItems.getChecked();
        if (topicButton != null) {
          // android.test.purchased - item already owned not handled
          // android.test.canceled - BUG: either purchasedata or datasignature is null
          // android.test.item_unavailable - shows item unavailable
          Topic purchaseTopic = Topic.valueOf(topicButton.getName());
          buttonTable.clear();
          buttonTable.add(waitActor);
          show(getStage());
          log(State.PurchaseRequest);
          ScienceEngine.getPlatformAdapter().launchPurchaseFlow(purchaseTopic, billing);
        } else {
          log(State.PurchaseCanceled);
          purchaseDone = false;
        }
      } 
    });
    return buttonTable;
  }
}
