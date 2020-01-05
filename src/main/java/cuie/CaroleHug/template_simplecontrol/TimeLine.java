package cuie.CaroleHug.template_simplecontrol;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cuie.CaroleHug.template_simplecontrol.demo.Skyscraper;
import cuie.CaroleHug.template_simplecontrol.demo.PresentationModel;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.Insets;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.canvas.Canvas;

/**
 * Es wird ein Zeitstrahl mit den Baujahren aller Wolkenkratzer erstellt, auf welcher die Länge der jeweiligen Linie auch gleich die Höhe des Gebäudes darstellt.
 * Ausserdem wird der aktuell ausgewählte Wolkenkratzer mit Bild dargestellt. Mit dem Verschieben dieses Bildes können die Höhe sowie das Baujahr dieses Gebäudes verändert werden.
 *
 * Carole Hug
 * @author Dieter Holz
 */

public class TimeLine extends Region {
    // needed for StyleableProperties
    private static final StyleablePropertyFactory<TimeLine> FACTORY = new StyleablePropertyFactory<>(Region.getClassCssMetaData());

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    private static final Locale CH = new Locale("de", "CH");

    private static final double ARTBOARD_WIDTH  = 1000;
    private static final double ARTBOARD_HEIGHT = 500;
    private static final double RADIUS_IMAGE_CIRCLE = 100;
    private static final double POSITION_TIMELINE = ARTBOARD_HEIGHT/3;
    private static final double ASPECT_RATIO = ARTBOARD_WIDTH / ARTBOARD_HEIGHT;

    private static final double MINIMUM_WIDTH  = 500;
    private static final double MINIMUM_HEIGHT = MINIMUM_WIDTH / ASPECT_RATIO;
    private static final double MAXIMUM_WIDTH = 800;
    private static final String noImage = "http://simpleicon.com/wp-content/uploads/sad.png";

    private Line arrow_line;
    private Line arrow_line_up;
    private Line arrow_line_down;
    private Line currentSkyscraper_line;
    private Text construction_year_label;
    private Text label_max_year;
    private Text label_min_year;
    private Text height_label;
    private Canvas canvas_skyscrapers;
    private Circle circle;

    private int MAX_BUILD_YEAR;
    private int MIN_BUILD_YEAR;
    private double MAX_HEIGHT;
    private Skyscraper currentSkyscraper;
    ObservableList<Skyscraper> allSkyscrapers;

    private final IntegerProperty currentSkyscraperYear = new SimpleIntegerProperty();
    private final IntegerProperty currentSkyscraperHeight = new SimpleIntegerProperty();
    private final StringProperty currentSkyscraperImage = new SimpleStringProperty();

    private static final CssMetaData<TimeLine, Color> BASE_COLOR_META_DATA = FACTORY.createColorCssMetaData("-base-color", s -> s.baseColor);

    private final StyleableObjectProperty<Color> baseColor = new SimpleStyleableObjectProperty<Color>(BASE_COLOR_META_DATA) {
        @Override
        protected void invalidated() {
            setStyle(getCssMetaData().getProperty() + ": " + colorToCss(get()) + ";");
            applyCss();
        }
    };

    // needed for resizing
    private Pane drawingPane;
    private PresentationModel presentationModel;

    public TimeLine(PresentationModel pm) {
        presentationModel = pm;
        initializeSelf();
        initializeParts();
        initializeDrawingPane();
        layoutParts();
        setupEventHandlers();
        setupValueChangeListeners();
        setupBindings();
    }

    private void initializeSelf() {
        // load stylesheets
        String fonts = getClass().getResource("/fonts/fonts.css").toExternalForm();
        getStylesheets().add(fonts);

        String stylesheet = getClass().getResource("style.css").toExternalForm();
        getStylesheets().add(stylesheet);

        getStyleClass().add("time-line");
    }

    private void initializeParts() {
        allSkyscrapers = presentationModel.getSkyscrapers();

        arrow_line = new Line( ARTBOARD_WIDTH-10,POSITION_TIMELINE, 80, POSITION_TIMELINE);
        arrow_line.getStyleClass().add("arrow_line");

        arrow_line_up = new Line( ARTBOARD_WIDTH-10,POSITION_TIMELINE, ARTBOARD_WIDTH - 25, POSITION_TIMELINE-12);
        arrow_line_up.getStyleClass().add("arrow_line");

        arrow_line_down = new Line( ARTBOARD_WIDTH - 25,POSITION_TIMELINE+12, ARTBOARD_WIDTH-10, POSITION_TIMELINE);
        arrow_line_down.getStyleClass().add("arrow_line");

        construction_year_label = new Text("Baujahr");
        construction_year_label.getStyleClass().add("labels");
        construction_year_label.setX(ARTBOARD_WIDTH-125);
        construction_year_label.setY(POSITION_TIMELINE+30);

        height_label = new Text("Höhe");
        height_label.getStyleClass().add("labels");
        height_label.setX(0);
        height_label.setY(POSITION_TIMELINE/2);

        canvas_skyscrapers = new Canvas(ARTBOARD_WIDTH, POSITION_TIMELINE);
        GraphicsContext gc = canvas_skyscrapers.getGraphicsContext2D();
        drawSkyscrapers(gc);

        label_max_year = new Text(Integer.toString(MAX_BUILD_YEAR));
        label_max_year.getStyleClass().add("labels");
        label_max_year.setX(ARTBOARD_WIDTH-70);
        label_max_year.setY(0);
        label_max_year.setRotate(-45);

        label_min_year = new Text(Integer.toString(MIN_BUILD_YEAR));
        label_min_year.getStyleClass().add("labels");
        label_min_year.setX(arrow_line.getEndX());
        label_min_year.setY(0);
        label_min_year.setRotate(-45);

        currentSkyscraper_line = new Line(calculateYearOnTimeline(currentSkyscraperYear.getValue()),POSITION_TIMELINE, calculateYearOnTimeline(currentSkyscraperYear.getValue()), POSITION_TIMELINE);
        currentSkyscraper_line.getStyleClass().add("current_element");

        circle = new Circle(calculateYearOnTimeline(currentSkyscraperYear.getValue()), ARTBOARD_HEIGHT+calculateHeightSkyscraperHeight(currentSkyscraperHeight.getValue()),RADIUS_IMAGE_CIRCLE);
        circle.getStyleClass().add("current_element");
        circle.getStyleClass().add("current_element_circle");
    }

    private void drawSkyscrapers(GraphicsContext gc) {
        gc.setStroke(Color.web("#089990"));
        gc.setLineWidth(4);
        findMinAndMaxYear();
        findMaxHeight();
        System.out.println(MAX_BUILD_YEAR);
        System.out.println(MIN_BUILD_YEAR);
        for(Skyscraper skyscraper : presentationModel.getSkyscrapers()) {
            double pointOnTimeline = calculateYearOnTimeline(skyscraper.getBuild());
            double skyscraperHeight = calculateHeightSkyscraperHeight(skyscraper.getHeightM());
            gc.strokeLine(pointOnTimeline, POSITION_TIMELINE - skyscraperHeight, pointOnTimeline, POSITION_TIMELINE);
        }
    }

    private double calculateYearOnTimeline(double build) {
        if(build<MIN_BUILD_YEAR) {
            build = MIN_BUILD_YEAR;
        } else if (build>MAX_BUILD_YEAR) {
            build = MAX_BUILD_YEAR;
        }
        return ((arrow_line.getStartX() - arrow_line.getEndX() -10) * (build-MIN_BUILD_YEAR))/(MAX_BUILD_YEAR - MIN_BUILD_YEAR)+80;
    }

    private int calculateYear(double x) {
        return (int) ((x*(MAX_BUILD_YEAR - MIN_BUILD_YEAR))/(arrow_line.getStartX() - arrow_line.getEndX()+1) + MAX_BUILD_YEAR);
    }

    private double calculateHeightSkyscraperHeight(double height) {
        if(height<0) {
            height = 0;
        } else if(height>MAX_HEIGHT) {
            height = MAX_HEIGHT;
        }
        return (((POSITION_TIMELINE)*height) / MAX_HEIGHT);
    }

    private int calculateHeight(double y) {
        return (int) ((y*MAX_HEIGHT)/(POSITION_TIMELINE));
    }

    private void findMinAndMaxYear () {
        MAX_BUILD_YEAR = Calendar.getInstance().get(Calendar.YEAR);
        MIN_BUILD_YEAR = MAX_BUILD_YEAR;
        for(Skyscraper skyscraper : presentationModel.getSkyscrapers()) {
            if(skyscraper.getBuild() < MIN_BUILD_YEAR && skyscraper.getBuild() != 0) {
                MIN_BUILD_YEAR = skyscraper.getBuild();
            }
            if(skyscraper.getBuild()>MAX_BUILD_YEAR) {
                MAX_BUILD_YEAR = skyscraper.getBuild();
            }
        }
    }

    private void findMaxHeight() {
        for(Skyscraper skyscraper : presentationModel.getSkyscrapers()) {
            if (skyscraper.getHeightM() > MAX_HEIGHT) {
                MAX_HEIGHT = skyscraper.getHeightM();
            }
        }
    }

    private void initializeDrawingPane() {
        drawingPane = new Pane();
        drawingPane.getStyleClass().add("drawing-pane");
        drawingPane.setMaxSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
        drawingPane.setMinSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
        drawingPane.setPrefSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
    }

    private void layoutParts() {
        drawingPane.getChildren().addAll(arrow_line, arrow_line_up, arrow_line_down, construction_year_label, height_label,canvas_skyscrapers, label_max_year, label_min_year,currentSkyscraper_line, circle);

        getChildren().add(drawingPane);
    }

    private void setupEventHandlers() {
        circle.setOnMouseDragged(event -> {
            double newXValue = ARTBOARD_WIDTH-event.getX();
            int newYear = calculateYear(-newXValue);
            if (newXValue <= ARTBOARD_WIDTH && newXValue >= 0 && newYear>=MIN_BUILD_YEAR) {
                setCurrentSkyscraperYear(newYear);
            }

            double newYValue = event.getY()-POSITION_TIMELINE;
            int newHeight = calculateHeight(newYValue);
            if (newYValue+3*RADIUS_IMAGE_CIRCLE <= ARTBOARD_HEIGHT && newYValue >= 0 && newHeight>=0) {
                setCurrentSkyscraperHeight(newHeight);
            }
        });
    }

    private void setupValueChangeListeners() {
        currentSkyscraperYear.addListener((observable, oldValue, newValue) -> {
            currentSkyscraper_line.setStartX(calculateYearOnTimeline(currentSkyscraperYear.getValue()));
            currentSkyscraper_line.setEndX(calculateYearOnTimeline(currentSkyscraperYear.getValue()));
            circle.setCenterX(calculateYearOnTimeline(currentSkyscraperYear.getValue()));
        });

        currentSkyscraperHeight.addListener((observable, oldValue, newValue) -> {
            currentSkyscraper_line.setStartY(POSITION_TIMELINE+calculateHeightSkyscraperHeight(currentSkyscraperHeight.getValue()));
            currentSkyscraper_line.setEndY(POSITION_TIMELINE);
            circle.setCenterY(POSITION_TIMELINE+calculateHeightSkyscraperHeight(currentSkyscraperHeight.getValue())+RADIUS_IMAGE_CIRCLE);
        });

        currentSkyscraperImage.addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                circle.setFill(new ImagePattern(new Image(noImage)));
            } else {
                circle.setFill(new ImagePattern(new Image(currentSkyscraperImage.getValue())));
            }
        });

        allSkyscrapers.addListener((ListChangeListener<Skyscraper>) c -> {
            initializeParts();
        });

    }

    private void setupBindings() {
        currentSkyscraper = presentationModel.getSkyscraperProxy();
        currentSkyscraperHeight.bindBidirectional(currentSkyscraper.heightMProperty());
        currentSkyscraperYear.bindBidirectional(currentSkyscraper.buildProperty());
        currentSkyscraperImage.bind(currentSkyscraper.imageUrlProperty());
    }

    //resize by scaling
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        resize();
    }

    private void resize() {
        Insets padding         = getPadding();
        double availableWidth  = getWidth() - padding.getLeft() - padding.getRight();
        double availableHeight = getHeight() - padding.getTop() - padding.getBottom();

        double width = Math.max(Math.min(Math.min(availableWidth, availableHeight * ASPECT_RATIO), MAXIMUM_WIDTH), MINIMUM_WIDTH);

        double scalingFactor = width / ARTBOARD_WIDTH;

        if (availableWidth > 0 && availableHeight > 0) {
            relocateDrawingPaneCentered();
            drawingPane.setScaleX(scalingFactor);
            drawingPane.setScaleY(scalingFactor);
        }
    }

    private void relocateDrawingPaneCentered() {
        drawingPane.relocate((getWidth() - ARTBOARD_WIDTH) * 0.5, (getHeight() - ARTBOARD_HEIGHT) * 0.5);
    }

    // some handy functions

    private String colorToCss(final Color color) {
  		return color.toString().replace("0x", "#");
  	}


    // compute sizes
    @Override
    protected double computeMinWidth(double height) {
        Insets padding           = getPadding();
        double horizontalPadding = padding.getLeft() + padding.getRight();

        return MINIMUM_WIDTH + horizontalPadding;
    }

    @Override
    protected double computeMinHeight(double width) {
        Insets padding         = getPadding();
        double verticalPadding = padding.getTop() + padding.getBottom();

        return MINIMUM_HEIGHT + verticalPadding;
    }

    @Override
    protected double computePrefWidth(double height) {
        Insets padding           = getPadding();
        double horizontalPadding = padding.getLeft() + padding.getRight();

        return ARTBOARD_WIDTH + horizontalPadding;
    }

    @Override
    protected double computePrefHeight(double width) {
        Insets padding         = getPadding();
        double verticalPadding = padding.getTop() + padding.getBottom();

        return ARTBOARD_HEIGHT + verticalPadding;
    }

    // alle getter und setter
    public Color getBaseColor() {
        return baseColor.get();
    }

    public StyleableObjectProperty<Color> baseColorProperty() {
        return baseColor;
    }

    public void setBaseColor(Color baseColor) {
        this.baseColor.set(baseColor);
    }

    public int getCurrentSkyscraperYear() {
        return currentSkyscraperYear.get();
    }

    public IntegerProperty currentSkyscraperYearProperty() {
        return currentSkyscraperYear;
    }

    public void setCurrentSkyscraperYear(int currentSkyscraperYear) {
        this.currentSkyscraperYear.set(currentSkyscraperYear);
    }

    public int getCurrentSkyscraperHeight() {
        return currentSkyscraperHeight.get();
    }

    public IntegerProperty currentSkyscraperHeightProperty() {
        return currentSkyscraperHeight;
    }

    public void setCurrentSkyscraperHeight(int currentSkyscraperHeight) {
        this.currentSkyscraperHeight.set(currentSkyscraperHeight);
    }

}
