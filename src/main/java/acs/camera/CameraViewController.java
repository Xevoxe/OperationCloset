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

    private VideoCapture videoCapture; //Captures the video
    private int cameraId;   // Id of the camera to use
    private ScheduledExecutorService videoService = null; //Thread to update Camera View


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //Loads in images
        setUpScene();

        //Sets up video capture
        setupVideoCapture();

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

    private void setupVideoCapture(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); //Load native OpenCV Library

        this.videoCapture = new VideoCapture();

        //TODO Change the 0 to variable of chosen camera
        this.videoCapture.open(0,Videoio.CAP_DSHOW);

        //Set the maximum resolution possible for camera being used
        this.videoCapture.set(3,10000);
        this.videoCapture.set(4,10000);

        //Get aspect ratio of camera
        System.out.println("Aspect Ratio: " + this.videoCapture.get(3) + " X " + this.videoCapture.get(4));


        //TODO add Exception that another program might be using camera.
        if(this.videoCapture.isOpened()) {
            System.out.println("Video Stream available");

            /**
             * Creates a thread that will get a Frame every 30 seconds
             */
            Runnable getFrames = () -> {
                //Prevent race condition in case of camera switch
                if (this.videoCapture.isOpened()) {

                    //Re-enable button after camera switch
                    this.changeCameraBtn.setDisable(false);
                    Mat frame = getFrame();

                    //Crop Frame
                    int centerX = frame.width() / 2;
                    int centerY = frame.height() / 2;

//                System.out.println("X: " + centerX + "Y: " + centerY);

                    //TODO: Uncomment following lines only there to handle different aspect rations to show image.
//                Rect rectCrop = new Rect(centerX - 247 ,0,495,720);
//                frame = frame.submat(rectCrop);


                    BufferedImage image = new BufferedImage(frame.width(), frame.height(), BufferedImage.TYPE_3BYTE_BGR);
                    final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                    frame.get(0, 0, pixels);

                    //Update ImageView on FXThread
                    Platform.runLater(() -> {
                        videoFrame.setImage(SwingFXUtils.toFXImage(image, null));
                    });
                }
            };

            this.videoService = Executors.newSingleThreadScheduledExecutor();
            this.videoService.scheduleAtFixedRate(getFrames, 0, 33, TimeUnit.MILLISECONDS);
        }

    }


    /**
     * Get a frame from the opened video stream if available
     * @return the frame to show
     */
    private Mat getFrame() {

        Mat frame = new Mat();

        if (this.videoCapture.isOpened()) {
            //Read the current frame
            this.videoCapture.read(frame);

        }

        return frame;
    }

    /**
     * Toggles between front and back cameras.
     */
    @FXML
    private void toggleCamera(){
        this.cameraId = this.cameraId == 0 ? 1 : 0;

        this.videoCapture.open(this.cameraId,Videoio.CAP_DSHOW); //Open new camera Id

        //Disable this button until new camera is loaded.
        this.changeCameraBtn.setDisable(true);
    }


    public void shutdown() {
        System.out.println("Shutdown Video Service and release camera");
        this.videoCapture.release();
        this.videoService.shutdown();

        try {
            if(!this.videoService.awaitTermination((long) 41.66,TimeUnit.MILLISECONDS)){
                System.out.println("Shutdown Video Service Now");
                this.videoService.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void captureImage(ActionEvent actionEvent) {

    }
}
