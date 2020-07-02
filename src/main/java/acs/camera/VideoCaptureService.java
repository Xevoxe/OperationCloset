package acs.camera;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class VideoCaptureService {

    private int selectedCamera; //Index of the selected camera


    private VideoCapture vCapture;
    private final Consumer<Mat> matConsumer; //Consume Mat from CapturedThread
    private CaptureThread captureThread;    //Separate Thread to Capture images from camera
    ScheduledExecutorService threadService;


    public VideoCaptureService(ImageView imageView){

        this.selectedCamera = 0; //Select Default Camera

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); //Load native OpenCV Library

        this.vCapture = new VideoCapture();

        //Create Consumer
        matConsumer = mat -> {

            BufferedImage image = createImageFromMat(mat);
            //Set JavaFX Element Image to Frame
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
        };

    }

    public void StartService(int cameraIndex) throws CameraInUse {
        if(this.vCapture.open(cameraIndex, Videoio.CAP_DSHOW)){
            this.selectedCamera = cameraIndex;
            captureThread = new CaptureThread(matConsumer,vCapture);

            //Start the Thread
            //Executes captureThread
            threadService = Executors.newSingleThreadScheduledExecutor();
            threadService.scheduleAtFixedRate(captureThread,0,33, TimeUnit.MILLISECONDS);
        }else{
            throw new CameraInUse();
        }
    }

    /**
     * Shut down Video Capture thread and release camera
     */
    public void ShutDown(){
        this.vCapture.release();
        this.captureThread.ShutDown(); //Ensure captureThread is running
        this.threadService.shutdown();

        try {
            if(!this.threadService.awaitTermination((long) 41.66,TimeUnit.MILLISECONDS)){
                System.out.println("Shutdown Video Service Now");
                this.threadService.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setCamera(int index){
        if(index != selectedCamera){
            this.selectedCamera = index;
            this.vCapture.open(selectedCamera,Videoio.CAP_DSHOW);
        }
    }

    /**
     * Pause Video Capture
     * @throws InterruptedException
     */
    public void PauseVideoCapture() throws InterruptedException {
        captureThread.PauseThread();
    }

    /**
     * Restart video capture
     */
    public void ResumeVideoCapture(){
        captureThread.ResumeThread();
    }

    private BufferedImage createImageFromMat(Mat frame){
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

        return image;
    }

    /**
     * Separate thread to capture images from selected camera
     */
    private static class CaptureThread implements Runnable {

        boolean suspended;
        Consumer<Mat> matConsumer; // Handle new frame.
        VideoCapture vCapture;

        /**
         *
         * @param sourceConsumer Callback consumer for JavaFXThread
         * @param vCapture VideoCapture from OpenCV
         */
        public CaptureThread(Consumer<Mat> sourceConsumer,VideoCapture vCapture) {
            this.vCapture = vCapture; //Instantiate a new video capture object
            this.matConsumer = sourceConsumer;
        }
        @Override
        public void run() {

            synchronized (this){
                while (suspended) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (this.vCapture.isOpened()) {
                Mat frame = new Mat();
                this.vCapture.read(frame); //Get frame from camera

                //Handle new Mat on JavaFX Thread
                Platform.runLater(() -> {
                    matConsumer.accept(frame); //JavaFX Thread consumes mat.
                });
            }
        }

        public void PauseThread(){
            suspended = true;
        }

        public synchronized void ResumeThread(){
            suspended = false;
            notify();
        }

        public void ShutDown(){
            if (suspended) {
                ResumeThread();
            }
        }

    }

    public static class CameraInUse extends Exception{
        public CameraInUse(){
            super("Camera in use by another application");
        }
    }
}
