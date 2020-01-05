package cuie.CaroleHug.template_simplecontrol;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cuie.CaroleHug.template_simplecontrol.demo.SkyScrapper;
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
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import javafx.util.Duration;
import javafx.scene.canvas.Canvas;

/**
 * ToDo: CustomControl kurz beschreiben
 *
 * Carole Hug
 * @author Dieter Holz
 */
//Todo: Umbenennen.
public class SimpleControl extends Region {
    // needed for StyleableProperties
    private static final StyleablePropertyFactory<SimpleControl> FACTORY = new StyleablePropertyFactory<>(Region.getClassCssMetaData());

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
    private Line currentSkyScrapper_line;
    private Text construction_year_label;
    private Text label_max_year;
    private Text label_min_year;
    private Text height_label;
    private Canvas canvas_skyScrappers;
    private Circle circle;

    private int MAX_BUILD_YEAR;
    private int MIN_BUILD_YEAR;
    private double MAX_HEIGHT;
    private SkyScrapper currentSkyScrapper;
    ObservableList<SkyScrapper> allSkyscrappers;

    private final IntegerProperty currentSkyScrapperYear = new SimpleIntegerProperty();
    private final IntegerProperty currentSkyScrapperHeight = new SimpleIntegerProperty();
    private final StringProperty currentSkyScrapperImage = new SimpleStringProperty();

    private static final CssMetaData<SimpleControl, Color> BASE_COLOR_META_DATA = FACTORY.createColorCssMetaData("-base-color", s -> s.baseColor);

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

    public SimpleControl(PresentationModel pm) {
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

        getStyleClass().add("simple-control");  // Todo: an den Namen der Klasse (des CustomControls) anpassen
    }

    private void initializeParts() {
        //ToDo: alle deklarierten Parts initialisieren
        allSkyscrappers = presentationModel.getSkyScrappers();

        arrow_line = new Line( ARTBOARD_WIDTH-10,POSITION_TIMELINE, 0, POSITION_TIMELINE);
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
        height_label.setX(-80);
        height_label.setY(POSITION_TIMELINE/2);

        canvas_skyScrappers = new Canvas(ARTBOARD_WIDTH, POSITION_TIMELINE);
        GraphicsContext gc = canvas_skyScrappers.getGraphicsContext2D();
        drawSkyScrappers(gc);

        label_max_year = new Text(Integer.toString(MAX_BUILD_YEAR));
        label_max_year.getStyleClass().add("labels");
        label_max_year.setX((arrow_line.getStartX() - arrow_line.getEndX()-50));
        label_max_year.setY(0);
        label_max_year.setRotate(-45);

        label_min_year = new Text(Integer.toString(MIN_BUILD_YEAR));
        label_min_year.getStyleClass().add("labels");
        label_min_year.setX(arrow_line.getEndX());
        label_min_year.setY(0);
        label_min_year.setRotate(-45);

        currentSkyScrapper_line = new Line(calculateYearOnTimeline(currentSkyScrapperYear.getValue()),POSITION_TIMELINE, calculateYearOnTimeline(currentSkyScrapperYear.getValue()), POSITION_TIMELINE);
        currentSkyScrapper_line.getStyleClass().add("current_element");

        circle = new Circle(calculateYearOnTimeline(currentSkyScrapperYear.getValue()), ARTBOARD_HEIGHT+calculateHeightSkyScrapperHeight(currentSkyScrapperHeight.getValue()),RADIUS_IMAGE_CIRCLE);
        circle.getStyleClass().add("current_element");
        circle.getStyleClass().add("current_element_circle");
    }

    private void drawSkyScrappers(GraphicsContext gc) {
        gc.setStroke(Color.web("#089990"));
        gc.setLineWidth(4);
        findMinAndMaxYear();
        findMaxHeight();
        System.out.println(MAX_BUILD_YEAR);
        System.out.println(MIN_BUILD_YEAR);
        for(SkyScrapper skyScrapper : presentationModel.getSkyScrappers()) {
            double pointOnTimeline = calculateYearOnTimeline(skyScrapper.getBuild());
            double skyScrapperHeight = calculateHeightSkyScrapperHeight(skyScrapper.getHeightM());
            gc.strokeLine(pointOnTimeline, POSITION_TIMELINE - skyScrapperHeight, pointOnTimeline, POSITION_TIMELINE);
        }
    }

    private double calculateYearOnTimeline(double build) {
        if(build<MIN_BUILD_YEAR) {
            build = MIN_BUILD_YEAR;
        } else if (build>MAX_BUILD_YEAR) {
            build = MAX_BUILD_YEAR;
        }
        return ((arrow_line.getStartX() - arrow_line.getEndX()-50) * (build-MIN_BUILD_YEAR))/(MAX_BUILD_YEAR - MIN_BUILD_YEAR);
    }

    private int calculateYear(double x) {
        return (int) ((x*(MAX_BUILD_YEAR - MIN_BUILD_YEAR))/(arrow_line.getStartX() - arrow_line.getEndX()+1) + MAX_BUILD_YEAR);
    }

    private double calculateHeightSkyScrapperHeight(double height) {
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
        for(SkyScrapper skyScrapper : presentationModel.getSkyScrappers()) {
            if(skyScrapper.getBuild() < MIN_BUILD_YEAR && skyScrapper.getBuild() != 0) {
                MIN_BUILD_YEAR = skyScrapper.getBuild();
            }
            if(skyScrapper.getBuild()>MAX_BUILD_YEAR) {
                MAX_BUILD_YEAR = skyScrapper.getBuild();
            }
        }
    }

    private void findMaxHeight() {
        for(SkyScrapper skyScrapper : presentationModel.getSkyScrappers()) {
            if (skyScrapper.getHeightM() > MAX_HEIGHT) {
                MAX_HEIGHT = skyScrapper.getHeightM();
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
        drawingPane.getChildren().addAll(arrow_line, arrow_line_up, arrow_line_down, construction_year_label, height_label,canvas_skyScrappers, label_max_year, label_min_year,currentSkyScrapper_line, circle);

        getChildren().add(drawingPane);
    }

    private void setupEventHandlers() {
        circle.setOnMouseDragged(event -> {
            double newXValue = ARTBOARD_WIDTH-event.getX();
            int newYear = calculateYear(-newXValue);
            if (newXValue <= ARTBOARD_WIDTH && newXValue >= 0 && newYear>=MIN_BUILD_YEAR) {
                setCurrentSkyScrapperYear(newYear);
            }

            double newYValue = event.getY()-POSITION_TIMELINE;
            int newHeight = calculateHeight(newYValue);
            if (newYValue+3*RADIUS_IMAGE_CIRCLE <= ARTBOARD_HEIGHT && newYValue >= 0 && newHeight>=0) {
                setCurrentSkyScrapperHeight(newHeight);
            }
        });

    }

    private void setupValueChangeListeners() {
        currentSkyScrapperYear.addListener((observable, oldValue, newValue) -> {
            currentSkyScrapper_line.setStartX(calculateYearOnTimeline(currentSkyScrapperYear.getValue()));
            currentSkyScrapper_line.setEndX(calculateYearOnTimeline(currentSkyScrapperYear.getValue()));
            circle.setCenterX(calculateYearOnTimeline(currentSkyScrapperYear.getValue()));
        });

        currentSkyScrapperHeight.addListener((observable, oldValue, newValue) -> {
            currentSkyScrapper_line.setStartY(POSITION_TIMELINE+calculateHeightSkyScrapperHeight(currentSkyScrapperHeight.getValue()));
            currentSkyScrapper_line.setEndY(POSITION_TIMELINE);
            circle.setCenterY(POSITION_TIMELINE+calculateHeightSkyScrapperHeight(currentSkyScrapperHeight.getValue())+RADIUS_IMAGE_CIRCLE);
        });

        currentSkyScrapperImage.addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                circle.setFill(new ImagePattern(new Image(noImage)));
            } else {
                circle.setFill(new ImagePattern(new Image(currentSkyScrapperImage.getValue())));
            }
        });

        allSkyscrappers.addListener((ListChangeListener<SkyScrapper>) c -> {
            initializeParts();
        });

    }

    private void setupBindings() {
        currentSkyScrapper = presentationModel.getSkyScrapperProxy();
        currentSkyScrapperHeight.bindBidirectional(currentSkyScrapper.heightMProperty());
        currentSkyScrapperYear.bindBidirectional(currentSkyScrapper.buildProperty());
        currentSkyScrapperImage.bind(currentSkyScrapper.imageUrlProperty());
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

    private void relocateDrawingPaneCenterBottom(double scaleY, double paddingBottom) {
        double visualHeight = ARTBOARD_HEIGHT * scaleY;
        double visualSpace  = getHeight() - visualHeight;
        double y            = visualSpace + (visualHeight - ARTBOARD_HEIGHT) * 0.5 - paddingBottom;

        drawingPane.relocate((getWidth() - ARTBOARD_WIDTH) * 0.5, y);
    }

    private void relocateDrawingPaneCenterTop(double scaleY, double paddingTop) {
        double visualHeight = ARTBOARD_HEIGHT * scaleY;
        double y            = (visualHeight - ARTBOARD_HEIGHT) * 0.5 + paddingTop;

        drawingPane.relocate((getWidth() - ARTBOARD_WIDTH) * 0.5, y);
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

    public int getCurrentSkyScrapperYear() {
        return currentSkyScrapperYear.get();
    }

    public IntegerProperty currentSkyScrapperYearProperty() {
        return currentSkyScrapperYear;
    }

    public void setCurrentSkyScrapperYear(int currentSkyScrapperYear) {
        this.currentSkyScrapperYear.set(currentSkyScrapperYear);
    }

    public int getCurrentSkyScrapperHeight() {
        return currentSkyScrapperHeight.get();
    }

    public IntegerProperty currentSkyScrapperHeightProperty() {
        return currentSkyScrapperHeight;
    }

    public void setCurrentSkyScrapperHeight(int currentSkyScrapperHeight) {
        this.currentSkyScrapperHeight.set(currentSkyScrapperHeight);
    }

}
