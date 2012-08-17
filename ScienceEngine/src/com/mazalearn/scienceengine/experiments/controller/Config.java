package com.mazalearn.scienceengine.experiments.controller;

import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;

public class Config {
  final Table table;
  Condition condition = null;

  public Config(Table table) {
    this.table = table;
  }

  public void addCondition(Condition condition) {
    this.condition = condition;
  }
  
  void validate() {
    table.visible = condition == null || condition.eval();
  }
}