package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.esotericsoftware.tablelayout.Cell;

public class Controller {
  final Cell<Table> cellTable;
  final IControl control;
  final Table table;

  public Controller(Cell<Table> cellTable, IControl control) {
    this.cellTable = cellTable;
    this.control = control;
    this.table = cellTable.getWidget();
  }

  public void validate() {
    control.syncWithModel();
    cellTable.setWidget(control.isAvailable() ? this.table : null);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static Controller createController(IModelConfig property, 
      Table controlTable, Skin skin) {
    Table table = new Table(skin);
    table.setName(property.getName());
    table.defaults().fill().expand();
    IControl control = null;
    
    switch(property.getType()) {
      case TOGGLE: 
        control = new ToggleButtonControl(property, skin);
        table.add(control.getActor());
        //for checkbox - we need - table.add(property.getName()).pad(0, 5, 0, 5);
        break;
      case RANGE:
        Label name = new Label(property.getParameter().name(), skin);
        table.add(name);
        table.row();
        control = new SliderControl(property, skin);
        Actor slider = control.getActor();
        table.add(slider);
        for (EventListener l: slider.getListeners()) {
          name.addListener(l);
        }
        break;
      case LIST:
        control = new SelectBoxControl(property, skin);
        table.add(control.getActor());
        break;
      case COMMAND:
        control = new CommandButtonControl(property, skin);
        table.add(control.getActor());
        break;
      case TEXT:
        control = new TextMeter(property, skin);
        table.add(control.getActor());
    }
    Controller c = new Controller(controlTable.add(table), control);
    controlTable.row();
    return c;
  }
}