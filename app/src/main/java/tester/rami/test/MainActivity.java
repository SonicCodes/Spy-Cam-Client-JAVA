package tester.rami.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Base64;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Parameter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.Policy;

import static android.graphics.BitmapFactory.decodeByteArray;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;


public class MainActivity extends AppCompatActivity  {
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

Thread goer;
    int cur = 1;
     WebSocketServer cameraserver = null;
    boolean asserver = true;
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
                    rawImage = baos.toByteArray();

                    cameraserver.broadcast(rawImage);
                }catch (    Exception ex){
                    ex.printStackTrace();
                }


            }
        });
    }
    public static void requestSystemAlertPermission(Activity context, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        final String packageName = context == null ? context.getPackageName() : context.getPackageName();
        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName));
        if (context != null)
            context.startActivityForResult(intent, requestCode);
        else
            context.startActivityForResult(intent, requestCode);
    }
    @TargetApi(23)
    public static boolean isSystemAlertPermissionGranted(Context context) {
        final boolean result = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
        return result;
    }


boolean wif = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

if(!isSystemAlertPermissionGranted(this)){

    requestSystemAlertPermission(this,8);
};


        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }

      if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA},
                    1);
        }


    }
    SurfaceTexture texture;
    AudioRecord audioRecorder;

    public void textureup() {


try {
    cam = Camera.open(cur);
texture = new SurfaceTexture(19);
    try {

        cam.setPreviewTexture(texture);
        cam.setDisplayOrientation(90);
    } catch (IOException e) {
        e.printStackTrace();
    }
    camtopreview(cam);
    if(!wif){

        Camera.Parameters params = cam.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_OFF);
        cam.setParameters(params);
        wif = false;
    }else{
        Camera.Parameters params = cam.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_ON);
        cam.setParameters(params);
        wif  = true;
    }

    cam.startPreview();
}catch (Exception ex){
    if(cam != null){
        cam.stopPreview();
        cam.release();
        cam = null;
    }
ex.printStackTrace();
}

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }





    public boolean texturedown() {

cam.stopPreview();
cam.release();
cam = null;
texture.release();
        return true;
    }


}
