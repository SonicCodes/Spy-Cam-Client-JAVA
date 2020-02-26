package tester.rami.test;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executor;

import androidx.constraintlayout.widget.ConstraintLayout;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

public class cli extends Service implements SurfaceHolder.Callback{
    public cli() {
  
    }
    static String b ()throws Exception {

        return  InetAddress.getLocalHost().getHostAddress();
    }
    ConstraintLayout lmainpage;
    Camera cam ;
    private static final int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 20; // Milliseconds
    private static final int SAMPLE_SIZE = 2; // Bytes
    private static final int BUF_SIZE = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2; //Bytes
    private Bitmap rotateBitmap(YuvImage yuvImage, Rect rectangle) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(rectangle, 100, os);

        byte[] bytes = os.toByteArray();
        os.close();
        return   BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    boolean wif = false;
    Thread goer;
    int cur = 1;
    WebSocketServer cameraserver = null;
    boolean asserver = true;
    private void switchCamera() {
        if (cam != null) {
            cam.setPreviewCallback(null);
            cam.stopPreview();
            cam.release();
            cam = null;
            try {

                cam = Camera.open(cur);
                cam.setDisplayOrientation(90);
                cam.setPreviewDisplay(holded);
                camtopreview(cam);

                cam.startPreview();
            }
            catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
    void camtopreview(Camera cam){
        cam.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {

                try     {
                    Camera.Size previewSize = camera.getParameters().getPreviewSize();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] rawImage = null;

                    // Decode image from the retrieved buffer to JPEG
                    YuvImage yuv = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                    yuv.compressToJpeg(new Rect(0, 0, previewSize.width,  previewSize.height), 40, baos);
                    rawImage = baos.toByteArray();

                    cameraserver.broadcast(rawImage);
                }catch (    Exception ex){
                    ex.printStackTrace();
                }


            }
        });
    }

    public void show(){

        WindowManager windowManager;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

//Type overlay
        WindowManager.LayoutParams params = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY ,
                    FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT
                    );
        }else{
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY ,
                    FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT
            );
        }

        params.width = 1;
params.height = 1;
        final SurfaceView myprof = new SurfaceView(this);
 windowManager.addView(myprof,params);
      holded =  myprof.getHolder();

        holded.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holded.addCallback(this);



        //Throught websocet

        Toast.makeText(this, "Here2", Toast.LENGTH_SHORT).show();
        final WebSocketServer audioserver = new WebSocketServer(new InetSocketAddress(9676)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
             //   audiobroadcast.resume();
            }@Override public void onClose(WebSocket conn, int code, String reason, boolean remote) {
              //   audiobroadcast.suspend();
            }@Override public void onMessage(WebSocket conn, String message) { }@Override public void onError(WebSocket conn, Exception ex) { }@Override public void onStart() { }
        };
        final Thread audiothread = new Thread(new Runnable() {
            @Override
            public void run() {
                audioserver.start();
            }
        });
        audiothread.start();
        final int[] b = {0};
        final Thread[] cambroadcast = {null};
        cameraserver = new WebSocketServer(new InetSocketAddress(9682)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
b[0] +=1;
                Log.e("Started","Client on");
                if (cam != null) {
                    cam.setPreviewCallback(null);
                    cam.stopPreview();
                    cam.release();
                    cam = null;

                    try {

                        cam = Camera.open(cur);
                        cam.setDisplayOrientation(90);
                        cam.setPreviewDisplay(holded);
                        camtopreview(cam);

                        cam.startPreview();
                    }
                    catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            }@Override public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                b[0] -=1;          
                if(b[0]==0){
                    Log.e("Finished","Client off");
                    if (cam != null) {
                        cam.setPreviewCallback(null);
                        cam.stopPreview();
                        cam.release();
                        cam = null;

                    }
                }

            }@Override public void onMessage(WebSocket conn, String message) {
                Camera.Parameters param =  cam.getParameters();
                holded =  myprof.getHolder();
                if(message.equals("lightoff")){
                    cam.setOneShotPreviewCallback(null);
                   cam.stopPreview();
                    Camera.Parameters params = cam.getParameters();
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    cam.setParameters(params);
                    wif = false;
                    try {
                        cam.setPreviewDisplay(holded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camtopreview(cam);
                    cam.startPreview();
                }
                if(message.equals("lighton")){
                    cam.setOneShotPreviewCallback(null);
                    cam.stopPreview();
                    Camera.Parameters params = cam.getParameters();
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    cam.setParameters(params);
                    wif  = true;
                    try {
                        cam.setPreviewDisplay(holded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camtopreview(cam);
                    cam.startPreview();
                }



                if(message.equals("back")) {

                    cur =0;
switchCamera();
                }
                if(message.equals("front")){

                    cur =  1;
              switchCamera();
                }
            }@Override public void onError(WebSocket conn, Exception ex) { }@Override public void onStart() {
                if (cam != null) {
                    cam.setPreviewCallback(null);
                    cam.stopPreview();
                    cam.release();
                    cam = null;

                }
            }
        };
        final Thread camthread = new Thread(new Runnable() {
            @Override
            public void run() {
                cameraserver.start();
            }
        });
        camthread.start();
        final AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 400, AudioTrack.MODE_STREAM);
        final byte[][] gotted = {null};



        final WebSocketServer spreakerserver = new WebSocketServer(new InetSocketAddress(9679)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
            track.play();

            }@Override public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                track.stop();

            }@Override public void onMessage(WebSocket conn, String message) {
                gotted[0] = message.getBytes();
                Log.e("Audio recieved:",String.valueOf(gotted[0].length));
                track.write(gotted[0],0, gotted[0].length);


            }@Override public void onError(WebSocket conn, Exception ex) { }@Override public void onStart() { }
        };
        final Thread spreakthread = new Thread(new Runnable() {
            @Override
            public void run() {
                spreakerserver.start();
            }
        });
        spreakthread.start();

//Start server thread
         audiobroadcast = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean mic = true;
                audioRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)*10);
                int bytes_read = 0;
                int bytes_sent = 0;

                try {
                    audioRecorder.startRecording();
                    while(mic) {
                        // Capture audio from the mic and transmit it
                        byte[] buf = new byte[BUF_SIZE];
                        bytes_read = audioRecorder.read(buf, 0, BUF_SIZE);
                        audioserver.broadcast(buf);
                        bytes_sent += bytes_read;
                        Thread.sleep(SAMPLE_INTERVAL, 0);
                    }
                    // Stop recording and release resources
                    audioRecorder.stop();
                    audioRecorder.release();

                    mic = false;
                    return;
                }
                catch(Exception e) {


                }
            }
        });
        audiobroadcast.start();


    }
    AudioRecord audioRecorder;
    Thread audiobroadcast;
    @Override
    public void onCreate() {
            super.onCreate();
        show();
       // Toast.makeText(this, "How are you", Toast.LENGTH_SHORT).show();
        WebSocketServer server = new WebSocketServer(new InetSocketAddress(9324)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                getApplicationContext().startActivity(intent);
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {

            }

            @Override
            public void onMessage(WebSocket conn, String message) {

            }

            @Override
            public void onError(WebSocket conn, Exception ex) {

            }

            @Override
            public void onStart() {

            }
        };
        server.start();
    }
SurfaceTexture texture;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    SurfaceHolder holded;
    public void textureup() {


            cam = Camera.open(cur);

            try {

                cam.setPreviewDisplay(holded);
                cam.setDisplayOrientation(90);
            } catch (IOException e) {
                e.printStackTrace();
            }
    //      camtopreview(cam);

  //     cam.startPreview();



    }

    public boolean texturedown() {

        cam.stopPreview();
        cam.release();

        return true;
    }

    /**
     * This is called immediately after the surface is first created.
     * Implementations of this should start up whatever rendering code
     * they desire.  Note that only one thread can ever draw into
     * a {@link Surface}, so you should not draw into the Surface here
     * if your normal rendering will be in another thread.
     *
     * @param holder The SurfaceHolder whose surface is being created.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
holded = holder;
textureup();
    }

    /**
     * This is called immediately after any structural changes (format or
     * size) have been made to the surface.  You should at this point update
     * the imagery in the surface.  This method is always called at least
     * once, after {@link #surfaceCreated}.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param width  The new width of the surface.
     * @param height The new height of the surface.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try     {
            Camera.Parameters parameters = cam.getParameters();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            if(!wif){

                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                wif = false;
            }else{

                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                wif  = true;
            }

            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size previewSize = previewSizes.get(1);
            parameters.setPreviewSize(previewSize.width,previewSize.height);
            parameters.setPreviewFormat(ImageFormat.NV21);

            try {
                cam.setParameters(parameters);
            }catch (    Exception ex){

            }

          //  cam.startPreview();
        }catch (    Exception ex){
            ex.printStackTrace();
        }




    }

    /**
     * This is called immediately before a surface is being destroyed. After
     * returning from this call, you should no longer try to access this
     * surface.  If you have a rendering thread that directly accesses
     * the surface, you must ensure that thread is no longer touching the
     * Surface before returning from this function.
     *
     * @param holder The SurfaceHolder whose surface is being destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
