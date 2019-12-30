package cuie.CaroleHug.template_simplecontrol.demo.View;

public interface ViewMixin {


    default void init() {
        initializeSelf();
        initializeControls();
        layoutControls();
        setupEventHandlers();
        setupValueChangedListeners();
        setupBindings();
        addEventHandlers();

    }




    default void initializeSelf(){
    }

    void initializeControls();

    void layoutControls();

    default void setupEventHandlers() {
    }

    default void setupValueChangedListeners() {
    }

    default void setupBindings() {
    }

    void addEventHandlers();


}
