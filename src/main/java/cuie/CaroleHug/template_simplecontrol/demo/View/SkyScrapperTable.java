package cuie.CaroleHug.template_simplecontrol.demo.View;

import cuie.CaroleHug.template_simplecontrol.demo.Building;
import cuie.CaroleHug.template_simplecontrol.demo.PresentationModel;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;


public class SkyScrapperTable extends GridPane implements ViewMixin {

    private final PresentationModel model;

    private TableView<Building> table;

    public SkyScrapperTable(PresentationModel model) {
        this.model = model;
        init();
    }

    @Override
    public void initializeSelf() {
        getStyleClass().add("form");
    }



    @Override
    public void initializeControls() {
        table = new TableView<Building>(model.getSkyScrappers());

        TableColumn<Building, String> name = new TableColumn("Skyscrapper");
        name.setCellValueFactory(cell -> cell.getValue().nameProperty());

        TableColumn<Building, Integer> build = new TableColumn("Jahr");
        build.setCellValueFactory(cell -> cell.getValue().buildProperty().asObject());

        TableColumn<Building, Float> height = new TableColumn("Höhe");
        height.setCellValueFactory(cell -> cell.getValue().heightMProperty().asObject());

        table.getColumns().addAll(name, build, height);

    }

    @Override
    public void layoutControls() {
        add(table, 0, 0);
    }

    public void setupBindings() {

    }

    @Override
    public void setupValueChangedListeners() {
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                model.setSelectedSkyScrapperId(newValue.getId());
            }
        });
        table.getSelectionModel().selectFirst();
        table.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                model.setSelectedIndex(newValue.intValue()));
    }

    @Override
    public void addEventHandlers() {

    }

}

