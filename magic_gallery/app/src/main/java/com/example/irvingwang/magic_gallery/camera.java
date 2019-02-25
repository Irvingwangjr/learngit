package com.example.irvingwang.magic_gallery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class camera extends AppCompatActivity{
    public final static int GALLARY_REQUEST_CODE=1;
    private static final String TAG="camera";
    private Camera mcamera;
    private CameraPreview mPreview;
    ImageButton capture;
    ImageButton album;
    Bitmap temp;
    Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
                Camera.Parameters parameters=camera.getParameters();
                int width=parameters.getPreviewSize().width;
                int height=parameters.getPreviewSize().height;
                mcamera.release();
                Bitmap bit = BitmapFactory.decodeByteArray(data,0,data.length);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bit.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Intent intent=new Intent(camera.this,generate.class);
                intent.putExtra("picture",byteArray);
                startActivity(intent);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mcamera=get_camera_instance();
        mcamera.setDisplayOrientation(90);
        mPreview=new CameraPreview(this,mcamera);
        FrameLayout preview=(FrameLayout)findViewById(R.id.preview);
        preview.addView(mPreview);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mcamera=get_camera_instance();
        mcamera.setDisplayOrientation(90);

        mPreview=new CameraPreview(this,mcamera);
        FrameLayout preview=(FrameLayout)findViewById(R.id.preview);
        preview.addView(mPreview);

        mPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //在这里实现对焦
                return false;
            }
        });

        capture=(ImageButton)findViewById(R.id.capture);
        album=(ImageButton)findViewById(R.id.album);
        capture.setBackgroundColor(Color.TRANSPARENT);
        album.setBackgroundColor(Color.TRANSPARENT);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mcamera.takePicture(null,null,mPicture);
            }
        });
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(camera.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {   //权限还没有授予，需要在这里写申请权限的代码
                    // 第二个参数是一个字符串数组，里面是你需要申请的权限 可以设置申请多个权限
                    // 最后一个参数是标志你这次申请的权限，该常量在onRequestPermissionsResult中使用到
                    ActivityCompat.requestPermissions(camera.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            GALLARY_REQUEST_CODE);

                }else { //权限已经被授予，在这里直接写要执行的相应方法即可
                    choosePhoto();
                }
            }
        });
    }

    private void choosePhoto(){
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型" 所有类型则写 "image/*"
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/jpeg");
        startActivityForResult(intentToPickPic, GALLARY_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == MainActivity.RESULT_OK) {
            switch (requestCode) {
                case GALLARY_REQUEST_CODE: {
                    // 获取图片
                    try {
                        //该uri是上一个Activity返回的
                        /*
                        Uri imageUri = data.getData();
                        if(imageUri!=null) {
                            mcamera.release();
                            Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bit.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            */
                        Uri image_uri=data.getData();
                        if (image_uri!=null){
                            Intent in1 = new Intent(this, generate.class);
                            in1.putExtra("picture",image_uri);
                            startActivity(in1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private int checkCameraHarware(Context context){
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return Camera.getNumberOfCameras();
        }
        else return 0;
    }

    public Camera get_camera_instance(){
        Camera c=null;
        int counter=checkCameraHarware(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 50);
        try {
            c=Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }catch (Exception e){
            Log.d(TAG, "get_camera_instance: "+ e.getMessage());
        }
        return c;
    }


}