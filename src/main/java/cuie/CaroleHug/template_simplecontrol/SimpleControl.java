package cuie.CaroleHug.template_simplecontrol;

import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cuie.CaroleHug.template_simplecontrol.demo.Building;
import cuie.CaroleHug.template_simplecontrol.demo.PresentationModel;
import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
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

    private static final double ARTBOARD_WIDTH  = 1000;  // Todo: Breite der "Zeichnung" aus dem Grafik-Tool übernehmen
    private static final double ARTBOARD_HEIGHT = 500;  // Todo: Anpassen an die Breite der Zeichnung

    private static final double ASPECT_RATIO = ARTBOARD_WIDTH / ARTBOARD_HEIGHT;

    private static final double MINIMUM_WIDTH  = 500;    // Todo: Anpassen
    private static final double MINIMUM_HEIGHT = MINIMUM_WIDTH / ASPECT_RATIO;

    private static final double MAXIMUM_WIDTH = 800;    // Todo: Anpassen

    // Todo: diese Parts durch alle notwendigen Parts der gewünschten CustomControl ersetzen
    private Circle backgroundCircle;

    private Line arrow_line;
    private Line arrow_line_up;
    private Line arrow_line_down;
    private Text construction_year_label;
    private Text height_label;
    private Canvas canvas_skyScrappers;

    private int MAX_BUILD_YEAR;
    private int MIN_BUILD_YEAR;
    private double MAX_HEIGHT;

    private Text   display;

    // Todo: ersetzen durch alle notwendigen Properties der CustomControl
    private final DoubleProperty value = new SimpleDoubleProperty();

    // Todo: ergänzen mit allen  CSS stylable properties
    private static final CssMetaData<SimpleControl, Color> BASE_COLOR_META_DATA = FACTORY.createColorCssMetaData("-base-color", s -> s.baseColor);

    private final StyleableObjectProperty<Color> baseColor = new SimpleStyleableObjectProperty<Color>(BASE_COLOR_META_DATA) {
        @Override
        protected void invalidated() {
            setStyle(getCssMetaData().getProperty() + ": " + colorToCss(get()) + ";");
            applyCss();
        }
    };

    // Todo: Loeschen falls keine getaktete Animation benoetigt wird
    private final BooleanProperty          blinking = new SimpleBooleanProperty(false);
    private final ObjectProperty<Duration> pulse    = new SimpleObjectProperty<>(Duration.seconds(1.0));

    private final AnimationTimer timer = new AnimationTimer() {
        private long lastTimerCall;

        @Override
        public void handle(long now) {
            if (now > lastTimerCall + (pulse.get().toMillis() * 1_000_000L)) {
                performPeriodicTask();
                lastTimerCall = now;
            }
        }
    };

    // Todo: alle Animationen und Timelines deklarieren


    // needed for resizing
    private Pane drawingPane;
    private PresentationModel presentationModel;

    public SimpleControl(PresentationModel pm) {
        presentationModel = pm;
        initializeSelf();
        initializeParts();
        initializeDrawingPane();
        initializeAnimations();
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
        double center = ARTBOARD_WIDTH * 0.5;

        backgroundCircle = new Circle(center, center, center);
        backgroundCircle.getStyleClass().add("background-circle");

        arrow_line = new Line( ARTBOARD_WIDTH-10,ARTBOARD_HEIGHT-50, 0, ARTBOARD_HEIGHT-50);
        arrow_line.getStyleClass().add("arrow_line");

        arrow_line_up = new Line( ARTBOARD_WIDTH,ARTBOARD_HEIGHT-50, ARTBOARD_WIDTH - 50, ARTBOARD_HEIGHT-100);
        arrow_line_up.getStyleClass().add("arrow_line");

        arrow_line_down = new Line( ARTBOARD_WIDTH - 50,ARTBOARD_HEIGHT, ARTBOARD_WIDTH, ARTBOARD_HEIGHT-50);
        arrow_line_down.getStyleClass().add("arrow_line");

        construction_year_label = new Text("Baujahr");
        construction_year_label.getStyleClass().add("labels");
        construction_year_label.setX(ARTBOARD_WIDTH - 125);
        construction_year_label.setY(ARTBOARD_HEIGHT-20);

        height_label = new Text("Höhe");
        height_label.getStyleClass().add("labels");
        height_label.setX(-55);
        height_label.setY(ARTBOARD_HEIGHT/4);

        canvas_skyScrappers = new Canvas(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
        GraphicsContext gc = canvas_skyScrappers.getGraphicsContext2D();
        canvas_skyScrappers.getStyleClass().add("pointOnTimeline");
        drawSkyScrappers(gc);



        display = createCenteredText("display");
    }

    private void drawSkyScrappers(GraphicsContext gc) {
        findMinAndMaxYear();
        findMaxHeight();
        System.out.println(MAX_BUILD_YEAR);
        System.out.println(MIN_BUILD_YEAR);
        gc.setStroke(Color.web("#089990"));
        for(Building skyScrapper : presentationModel.getSkyScrappers()) {
            double pointOnTimeline = ((arrow_line.getStartX() - arrow_line.getEndX()-50) * (skyScrapper.getBuild()-MIN_BUILD_YEAR))/(MAX_BUILD_YEAR - MIN_BUILD_YEAR);
            double skyScrapperHeight = (((ARTBOARD_HEIGHT-50)*skyScrapper.getHeightM()) / MAX_HEIGHT);
            System.out.println(skyScrapperHeight);
            gc.strokeLine(pointOnTimeline, skyScrapperHeight, pointOnTimeline, ARTBOARD_HEIGHT-50);
        }
    }

    private void findMinAndMaxYear () {
        MAX_BUILD_YEAR = Calendar.getInstance().get(Calendar.YEAR);
        MIN_BUILD_YEAR = MAX_BUILD_YEAR;
        for(Building skyScrapper : presentationModel.getSkyScrappers()) {
            if(skyScrapper.getBuild() < MIN_BUILD_YEAR && skyScrapper.getBuild() != 0) {
                MIN_BUILD_YEAR = skyScrapper.getBuild();
            }
            if(skyScrapper.getBuild()>MAX_BUILD_YEAR) {
                MAX_BUILD_YEAR = skyScrapper.getBuild();
            }
        }
    }

    private void findMaxHeight() {
        for(Building skyScrapper : presentationModel.getSkyScrappers()) {
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

    private void initializeAnimations(){
        //ToDo: alle deklarierten Animationen initialisieren
    }

    private void layoutParts() {
        // ToDo: alle Parts zur drawingPane hinzufügen
        //drawingPane.getChildren().addAll(arrow_line, arrow_line_up, arrow_line_down, construction_year_label, height_label,canvas_skyScrappers, display);
        drawingPane.getChildren().addAll(arrow_line, arrow_line_up, arrow_line_down, construction_year_label, height_label,canvas_skyScrappers);

        getChildren().add(drawingPane);
    }

    private void setupEventHandlers() {
        //ToDo: bei Bedarf ergänzen
    }

    private void setupValueChangeListeners() {
        //ToDo: bei Bedarf ergänzen

        // fuer die getaktete Animation
        blinking.addListener((observable, oldValue, newValue) -> {
            startClockedAnimation(newValue);
        });
    }

    private void fillSkyScrapperList() {

    }


    private void setupBindings() {
        //ToDo dieses Binding ersetzen
        display.textProperty().bind(valueProperty().asString(CH, "%.2f"));
    }

    private void performPeriodicTask(){
        //todo: ergaenzen mit dem was bei der getakteten Animation gemacht werden muss
        // in der Regel: den Wert einer der Status-Properties aendern
    }

    private void startClockedAnimation(boolean start) {
        if (start) {
            timer.start();
        } else {
            timer.stop();
        }
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

    //ToDo: diese Funktionen anschauen und für die Umsetzung des CustomControls benutzen

    private double percentageToValue(double percentage, double minValue, double maxValue){
        return ((maxValue - minValue) * percentage) + minValue;
    }

    private double valueToPercentage(double value, double minValue, double maxValue) {
        return (value - minValue) / (maxValue - minValue);
    }

    private double valueToAngle(double value, double minValue, double maxValue) {
        return percentageToAngle(valueToPercentage(value, minValue, maxValue));
    }

    private double radialMousePositionToValue(double mouseX, double mouseY, double cx, double cy, double minValue, double maxValue){
        double percentage = angleToPercentage(angle(cx, cy, mouseX, mouseY));

        return percentageToValue(percentage, minValue, maxValue);
    }

    private double angleToPercentage(double angle){
        return angle / 360.0;
    }

    private double percentageToAngle(double percentage){
        return 360.0 * percentage;
    }

    private double angle(double cx, double cy, double x, double y) {
        double deltaX = x - cx;
        double deltaY = y - cy;
        double radius = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        double nx     = deltaX / radius;
        double ny     = deltaY / radius;
        double theta  = Math.toRadians(90) + Math.atan2(ny, nx);

        return Double.compare(theta, 0.0) >= 0 ? Math.toDegrees(theta) : Math.toDegrees((theta)) + 360.0;
    }

    private Point2D pointOnCircle(double cX, double cY, double radius, double angle) {
        return new Point2D(cX - (radius * Math.sin(Math.toRadians(angle - 180))),
                           cY + (radius * Math.cos(Math.toRadians(angle - 180))));
    }

    private Text createCenteredText(String styleClass) {
        return createCenteredText(ARTBOARD_WIDTH * 0.5, ARTBOARD_HEIGHT * 0.5, styleClass);
    }

    private Text createCenteredText(double cx, double cy, String styleClass) {
        Text text = new Text();
        text.getStyleClass().add(styleClass);
        text.setTextOrigin(VPos.CENTER);
        text.setTextAlignment(TextAlignment.CENTER);
        double width = cx > ARTBOARD_WIDTH * 0.5 ? ((ARTBOARD_WIDTH - cx) * 2.0) : cx * 2.0;
        text.setWrappingWidth(width);
        text.setBoundsType(TextBoundsType.VISUAL);
        text.setY(cy);
        text.setX(cx - (width / 2.0));

        return text;
    }

    private Group createTicks(double cx, double cy, int numberOfTicks, double overallAngle, double tickLength, double indent, double startingAngle, String styleClass) {
        Group group = new Group();

        double degreesBetweenTicks = overallAngle == 360 ?
                                     overallAngle /numberOfTicks :
                                     overallAngle /(numberOfTicks - 1);
        double outerRadius         = Math.min(cx, cy) - indent;
        double innerRadius         = Math.min(cx, cy) - indent - tickLength;

        for (int i = 0; i < numberOfTicks; i++) {
            double angle = 180 + startingAngle + i * degreesBetweenTicks;

            Point2D startPoint = pointOnCircle(cx, cy, outerRadius, angle);
            Point2D endPoint   = pointOnCircle(cx, cy, innerRadius, angle);

            Line tick = new Line(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
            tick.getStyleClass().add(styleClass);
            group.getChildren().add(tick);
        }

        return group;
    }

//    private Group createNumberedTicks(double cx, double cy, int numberOfTicks, double overallAngle, double tickLength, double indent, double startingAngle, String styleClass) {
//            Group group = new Group();
//
//            int width = 30;
//            double degreesBetweenTicks = overallAngle == 360 ?
//                    overallAngle / numberOfTicks :
//                    overallAngle / (numberOfTicks - 1);
//            double outerRadius = Math.min(cx - width, cy - width) - indent;
//            double innerRadius = Math.min(cx - width, cy - width) - indent - tickLength;
//
//            for (int i = 0; i < numberOfTicks; i++) {
//                double angle = 180 + startingAngle + i * degreesBetweenTicks;
//
//                if (i % 5 == 0 && i % 2 != 0) {
//                    Point2D startPoint = pointOnCircle(cx, cy, outerRadius, angle);
//                    Point2D endPoint = pointOnCircle(cx, cy, innerRadius * 0.95, angle);
//                    Line tick = new Line(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
//                    tick.getStyleClass().add(styleClass);
//                    group.getChildren().add(tick);
//                }
//                if (i % 10 == 0) {
//                    Point2D startPoint = pointOnCircle(cx, cy, outerRadius, angle);
//                    Point2D endPoint = pointOnCircle(cx, cy, innerRadius * 0.9, angle);
//
//                    Line bigTick = new Line(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
//                    bigTick.getStyleClass().add(styleClass);
//                    group.getChildren().add(bigTick);
//
//                    Point2D textPosition = pointOnCircle(cx - 7, cy + 5, innerRadius * 0.8, angle);
//                    Text number = new Text(textPosition.getX(), textPosition.getY(), Integer.toString(i));
//                    number.getStyleClass().add("tick-number");
//                    group.getChildren().add(number);
//
//                } else {
//                    Point2D startPoint = pointOnCircle(cx, cy, outerRadius, angle);
//                    Point2D endPoint = pointOnCircle(cx, cy, innerRadius, angle);
//                    Line tick = new Line(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
//                    tick.getStyleClass().add(styleClass);
//                    group.getChildren().add(tick);
//                }
//            }
//
//            return group;
//        }

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

    // alle getter und setter  (generiert via "Code -> Generate... -> Getter and Setter)

    // ToDo: ersetzen durch die Getter und Setter Ihres CustomControls
    public double getValue() {
        return value.get();
    }

    public DoubleProperty valueProperty() {
        return value;
    }

    public void setValue(double value) {
        this.value.set(value);
    }

    public Color getBaseColor() {
        return baseColor.get();
    }

    public StyleableObjectProperty<Color> baseColorProperty() {
        return baseColor;
    }

    public void setBaseColor(Color baseColor) {
        this.baseColor.set(baseColor);
    }

    public boolean isBlinking() {
        return blinking.get();
    }

    public BooleanProperty blinkingProperty() {
        return blinking;
    }

    public void setBlinking(boolean blinking) {
        this.blinking.set(blinking);
    }

    public Duration getPulse() {
        return pulse.get();
    }

    public ObjectProperty<Duration> pulseProperty() {
        return pulse;
    }

    public void setPulse(Duration pulse) {
        this.pulse.set(pulse);
    }
}
