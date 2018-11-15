package com.example.callback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * 摄像头调用
 *
 * @author Administrator
 */
public class CameraCallback implements SurfaceHolder.Callback, PreviewCallback {
    private static final String TAG = "CameraCallback";
    private static final boolean D = true;
    @SuppressWarnings("unused")
    private Context mContext;
    private Handler mHandler;
    private Camera mCamera = null;
    /**
     * 摄像头预览开启标志
     */
    public boolean isPreview = false;

    public static boolean isrelease = false;

    private boolean isCameraInit = false;

    private byte[] tmp;

    /**
     * 自动对焦回调
     */
    private AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success)// success表示对焦成功
            {
                if (D)
                    Log.d(TAG, "自动对焦成功");
                // myCamera.setOneShotPreviewCallback(null);
            } else {
                // 未对焦成功
                if (D)
                    Log.d(TAG, "自动对焦失败了");
            }
        }
    };

    /**
     * 摄像头调用构造函数
     *
     * @param context
     * @param mHandler
     */
    public CameraCallback(Context context, Handler mHandler) {
        this.mContext = context;
        this.mHandler = mHandler;
    }

    /**
     * 摄像头调用构造函数
     */
    public CameraCallback() {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (D)
            Log.d(TAG, " surfaceChanged");
        initCamera();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (D)
            Log.d(TAG, " surfaceCreated");
        try {
            if (mCamera == null) {
                mCamera = Camera.open();
                mCamera.lock();
            }
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "打开摄像头出错");
            e.printStackTrace();
        }

        tmp = new byte[1024 * 10];

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (D)
            Log.d(TAG, " surfaceDestroyed");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // try {
                    // Thread.sleep(3000);
                    // } catch (InterruptedException e) {
                    // e.printStackTrace();
                    // }
                    // if (MediaH263Record.isrecorddistory) {
                    if (mCamera != null) {
                        mCamera.setPreviewCallback(null);
                        mCamera.setOneShotPreviewCallback(null);
                        mCamera.stopPreview();
                        isPreview = false;
                        CameraCallback.isrelease = true;
                        mCamera.release();
                        mCamera = null;
                        isCameraInit = false;
                        Log.v(TAG, "释放摄像头成功");
                    }
                    return;
                    // }
                }
            }
        }).start();
    }

    /**
     * 初始化摄像头
     */
    public void initCamera() {
        if (isPreview) {
            mCamera.stopPreview();
        }
        if (null != mCamera) {
            Parameters parameters = mCamera.getParameters();
            // parameters.setPictureFormat(PixelFormat.JPEG);// 设置拍照后存储的图片格式
            // 设置大小和方向等参数
            Size size = parameters.getPreviewSize();
            if (D)
                Log.d(TAG, "size.width=" + size.width + "size.height=" + size.height); // setPreviewSize 宽高不能大于，否者会出现错误
            parameters.setPreviewSize(640, 480);
            parameters.setPictureSize(640, 480);
            // parameters.setPreviewSize(240, 320);
            // parameters.setPictureSize(240, 320);
            // parameters.setRotation(90);
            // parameters.setRotation(180);
            // parameters.setRotation(270);
            // parameters.set("orientation", "portrait");
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.setRecordingHint(true);
            mCamera.setParameters(parameters);
            // mCamera.setDisplayOrientation(90);// 旋转摄像头
            // mCamera.setDisplayOrientation(180);
            mCamera.startPreview();
            // mCamera.setPreviewCallback(this);
            mCamera.autoFocus(myAutoFocusCallback);
            isPreview = true;
            CameraCallback.isrelease = false;
            isCameraInit = true;
        }
    }

    // if (orientation == ORIENTATION_UNKNOWN) return;
    // android.hardware.Camera.CameraInfo info =
    // new android.hardware.Camera.CameraInfo();
    // android.hardware.Camera.getCameraInfo(cameraId, info);
    // orientation = (orientation + 45) / 90 * 90;
    // int rotation = 0;
    // if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
    // rotation = (info.orientation - orientation + 360) % 360;
    // } else { // back-facing camera
    // rotation = (info.orientation + orientation) % 360;
    // }
    // mParameters.setRotation(rotation);

    /**
     * 获取摄像头实例
     *
     * @return Camera
     */
    public Camera getCamera() {
        return mCamera;
    }

    /**
     * 快门按下的回调，设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
     */
    ShutterCallback myShutterCallback = new ShutterCallback() {

        public void onShutter() {
            if (D)
                Log.d(TAG, "快门按下的回调");

        }
    };

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
    }

/*
    // 拍照存储照片
    // public void takePicture(final Handler handler) {
    // mCamera.takePicture(myShutterCallback, null, new PictureCallback() {
    // @Override
    // public void onPictureTaken(byte[] data, Camera camera) {
    // if (null != data) {
    // mCamera.stopPreview();
    // isPreview = false;
    // }
    // FileOutputStream fos = null;
    // try {
    // File directory;
    // if (Environment.getExternalStorageState().equals(
    // Environment.MEDIA_MOUNTED)) {
    // directory = new File(Environment
    // .getExternalStorageDirectory(), "Student");
    // } else {
    // directory = new File(mContext.getCacheDir(), "Student");
    // }
    // if (!directory.exists()) {
    // directory.mkdir();
    // }
    // // 保存的地址和格式
    // File file = new File(directory, filename + ".jpg");
    // filename += 1;
    // // File file = new File(directory,
    // // System.currentTimeMillis()
    // // + ".jpg");
    //
    // // 数组生成bitmap图片
    // mBitmap = BitmapFactory.decodeByteArray(data, 0,
    // data.length);
    // // 旋转图像
    // // Matrix matrix = new Matrix();
    // // matrix.postRotate((float) 90.0);
    // // Bitmap bitmap = Bitmap.createBitmap(mBitmap, 0, 0,
    // // mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
    //
    // // MainActivity.mImageView1.setImageBitmap(mBitmap);
    //
    // fos = new FileOutputStream(file);
    // // 压缩输出
    // boolean compress = mBitmap.compress(CompressFormat.JPEG,
    // 100, fos);
    // if (compress) {
    // handler.sendEmptyMessage(MainActivity.PHOTO_SVAE_SUCCESS);
    // } else {
    // handler.sendEmptyMessage(MainActivity.PHOTO_SVAE_FAILURE);
    // }
    // // 再次进入预览
    // mCamera.startPreview();
    // isPreview = true;
    //
    // if (D)
    // Log.d(TAG, " 保存是否成功:" + compress + "  file.exists:"
    // + file.exists());
    // } catch (FileNotFoundException e) {
    // e.printStackTrace();
    // } finally {
    // if (fos != null) {
    // try {
    // fos.close();
    // } catch (IOException e) {
    // }
    // }
    // }
    // }
    // });
    // }

    // 拍照
    // public void takePicture(final Handler mHandler) {
    // // final ByteArrayOutputStream os = new ByteArrayOutputStream();
    // mCamera.takePicture(myShutterCallback, null, new PictureCallback() {
    // @Override
    // public void onPictureTaken(byte[] data, Camera camera) {
    // if ((null != data) && isPreview) {
    // mCamera.stopPreview();
    // isPreview = false;
    // }
    // // 数组生成bitmap图片
    // mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
    // mHandler.obtainMessage(MainActivity.UART_RECEIVER_SUCCESS, -1,
    // -1, mBitmap).sendToTarget();
    // // 再次进入预览
    // if (!isPreview) {
    // camera.startPreview();
    // isPreview = true;
    // }
    //
    // }
    //
    // });
    // }

    *//**
     * 拍照
     *//*
    public void takePicture() {
        if (isCameraInit) {
            if (mCamera != null) {
                if (D)
                    Log.e(TAG, "开始照相");
                mCamera.setOneShotPreviewCallback(this); // 对于实现的PreviewCallback接口，这一句用于调用onPreviewFrame方法得到预览帧数据
                mHandler.sendEmptyMessage(MainActivity.SHUTTER_ANIMATION);
            }
        } else {
            if (D)
                Log.e(TAG, "照相失败");
            mHandler.sendEmptyMessage(MainActivity.PHOTO_SVAE_FAILURE);
        }

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Size size = camera.getParameters().getPreviewSize(); // 获取预览大小
        final int w = size.width; // 宽度
        final int h = size.height;
        // Log.e(TAG, "W" + w);
        // Log.e(TAG, "H" + h);
        // Log.e(TAG, "" + data.length);
        // data = rotate180(data, w, h);// 旋转数据流
        try {
            YuvImage image = new YuvImage(data, ImageFormat.NV21, w, h, null);
            ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
            if (!image.compressToJpeg(new Rect(0, 0, w, h), 50, os)) {// 压缩质量中等,出图大概是3~5k
                return;
            }
            tmp = os.toByteArray();

            // Log.e(TAG, "" + tmp.length);
            mHandler.obtainMessage(MainActivity.PHOTO_SAVE_SUCCESS, -1, -1, tmp).sendToTarget();
            os.close();
            if (D)
                Log.e(TAG, "结束照相");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Size size = camera.getParameters().getPreviewSize(); // 获取预览大小
        // final int w = size.width; // 宽度
        // final int h = size.height;
        // final YuvImage image = new YuvImage(data, ImageFormat.NV21, w, h,
        // null);
        // ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
        // if (!image.compressToJpeg(new Rect(0, 0, w, h), 100, os)) {
        // return;
        // }
        // byte[] tmp = os.toByteArray();
        // Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
        //
        // Matrix matrix = new Matrix(); // 完成将图片翻转180度
        // matrix.reset();
        // matrix.postRotate(180);
        // Bitmap bMapRotate;
        // bMapRotate = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
        // bmp.getHeight(), matrix, true);
        // bmp = bMapRotate;
        // mHandler.obtainMessage(MainActivity.PHOTO_SVAE_SUCCESS, -1, -1, bmp)
        // .sendToTarget();

        // MainActivity.mImageView1.setImageBitmap(bmp);
        // MainActivity.mImageText1.setText(String.valueOf(a++));
        // if (null != mTPTask) {
        // switch (mTPTask.getStatus()) {
        // case RUNNING:
        // return;
        // case PENDING:
        // mTPTask.cancel(false);
        // break;
        // default:
        // break;
        // }
        // }
        // mTPTask = new TakePictureTask(data);
        // mTPTask.execute((Void) null);
    }

    // private class TakePictureTask extends AsyncTask<Void, Void, Void> {
    //
    // private byte[] mData;
    //
    // // 构造函数
    // TakePictureTask(byte[] data) {
    // this.mData = data;
    // }
    //
    // @Override
    // protected Void doInBackground(Void... params) {
    // Size size = mCamera.getParameters().getPreviewSize(); // 获取预览大小
    // final int w = size.width; // 宽度
    // final int h = size.height;
    // System.out.println("" + size.width + "," + size.height);
    // final YuvImage image = new YuvImage(mData, ImageFormat.NV21, w, h,
    // null);
    // ByteArrayOutputStream os = new ByteArrayOutputStream(mData.length);
    // if (!image.compressToJpeg(new Rect(0, 0, w, h), 100, os)) {
    // return null;
    // }
    // byte[] tmp = os.toByteArray();
    // Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
    // if (D)
    // Log.d(TAG, "到达了转换图片的地方");
    //
    // Matrix matrix = new Matrix(); // 完成将图片翻转180度
    // matrix.reset();
    // matrix.postRotate(180);
    // Bitmap bMapRotate;
    // bMapRotate = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
    // bmp.getHeight(), matrix, true);
    // bmp = bMapRotate;
    // takePicturecallback(bmp);
    // return null;
    // }
    // }
    //
    // public void takePicturecallback(Bitmap bmp) {
    // MainActivity.mImageView1.setImageBitmap(bmp);
    // MainActivity.mImageText1.setText(String.valueOf(a++));
    // }

    *//**
     * data[]数组的180°旋转<br>
     * I420: YYYYYYYY UU VV =>YUV420P<br>
     * YV12: YYYYYYYY VV UU =>YUV420P<br>
     * NV12: YYYYYYYY UVUV =>YUV420SP<br>
     * NV21: YYYYYYYY VUVU =>YUV420SP<br>
     *
     * @param data
     * @param w
     * @param h
     * @return byte[]
     *//*
    @SuppressWarnings("unused")
    private synchronized byte[] rotate180(byte[] data, int w, int h) {
        byte[] temp = new byte[w * h * 3 / 2];
        int n = 0, s = 0, k = 0;
        // YYYYYYYYYYYYYYYYYYYYYYYYY
        n = w * h;
        k = w * h - 1;
        s = 0;
        for (int i = s; i < n; i++) {
            temp[k] = data[s];
            k--;
            s++;
        }

        // // UUUUUUUUUUUUUUUUUUUUUUUUU
        // n = w * h * 5 / 4;
        // k = w * h * 5 / 4 - 1;
        // s = w * h;
        // for (int i = s; i < n; i++) {
        // temp[k] = data[s];
        // k--;
        // s++;
        // }
        // // VVVVVVVVVVVVVVVVVVVVVVVVVV
        // n = w * h * 3 / 2;
        // k = w * h * 3 / 2 - 1;
        // s = w * h * 5 / 4;
        // for (int i = s; i < n; i++) {
        // temp[k] = data[s];
        // k--;
        // s++;
        // }

        // VUVUVUVUVUVUVUVUVUVUVUVUVUVU
        // VUVUVUVUVUVUVUVUVUVUVUVUVUVU
        n = w * h * 3 / 2;
        k = w * h * 3 / 2 - 1;
        s = w * h;
        for (int i = s; i < n; i += 2) {
            temp[k] = data[s + 1];
            temp[k - 1] = data[s];
            k -= 2;
            s += 2;
        }
        return temp;
    }

    *//**
     * data[]数组的90°旋转<br>
     * 旋转完成后要将 w跟h对调<br>
     * I420: YYYYYYYY UU VV =>YUV420P<br>
     * YV12: YYYYYYYY VV UU =>YUV420P<br>
     * NV12: YYYYYYYY UVUV =>YUV420SP<br>
     * NV21: YYYYYYYY VUVU =>YUV420SP<br>
     *
     * @param data
     * @param w
     * @param h
     * @return byte[]
     *//*
    @SuppressWarnings("unused")
    private synchronized byte[] rotate90(byte[] data, int w, int h) {
        byte[] temp = new byte[w * h * 3 / 2];
        int k = 0;
        // YYYYYYYYYYYYYYYYYYYYYYYYY
        int wh = w * h;
        k = 0;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                temp[k] = data[w * j + i];
                k++;
            }
        }

        // // UUUUUUUUUUUUUUUUUUUUUUUUU
        // for (int i = 0; i < w; i++) {
        // for (int j = 0; j < h / 4; j++) {
        // temp[k] = data[wh + w * j + i];
        // k++;
        // }
        // }
        // // VVVVVVVVVVVVVVVVVVVVVVVVV
        // wh = wh * 5 / 4;
        // for (int i = 0; i < w; i++) {
        // for (int j = 0; j < h / 4; j++) {
        // temp[k] = data[wh + w * j + i];
        // k++;
        // }
        // }

        // VUVUVUVUVUVUVUVUVUVUVUVUVUVU
        // VUVUVUVUVUVUVUVUVUVUVUVUVUVU
        for (int i = 0; i < w; i += 2) {
            for (int j = 0; j < h / 2; j++) {
                temp[k] = data[wh + w * j + i];
                temp[k + 1] = data[wh + w * j + i + 1];
                k += 2;
            }
        }
        return temp;
    }*/
}