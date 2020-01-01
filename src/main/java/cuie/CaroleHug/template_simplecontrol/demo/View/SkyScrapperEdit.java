package cuie.CaroleHug.template_simplecontrol.demo.View;

import cuie.CaroleHug.template_simplecontrol.demo.Building;
import cuie.CaroleHug.template_simplecontrol.demo.PresentationModel;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.converter.NumberStringConverter;

public class SkyScrapperEdit extends GridPane implements ViewMixin {
    private final PresentationModel model;
    private Label name;
    private TextField nameEdit;
    private Label height;
    private TextField heightEdit;
    private Label build;
    private TextField buildEdit;

    public SkyScrapperEdit(PresentationModel model) {
        this.model = model;
        init();
    }


    @Override
    public void initializeControls() {
        name = new Label("name");
        nameEdit = new TextField();
        height = new Label("Höhe: ");
        heightEdit = new TextField();
        build = new Label("Baujahr: ");
        buildEdit = new TextField();
    }

    @Override
    public void layoutControls() {
        add(name, 0, 0);
        add(nameEdit, 1, 0);
        add(height, 0, 1);
        add(heightEdit, 1, 1);
        add(build, 0, 2);
        add(buildEdit, 1, 2);
    }

    @Override
    public void addEventHandlers() {

    }

    @Override
    public void setupBindings() {
        Building proxy = model.getSkyScrapperProxy();
        //Editierte Angaben binden
        heightEdit.textProperty().bindBidirectional(proxy.heightMProperty(), new NumberStringConverter());
        buildEdit.textProperty().bindBidirectional(proxy.buildProperty(), new NumberStringConverter());
    }
}