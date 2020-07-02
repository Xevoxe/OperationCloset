package acs.camera;

import acs.utilities.scenemanager.SceneManager;
import acs.utilities.scenemanager.SceneScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class  main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException, SceneManager.ExistingScene {

        SceneManager sceneManager = new SceneManager();

        SceneScreen<CameraViewController> cameraScene = new SceneScreen<>("cameraScene","CameraView.fxml");

        sceneManager.loadScene(cameraScene);

//        scene.setFill(Color.TRANSPARENT);
//        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(cameraScene.getScene());
        primaryStage.setOnCloseRequest((e)->{
            CameraViewController c = cameraScene.getController();
            c.shutdown();
            Platform.exit();
        });
        primaryStage.show();
    }
}
