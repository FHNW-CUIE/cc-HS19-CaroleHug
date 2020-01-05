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
    private final ObservableList<Skyscraper> skyscrapers = FXCollections.observableArrayList();
    //private final IntegerProperty skyScrappersId = new SimpleIntegerProperty(-1);

    public ObservableList<Skyscraper> getSkyscrapers() {
        return skyscrapers;
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
        skyscrapers.addAll(readFromFile());
        addListener();
    }

    private void addListener() {
        System.out.println("addlistener");
        selectedSkyScrapperId.addListener((observable, oldValue, newValue) -> {
            Skyscraper oldSelection = getSkyScrapper(oldValue.intValue());
            Skyscraper newSelection = getSkyScrapper(newValue.intValue());

                    if (oldSelection != null) {
                        skyscraperProxy.heightMProperty().unbindBidirectional(oldSelection.heightMProperty());
                        skyscraperProxy.buildProperty().unbindBidirectional(oldSelection.buildProperty());
                        skyscraperProxy.imageUrlProperty().unbindBidirectional(oldSelection.imageUrlProperty());
                        System.out.println(oldSelection);
                    }

                    if (newSelection != null) {
                        skyscraperProxy.heightMProperty().bindBidirectional(newSelection.heightMProperty());
                        skyscraperProxy.buildProperty().bindBidirectional(newSelection.buildProperty());
                        skyscraperProxy.imageUrlProperty().bindBidirectional(newSelection.imageUrlProperty());
                        System.out.println(newSelection);
                    }
                }
        );

    }


    // OOP2-Project
    private final IntegerProperty selectedSkyScrapperId = new SimpleIntegerProperty(-1);
    private final Skyscraper skyscraperProxy = new Skyscraper();
    private IntegerProperty selectedIndex = new SimpleIntegerProperty();

    public final Skyscraper getSkyscraperProxy() {
        return skyscraperProxy;
    }

    public Skyscraper getSkyScrapper(int id) {
        System.out.println("getskyscrapper" + id);
        return skyscrapers.stream()
                .filter(Building -> Building.getId() == id)
                .findAny()
                .orElse(null);
    }

    public void setSelectedSkyScrapperId(int selectedSkyScrapperId) {
        this.selectedSkyScrapperId.set(selectedSkyScrapperId);
    }


    public void setSelectedIndex(int value) {
        selectedIndex.set(value);
    }



    private List<Skyscraper> readFromFile() {
        try (Stream<String> stream = getStreamOfLines(FILE_NAME, false)) {
            return stream.skip(1)                                  // erste Zeile ist die Headerzeile; ueberspringen
                    .map(s -> new Skyscraper(s.split(TAB,16))) // aus jeder Zeile ein Objekt machen
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
