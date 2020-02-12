package com.example.myapplicationv3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    // all the permissions the app could ask for are stored in an Arraylist
    private static final String [] PERMISSIONS = {
            Manifest.permission.CAMERA
    };


    // the number value doesnt matter, its just a code for comparision
    private static final int REQUEST_PERMISSIONS = 53;


    // store the number of needed/possible permissions
    private static final int PERMISSONS_COUNT = 1;


    // check method: checks if the permissions are given or not
    @SuppressLint("NewApi")
    private boolean arePermissionsDenied(){
    for (int i = 0; i<PERMISSONS_COUNT; i++){
        if(checkSelfPermission(PERMISSIONS [i]) != PackageManager.PERMISSION_GRANTED)
            // return that the permissions are denied
            return true;
    }
    // return that all permissions are granted / no permission is denied
        return false;
    }


    // if permissions are requested, the user gets an on screen information, here he read the output
    // of this onscreen menu
    @SuppressLint("NewApi")
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // check if the user has given permission for the app
        if(requestCode == REQUEST_PERMISSIONS && grantResults.length > 0){
            if (arePermissionsDenied()) {
                // close the app and keep aksing the user for permission every time he opend the app
                ((ActivityManager) (this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
                recreate();
            }
            else{
                // this event occurs when permission is granted
                onResume();
            }
        }
    }


    // check if the camera is initialized
    private boolean isCameraInitilaized;

    // Camera object
    private Camera mCamera = null;

    private static SurfaceHolder myHolder;

    // for the created preview
    private static CameraPreview mPreview;

    // make a Layer that is visible to the user
    private FrameLayout preview;

    private Button flashButton;

    private static OrientationEventListener orientationEventListener = null;

    private static boolean focusMode;


    @Override
    protected void onResume() {
        super.onResume();
        // check if the software version is greater than marshmallow
        // Version newer than marshallow ask the user for permission
        // only ask for permissions when both of the statements are true which means a permission
        // request is needed
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied()){
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }
        // when the camera is not initialized
        if(!isCameraInitilaized){
            // Especially this line of code is needed for the app to work,otherwise the app wont
            // start at all.
            // @ 1:10:04
            mCamera = Camera.open();
            mPreview = new CameraPreview(this, mCamera);
            // add the preview to the xml layout
            preview = findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            rotateCamera();
            // set the flashbutton to the id from the xml
            flashButton = findViewById(R.id.flash);
            if(hasFlash()){
                flashButton.setVisibility(View.VISIBLE);
            }
            else {
                flashButton.setVisibility(View.GONE);
            }
            // check if the Handy is rotating and then change the rotation, if neccesary
            // @59:33
            orientationEventListener = new OrientationEventListener(this) {
                @Override
                public void onOrientationChanged(int orientation) {
                    rotateCamera();
                }
            };
            orientationEventListener.enable();
            // when the user is longClicking the image on his screen
            // the focus mode should change, if possible
            // @1:02:26
            preview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(whichCamera) {
                        if( focusMode){
                            p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }   else{
                          p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        }
                        try {
                            mCamera.setParameters(p);
                        }catch (Exception e){
                        }
                        focusMode = ! focusMode;
                    }
                    return true;
                }
            });
        }
    }

    private static List<String> camEffects;

    // check if the camera has a flash light
    private static boolean hasFlash(){
        camEffects = p.getSupportedColorEffects();
        final List<String> flashModes = p.getSupportedFlashModes();
        if (flashModes == null){
            return false;
        }
        // @ 54:02
        for (String flashMode:flashModes){
            if(Camera.Parameters.FLASH_MODE_ON.equals(flashMode)){
                return true;
            }
        }
        return false;
    }


    // static stuff improves the performance
    private static int rotation;

    private static boolean whichCamera = true;

    private static Camera.Parameters p;

    // @ 42:28
    private void rotateCamera(){
        if (mCamera!=null){
            rotation = this.getWindowManager().getDefaultDisplay().getRotation();
            if(rotation == 0) {
                rotation = 90;
            } else if (rotation == 1){
                rotation = 0;
            } else if (rotation == 2){
                rotation = 270;
            } else {
                rotation = 180;
            }
            mCamera.setDisplayOrientation(rotation);
            // when whichCamera is fals
            if(!whichCamera){
                if(rotation == 90){
                    rotation = 270;
                }else if (rotation == 270){
                    rotation = 90;
                }
            }

            p = mCamera.getParameters();
            p.setRotation(rotation);
            mCamera.setParameters(p);
        }

    }



    // to improve the perfomance, you can make the class static
    private static class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

        private static SurfaceHolder mHolder;
        private static Camera mCamera;

        // constructor method
        private  CameraPreview (Context context, Camera camera) {
            super (context);
            mCamera = camera;
            mHolder = getHolder();
            // here we implement the callback in the surfaceHOlder class
            // https://www.youtube.com/watch?v=u4hfZXorDAQ&t=632s
            // @ min 32:11
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        // i think this is necessary so that the user can see the camera preview
        public void surfaceCreated (SurfaceHolder holder) {
            myHolder = holder;
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

       public void surfaceDestroyed(SurfaceHolder holder){

       }

       public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){

       }

    }


    // create some interaction features



}
