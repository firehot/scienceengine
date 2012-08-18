package com.mazalearn.scienceengine.experiments.controller;

import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;

public class Config {
  final Table table;
  ICondition iCondition = null;

  public Config(Table table) {
    this.table = table;
  }

  public void addCondition(ICondition iCondition) {
    this.iCondition = iCondition;
  }
  
  void validate() {
    table.visible = iCondition == null || iCondition.eval();
  }
}