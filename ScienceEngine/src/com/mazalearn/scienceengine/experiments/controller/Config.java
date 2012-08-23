package com.mazalearn.scienceengine.experiments.controller;

import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;

public class Config {
  final Table table;
  final IConfigElement configElement;
  ICondition iCondition = null;

  public Config(Table table, IConfigElement configElement) {
    this.table = table;
    this.configElement = configElement;
  }

  public void addCondition(ICondition iCondition) {
    this.iCondition = iCondition;
  }
  
  void validate() {
    configElement.syncWithModel();
    table.visible = iCondition == null || iCondition.eval();
  }
}