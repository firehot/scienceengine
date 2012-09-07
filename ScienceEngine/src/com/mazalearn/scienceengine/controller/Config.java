package com.mazalearn.scienceengine.controller;

import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.esotericsoftware.tablelayout.Cell;

public class Config {
  final Cell<Table> cellTable;
  final IViewConfig viewConfig;
  private static int CONFIG_HEIGHT = 40;

  public Config(Cell<Table> cellTable, IViewConfig viewConfig) {
    this.cellTable = cellTable;
    this.viewConfig = viewConfig;
  }

  void validate() {
    viewConfig.syncWithModel();
    cellTable.getWidget().visible = viewConfig.isAvailable();
    cellTable.height(viewConfig.isAvailable() ? CONFIG_HEIGHT : 0);
  }
}