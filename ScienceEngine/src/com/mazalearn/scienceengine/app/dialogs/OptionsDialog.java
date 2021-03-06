package com.mazalearn.scienceengine.app.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.PreferencesManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.view.CommandClickListener;

/**
 * A simple options screen.
 */
public class OptionsDialog extends Dialog {
  private Label volumeValue;
  private Skin skin;
  private PreferencesManager preferencesManager;
  
  private static String getMsg(String msgId) {
    return ScienceEngine.getMsg().getString(msgId);
  }

  public OptionsDialog(final Stage stage, final Skin skin) {
    super(getMsg("ScienceEngine.Options"), skin, "dialog");
    this.skin = skin;
    preferencesManager = ScienceEngine.getPreferencesManager();

    // retrieve the default table actor
    Table table = getContentTable();
    table.defaults().spaceBottom(ScreenComponent.getScaledY(10));
    table.columnDefaults(0).padRight(ScreenComponent.getScaledX(20));

    // Create locale selection box if platform supports languages
    final IPlatformAdapter platform = ScienceEngine.getPlatformAdapter();
    
    addLanguageOption(table, platform);
    
    addSyncOption(table, platform);
    
    addSoundOption(table);

    addMusicOption(table);

    addSpeechOption(table, platform);
    
    addVolumeSlider(table);
    
    addAboutOption(stage, table);
    
    getButtonTable().add(new TextButton("OK", skin)).width(150);
  }

  public void addAboutOption(final Stage stage, Table table) {
    table.row();
    TextButton aboutButton = 
        new TextButton(getMsg("ScienceEngine.About"), skin, "body");
    aboutButton.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        new AboutDialog(skin).show(stage);
      }      
    });
    table.add(aboutButton).colspan(3);
  }

  public void addVolumeSlider(Table table) {
    // range is [0.0,1.0]; step is 0.1f
    final Slider volumeSlider = new Slider(0f, 1f, 0.1f, false, skin);
    volumeSlider.setValue(ScienceEngine.getPreferencesManager().getVolume());
    volumeSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float value = volumeSlider.getValue();
        preferencesManager.setVolume(value);
        ScienceEngine.getMusicManager().setVolume(value);
        ScienceEngine.getSoundManager().setVolume(value);
        updateVolumeLabel();
      }
    });

    // create the volume label
    volumeValue = new Label("", skin); //$NON-NLS-1$
    updateVolumeLabel();

    // add the volume row
    table.row();
    table.add(getMsg("ScienceEngine.Volume")); //$NON-NLS-1$
    table.add(volumeSlider);
    table.add(volumeValue).width(40);
  }

  public void addSpeechOption(Table table,
      final IPlatformAdapter platform) {
    if (platform.supportsSpeech()) {
      final TextButton speechCheckbox = ScreenUtils.createCheckBox("", 
          0, 0, 50, 30, skin.get(CheckBoxStyle.class));
      speechCheckbox.setChecked(ScienceEngine.getPreferencesManager().isSpeechEnabled());
      speechCheckbox.addListener(new CommandClickListener() {
        @Override
        public void doCommand() {
          boolean enabled = speechCheckbox.isChecked();
          preferencesManager.setSpeechEnabled(enabled);
        }
      });
      table.row();
      table.add(getMsg("ScienceEngine.Speech")); //$NON-NLS-1$
      table.add(speechCheckbox).colspan(1).left().fillX();
    }
  }

  public void addSoundOption(Table table) {
    final TextButton soundEffectsCheckbox = ScreenUtils.createCheckBox("", 
        0, 0, 50, 30, skin.get(CheckBoxStyle.class));
    soundEffectsCheckbox.setChecked(ScienceEngine.getPreferencesManager()
        .isSoundEnabled());
    soundEffectsCheckbox.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        boolean enabled = soundEffectsCheckbox.isChecked();
        preferencesManager.setSoundEnabled(enabled);
        ScienceEngine.getSoundManager().setEnabled(enabled);
      }
    });
    table.row();
    table.add(getMsg("ScienceEngine.SoundEffects")); //$NON-NLS-1$
    table.add(soundEffectsCheckbox).colspan(1).left().fillX();
  }

  public void addMusicOption(Table table) {
    final TextButton musicCheckbox = ScreenUtils.createCheckBox("", 
        0, 0, 50, 30, skin.get(CheckBoxStyle.class));
    musicCheckbox.setChecked(ScienceEngine.getPreferencesManager().isMusicEnabled());
    musicCheckbox.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        boolean enabled = musicCheckbox.isChecked();
        preferencesManager.setMusicEnabled(enabled);
        ScienceEngine.getMusicManager().setEnabled(enabled);
 
        // if the music is now enabled, start playing the menu music
        if (enabled)
          ScienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);
      }
    });
    table.row();
    table.add(getMsg("ScienceEngine.Music")); //$NON-NLS-1$
    table.add(musicCheckbox).colspan(1).left().fillX();
  }

  public void addSyncOption(Table table,
      final IPlatformAdapter platform) {
    if (platform.supportsSync() && ScienceEngine.DEV_MODE.isDebug()) {
      final SelectBox syncSelect = 
          new SelectBox(new String[] { "Manual", "Automatic"}, skin);
      syncSelect.setSelection("Automatic");
      syncSelect.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          preferencesManager.setSync(syncSelect.getSelection());
        }
      });
      table.row();
      table.add(getMsg("ScienceEngine.SyncMode")); // $NON-NLS-1$
      table.add(syncSelect).left().fillX();
      TextButton syncButton = new TextButton(getMsg("ScienceEngine.ForceSync"), skin, "body");
      syncButton.addListener(new CommandClickListener() {
        @Override
        public void doCommand() {
          preferencesManager.syncProfiles(true);
        }
      });
      table.add(syncButton).left();
    }
  }

  public void addLanguageOption(Table table, final IPlatformAdapter platform) {
    if (platform.supportsLanguage()) {
      final SelectBox languageSelect = 
          new SelectBox(new String[] { "en", "ka", "hi"}, skin);
      languageSelect.setSelection(ScienceEngine.getMsg().getLanguage());
      languageSelect.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          ScienceEngine.getMsg().setLanguage(skin, languageSelect.getSelection());
        }
      });
      table.row();
      table.add(getMsg("ScienceEngine.Language")); // $NON-NLS-1$
      table.add(languageSelect).colspan(1).left().fillX();
    }
  }

  /**
   * Updates the volume label next to the slider.
   */
  private void updateVolumeLabel() {
    int volume = Math.round(ScienceEngine.getPreferencesManager().getVolume() * 100);
    volumeValue.setText(String.valueOf(volume));
  }
}
