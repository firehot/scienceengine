package com.mazalearn.scienceengine.controller;

import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;

public class Config {
  final Table table;
  final IViewConfig viewConfig;

  public Config(Table table, IViewConfig viewConfig) {
    this.table = table;
    this.viewConfig = viewConfig;
  }

  void validate() {
    viewConfig.syncWithModel();
    table.visible = viewConfig.isAvailable();
  }
}