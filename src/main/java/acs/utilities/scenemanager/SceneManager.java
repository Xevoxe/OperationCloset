package acs.utilities.scenemanager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.HashMap;

public class SceneManager {

    private HashMap<String, SceneScreen<?>> scenes; //Holds list of scenes
    private Scene main;

    public SceneManager(){
        scenes = new HashMap<>();
    }

    public void loadScene(SceneScreen<?> screen) throws ExistingScene, IOException {
        if(!scenes.containsKey(screen.getName())){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + screen.getFxmlFileName()));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            screen.setScene(scene);
            screen.setController(loader.getController());

            scenes.put(screen.getName(),screen);
        }
       else throw new ExistingScene();
    }

    public void removeScene(String name) throws MissingScene {
        if(scenes.containsKey(name)) {
            scenes.remove(name);
        }else throw new MissingScene();
    }

    public void setScene(String name) throws MissingScene {
        if(scenes.containsKey(name)) {
            main.setRoot(scenes.get(name).getScene().getRoot());
        }else throw new MissingScene();
    }



    public static class MissingScene extends Exception{
        public MissingScene(){
            super("Scene not loaded!");
        }
    }
    public static class ExistingScene extends Exception{
        public ExistingScene(){
            super("Scene by that name has already been loaded!");
        }
    }
}
