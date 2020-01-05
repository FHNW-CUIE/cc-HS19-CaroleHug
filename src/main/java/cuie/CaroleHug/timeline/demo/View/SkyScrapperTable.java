package cuie.CaroleHug.timeline.demo.View;

import cuie.CaroleHug.timeline.demo.Skyscraper;
import cuie.CaroleHug.timeline.demo.PresentationModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;


public class SkyScrapperTable extends GridPane implements ViewMixin {

    private final PresentationModel model;
    private TableView<Skyscraper> table;

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
        table = new TableView<Skyscraper>(model.getSkyscrapers());

        TableColumn<Skyscraper, String> name = new TableColumn("Skyscrapper");
        name.setCellValueFactory(cell -> cell.getValue().nameProperty());

        TableColumn<Skyscraper, Integer> build = new TableColumn("Jahr");
        build.setCellValueFactory(cell -> cell.getValue().buildProperty().asObject());

        TableColumn<Skyscraper, Float> height = new TableColumn("Höhe");
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

