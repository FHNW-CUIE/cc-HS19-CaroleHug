package cuie.CaroleHug.template_simplecontrol.demo;

import cuie.CaroleHug.template_simplecontrol.demo.View.SkyScrapperEdit;
import cuie.CaroleHug.template_simplecontrol.demo.View.SkyScrapperTable;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import cuie.CaroleHug.template_simplecontrol.SimpleControl;

public class DemoPane extends BorderPane {

    private final PresentationModel pm;

    // declare the custom control
    private SimpleControl cc;

    // all controls
    private Slider      slider;
    private ColorPicker colorPicker;
    private SkyScrapperTable skyScrapperTable;
    private SkyScrapperEdit skyScrapperEdit;

    public DemoPane(PresentationModel pm) {
        this.pm = pm;
        initializeControls();
        layoutControls();
        setupBindings();
    }

    private void initializeControls() {
        setPadding(new Insets(10));

        cc = new SimpleControl(pm);

        slider = new Slider();
        slider.setShowTickLabels(true);

        colorPicker = new ColorPicker();
        skyScrapperTable = new SkyScrapperTable(pm);
        skyScrapperEdit = new SkyScrapperEdit(pm);
    }

    private void layoutControls() {
        VBox controlPane = new VBox(new Label("SimpleControl Properties"),
                                    slider, colorPicker);
        controlPane.setPadding(new Insets(0, 50, 0, 50));
        controlPane.setSpacing(10);

        setCenter(cc);
        //setRight(controlPane);
        setLeft(skyScrapperTable);
        setRight(skyScrapperEdit);
    }

    private void setupBindings() {
        slider.valueProperty().bindBidirectional(pm.pmValueProperty());
        colorPicker.valueProperty().bindBidirectional(pm.baseColorProperty());


        cc.valueProperty().bindBidirectional(pm.pmValueProperty());
        cc.baseColorProperty().bindBidirectional(pm.baseColorProperty());
    }

}
