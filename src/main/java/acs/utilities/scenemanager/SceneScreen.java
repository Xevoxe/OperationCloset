package acs.utilities.scenemanager;

import javafx.scene.Scene;

public class SceneScreen<T> {

    private final String name;
    private final String fxmlFileName;
    private T controller;

    private Scene scene;

    public SceneScreen(String name, String fxmlFileName){
        this.name = name;
        this.fxmlFileName = fxmlFileName;
    }

    public String getFxmlFileName() {
        return fxmlFileName;
    }

    public T getController() {
        return controller;
    }

    public Scene getScene() {
        return scene;
    }

    public String getName() {
        return name;
    }

    public void setController(T controller) {
        this.controller = controller;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
