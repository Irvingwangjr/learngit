package com.example.irvingwang.magic_gallery;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EventListener;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class generate extends AppCompatActivity {
    //add client class
    private static final String TAG = "generate";
    public  static  final int PICTURE_SAVED = 1;
    public  static  final int PICTURE_LOAD = 2;
    public static final int LABLE_RETURN =3;
    ProgressDialog progressDialog;

    ImageClient Client;
    int mode=0;
    FloatingActionButton left;
    FloatingActionButton right;
    FloatingActionButton compare;
    ImageButton save;
    ImageButton back_to_pic;
    ImageView pic;
    Uri data;


    Bitmap original;
    Bitmap rcnn;
    Bitmap cropped;


    TabLayout menu;
    TabLayout label;

    String delete_label;
    String[] type_list={"aaa","bbb","ccc","ddd","eeee","ffff"};
    int[] clicked_record;
    MotionEventCompat motionEventCompat;

    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case PICTURE_SAVED:
                    progressDialog.dismiss();
                    Intent intent=new Intent(generate.this,camera.class);
                    startActivity(intent);
                    break;
                case PICTURE_LOAD:
                    progressDialog.dismiss();
                    pic=(ImageView)findViewById(R.id.imageView);
                    pic.setImageBitmap(original);
                    pic.setAdjustViewBounds(true);
                    break;
                case LABLE_RETURN:
                    progressDialog.dismiss();
                    for (int i=0;i<=type_list.length-1;i++){
                        label.addTab(label.newTab());
                        label.getTabAt(i).setText(type_list[i]);
                        //IF YOU WANT TO SET THE PIC :USE SETTEXT.SETICON
                    }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent=getIntent();
        /*
        data=intent.getByteArrayExtra ("picture");
        original=BitmapFactory.decodeByteArray(data,0,data.length);
        */
        data=intent.getData();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    original= BitmapFactory.decodeStream(getContentResolver().openInputStream(data));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        progressDialog=new ProgressDialog(generate.this);
        progressDialog.setTitle("saving the picture");
        progressDialog.setMessage("saving....");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inpaint);
        Intent intent=getIntent();
       init();
    }

    private void init(){
        label=(TabLayout)findViewById(R.id.tabLayout);
        menu=(TabLayout)findViewById(R.id.tabLayout2);
        save=(ImageButton)findViewById(R.id.save);
        back_to_pic=(ImageButton)findViewById(R.id.back_to_pic);
        left=(FloatingActionButton)findViewById(R.id.floatingActionButton);

        left.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pic.setImageBitmap(original);
                return false;
            }
        });
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rcnn!=null)
                    pic.setImageBitmap(rcnn);
                else return;
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_image();
            }
        });
        label.setVisibility(View.INVISIBLE);
        label.addTab(label.newTab());
        label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i=(int)v.getTag();
                delete_label=type_list[i];
            }
        });


        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i=(int)v.getTag();
                switch (i){
                    case 0:{
                        mode=1;//reverse
                        break;
                    }
                    case 1:{
                        mode=2;
                        break;
                    }
                    case 2:{
                        pic.setImageBitmap(original);
                        break;
                    }
                }
            }
        });
        //upload the picture to the server here
        /*
        {}
        */


        back_to_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(generate.this,camera.class);
                startActivity(intent);
            }
        });

        pic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onSingleTap(event);
                return false;
            }
        });
    }



    String currentPhotoPath;
    private void save_image(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File save_file=createImageFile();
                    FileOutputStream fos = new FileOutputStream(save_file);
                    //通过io流的方式来压缩保存图片
                    boolean isSuccess = cropped.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                    Message message= new Message();
                    message.what=PICTURE_SAVED;
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(save_file);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        progressDialog=new ProgressDialog(generate.this);
        progressDialog.setTitle("saving the picture");
        progressDialog.setMessage("saving....");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        }
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void onSingleTap(MotionEvent e) {
        int dstX,dstY;
        // 获取触摸点的坐标 x, y
        float x = e.getX();
        float y = e.getY();
        // 目标点的坐标
        float dst[] = new float[2];
        // 获取到ImageView的matrix
        Matrix imageMatrix = pic.getImageMatrix();
        // 创建一个逆矩阵
        Matrix inverseMatrix = new Matrix();
        // 求逆，逆矩阵被赋值
        imageMatrix.invert(inverseMatrix);
        // 通过逆矩阵映射得到目标点 dst 的值
        inverseMatrix.mapPoints(dst, new float[]{x, y});
        dstX = (int) dst[0];
        dstY = (int) dst[1];
        clicked_record[clicked_record.length]=dstX;
        clicked_record[clicked_record.length]=dstY;
        String a="x="+String.valueOf(dstX)+"   y="+String.valueOf(dstY);
        // 判断dstX, dstY在Bitmap上的位置即可
    }
    private void ShowImg(final Bitmap img){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pic.setImageBitmap(img);
                Log.d(TAG, "run: show");
            }
        });
    }

}
