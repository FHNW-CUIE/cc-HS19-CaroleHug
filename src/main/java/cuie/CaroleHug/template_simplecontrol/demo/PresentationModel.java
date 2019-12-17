package cuie.CaroleHug.template_simplecontrol.demo;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PresentationModel {
    private final DoubleProperty        pmValue   = new SimpleDoubleProperty();
    private final ObjectProperty<Color> baseColor = new SimpleObjectProperty<>();

    private static final String FILE_NAME = "data/skyscrapers.csv";
    private static final String TAB = ";";
    private final ObservableList<Building> skyScrappers = FXCollections.observableArrayList();
    //private final IntegerProperty skyScrappersId = new SimpleIntegerProperty(-1);

    public ObservableList<Building> getSkyScrappers() {
        return skyScrappers;
    }

    public double getPmValue() {
        return pmValue.get();
    }

    public DoubleProperty pmValueProperty() {
        return pmValue;
    }

    public void setPmValue(double pmValue) {
        this.pmValue.set(pmValue);
    }

    public Color getBaseColor() {
        return baseColor.get();
    }

    public ObjectProperty<Color> baseColorProperty() {
        return baseColor;
    }

    public void setBaseColor(Color baseColor) {
        this.baseColor.set(baseColor);
    }



    public PresentationModel() {
        skyScrappers.addAll(readFromFile());
    }

    private List<Building> readFromFile() {
        try (Stream<String> stream = getStreamOfLines(FILE_NAME, false)) {
            return stream.skip(1)                                  // erste Zeile ist die Headerzeile; ueberspringen
                    .map(s -> new Building(s.split(TAB,16))) // aus jeder Zeile ein Objekt machen
                    .collect(Collectors.toList());            // alles aufsammeln*/
        }
    }

    private Stream<String> getStreamOfLines(String fileName, boolean locatedInSameFolder) {
        try {
            return Files.lines(getPath(fileName, locatedInSameFolder), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Path getPath(String fileName, boolean locatedInSameFolder)  {
        try {
            if(!locatedInSameFolder) {
                fileName = "/" + fileName;
            }
            return Paths.get(getClass().getResource(fileName).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
