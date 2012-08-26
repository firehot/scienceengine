package com.mazalearn.scienceengine.experiments.controller;

import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;

public class Config {
  final Table table;
  final IConfigElement configElement;

  public Config(Table table, IConfigElement configElement) {
    this.table = table;
    this.configElement = configElement;
  }

  void validate() {
    configElement.syncWithModel();
    table.visible = configElement.isAvailable();
  }
}