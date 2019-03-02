package com.example.irvingwang.magic_gallery;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ImageClient {
    private static final String TAG = "ImageClient";
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    /*
     函数名:ConnectToServer
     功能：指定IP地址与端口号，与服务器建立连接
     返回值：无
     注意：此函数会让运行它的线程挂起，直到连接建立完成，请在非GUI线程中使用此函数。
     */
    public  void ConnectToServer(final String IpAddress,final int port){
        try {
            socket = new Socket(InetAddress.getByName(IpAddress), port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        }catch (UnknownHostException e){
            Log.e(TAG, "UnknowHostException");
            Log.i(TAG,Log.getStackTraceString(e));
        }catch (IOException e){
            Log.e(TAG, "ConnectIOException");
            Log.i(TAG,Log.getStackTraceString(e));
        }
    }

    /*
    函数名：SendRawImg
    功能:图像还原的第一步：发送原图给服务器
    返回值：返回此图片中存在的labels
    注意：此函数会让运行它的线程挂起，直到此次通信完成，请在非GUI线程中使用此函数。
    */
    public String[] SendRawImg(Bitmap rawImg){
        try {
            //发送状态数字1
            out.writeInt(1);

            //发图片
            sendImgMsg(out,rawImg);

            //收标签数目
            int LabelNum=in.readInt();

            //收标签
            String[] labels=new String[LabelNum];
            for(int i=0;i<LabelNum;i++){
                labels[i]=in.readUTF();
            }

            return labels;
        }catch (SocketException e){
            Log.e(TAG, "SocketException");
            Log.i(TAG,Log.getStackTraceString(e));
        }catch (IOException e){
            Log.e(TAG, "SendImgIOException");
            Log.i(TAG,Log.getStackTraceString(e));
        }
        return null;
    }

    /*
    函数名：SendCoordinate
    功能：图像还原的第二步，发送用户点击的坐标给服务器，服务器发回一张用户点击处物体被高亮的图片
    发送格式：第一个数表示坐标个数，之后先发横坐标再发纵坐标
        例如发送三个坐标 (1,2) (3,4) (5,6)
        发送格式为 3 1 2 3 4 5 6
    返回值：服务器返回的图片，图片中用户点击处的物体被高亮
    注意：此函数会让运行它的线程挂起，直到此次通信完成，请在非GUI线程中使用此函数。
    */
    public Bitmap SendCoordinate(ArrayList<Integer> PosLists){
        try {
            Log.e(TAG, "SendCoordinate: start" );

            //发送状态数字2
            out.writeInt(2);

            //发坐标
            for(int i=0;i<PosLists.size();i++)
                out.writeInt((int)PosLists.get(i));

            //收图片
            Bitmap HighlightImg=getImgMsg(in);

            return HighlightImg;
        }catch (SocketException e){
            Log.e(TAG, "SocketException");
            Log.i(TAG,Log.getStackTraceString(e));
        }catch (IOException e){
            Log.e(TAG, "IOException");
            Log.i(TAG,Log.getStackTraceString(e));
        }
        return null;
    }

    /*
    函数名：SendSelectedLable
    功能：发送label，受到一张所有lable类型的物品被高亮的图片
    返回值：被高亮的图片
    */
    public Bitmap SendSelectedLable(String label){
        try{
            Log.e(TAG, "SendSelectedLable: start" );
            //发状态字
            out.writeInt(3);

            //发送label
            out.writeUTF(label);

            //收图片
            Bitmap HighlightImg=getImgMsg(in);
            return HighlightImg;
        }catch (SocketException e){
            Log.e(TAG, "SocketException");
            Log.i(TAG,Log.getStackTraceString(e));
        }catch (IOException e){
            Log.e(TAG, "IOException");
            Log.i(TAG,Log.getStackTraceString(e));
        }
        return null;
    }


    /*
    函数名：RecieveFinalImg
    功能：图像还原的第三步，要求服务器还原图片，并接收
    返回值：被还原完成的图片
    注意：第三步的图像还原是基于第二步用户选择的物体进行的，即被抹掉的物体是第二步中用户选择的物体。
          此函数会让运行它的线程挂起，直到此次通信完成，请在非GUI线程中使用此函数
    */
    public Bitmap RecieveFinalImg(){
        try {
            Log.e(TAG, "RecieveFinalImg: start" );
            //送状态数字4
            out.writeInt(4);

            //接受最终的图片
            Bitmap FinalImg=getImgMsg(in);

            return FinalImg;
        }catch (SocketException e){
            Log.e(TAG, "SocketException");
            Log.i(TAG,Log.getStackTraceString(e));
        }catch (IOException e){
            Log.e(TAG, "IOException");
            Log.i(TAG,Log.getStackTraceString(e));
        }
        return null;
    }


    /*
    函数名：CloseSocket
    功能：关闭TCP连接
    注意：请在结束图像还原的三步操作之后调用此方法关闭连接，否则我会很生气
    */
    public void CloseSocket(){
        try{
            Log.e(TAG, "CloseSocket: start" );
            socket.close();
            in.close();
            out.close();
        }catch (IOException e){
            Log.i(TAG,Log.getStackTraceString(e));
        }
    }

    //发送bitmap图片的方法,不是给你用的
    private void sendImgMsg(DataOutputStream out,final Bitmap RawImg) throws IOException {
        //将bitmap转为字节数组
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        RawImg.compress(Bitmap.CompressFormat.PNG,100,bout);
        //写入字节的长度，再写入图片的字节
        int len = bout.size();
        //这里打印一下发送的长度
        Log.i(TAG, "len: "+len);
        //发送图片
        out.writeInt(len);
        out.write(bout.toByteArray());
    }
    //接受bitmap图片的方法，不是给你用的
    private Bitmap getImgMsg(DataInputStream in) throws  IOException{
        int len = in.readInt();
        byte[] bytes = new byte[len];
        Log.d(TAG, "len:"+len);
        in.readFully(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }
}
