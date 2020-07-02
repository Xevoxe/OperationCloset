package acs.camera;

import acs.constants.CameraSettings;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CameraViewController implements Initializable {

    @FXML
    public Button changeCameraBtn; //Button that switches between cameras
    @FXML
    private ImageView videoFrame; //Holds the current frame from capture stream

    private int cameraId;   // Id of the camera to use
    VideoCaptureService videoCaptureService;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.cameraId = CameraSettings.DEFAULT_CAMERA;

        setUpScene();

        videoCaptureService = new VideoCaptureService(videoFrame);

        try {
            videoCaptureService.StartService(cameraId);
        } catch (VideoCaptureService.CameraInUse cameraInUse) {
            cameraInUse.printStackTrace();
        }
    }


    /**
     *  Loads in any images and sets up scene elements.
     */
    private void setUpScene(){

        //Load in Icons
        Image switchCamIcon = null;
        try {
            switchCamIcon = new Image(new FileInputStream("src/main/resources/images/camera.png"),50,50,true,true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        changeCameraBtn.setGraphic(new ImageView(switchCamIcon));


        //Setup ImageView
        videoFrame.setFitHeight(800);
        videoFrame.setFitWidth(495);
        videoFrame.setPreserveRatio(true);
    }


    /**
     * Toggles between front and back cameras.
     */
    @FXML
    private void toggleCamera(){
        this.cameraId = this.cameraId == 0 ? 1 : 0;

        this.videoCaptureService.setCamera(this.cameraId);
        //Open new camera Id

        //Disable this button until new camera is loaded.
//        this.changeCameraBtn.setDisable(true);
    }


    public void shutdown() {
        System.out.println("Shutdown Video Service and release camera");
        this.videoCaptureService.ShutDown();
    }

    public void captureImage(ActionEvent actionEvent) {

    }
}
