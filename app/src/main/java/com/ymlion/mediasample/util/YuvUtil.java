package com.ymlion.mediasample.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.io.ByteArrayOutputStream;

/**
 * Created by YMlion on 2017/9/18.
 */

public class YuvUtil {

    private static final String TAG = "YuvUtil";

    public static native void yuvToRgb(byte[] yuvData, int width, int height, int dstWidth,
            int dstHeight, int orientation, int format, int scaleMode, Surface surface,
            boolean front);

    public static native void convertToARGB(byte[] yuvData, int width, int height, int dstWidth,
            int dstHeight, int orientation, int format, int scaleMode, Surface surface,
            boolean front);

    public static boolean drawByBitmap(Camera camera, int orientation, int displayWidth,
            int displayHeight, boolean isFront, SurfaceHolder surface, byte[] data) {
        long s = System.currentTimeMillis();
        Camera.Size size = camera.getParameters().getPreviewSize();
        //这里一定要得到系统兼容的大小，否则解析出来的是一片绿色或者其他
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, outputStream);
        long s1 = System.currentTimeMillis() - s;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;//必须设置为565，否则无法检测
        byte[] bytes = outputStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        long s2 = System.currentTimeMillis() - s;
        Matrix matrix = new Matrix();
        if (isFront && orientation == 90) {
            matrix.setScale(-1, 1);
            //matrix.postRotate(270);
        }
        matrix.postRotate(orientation);

        float sx = displayWidth / 1.0f / bitmap.getWidth();
        float sy = displayHeight / 1.0f / bitmap.getHeight();
        if (orientation == 90 || orientation == 270) {
            matrix.postScale(sy, sx);
        } else {
            matrix.postScale(sx, sy);
        }
        Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                true);
        long s3 = System.currentTimeMillis() - s;
        Canvas canvas = surface.lockCanvas(null);
        canvas.drawBitmap(bm, 0, 0, null);
        surface.unlockCanvasAndPost(canvas);
        Log.d(TAG, "handleMessage: "
                + s1
                + " ; "
                + s2
                + " ; "
                + s3
                + " ; "
                + (System.currentTimeMillis() - s));
        return true;
    }

    public static boolean drawByArgb(Camera camera, int orientation, int displayWidth,
            int displayHeight, Surface surface, byte[] data, boolean front) {
        long s = System.currentTimeMillis();
        Camera.Size size = camera.getParameters().getPreviewSize();
        YuvUtil.convertToARGB(data, size.width, size.height, displayWidth, displayHeight,
                orientation, 1, 0, surface, front);
        // 两者速度几乎无差别，但第二个内存无抖动
        //textureBmp.copyPixelsFromBuffer(ByteBuffer.wrap(dst));
        //YuvUtil.fillBitmap(textureBmp, dst, displaySize);
        //Canvas canvas = mSurface.lockCanvas(null);
        //canvas.drawBitmap(textureBmp, 0, 0, null);
        //mSurface.unlockCanvasAndPost(canvas);
        Log.d(TAG, "drawByArgb: " + orientation + " ; total = " + (System.currentTimeMillis() - s));
        return true;
    }

    public static boolean drawByYuv(Camera camera, int orientation, int displayWidth,
            int displayHeight, Surface surface, byte[] data, boolean front) {
        long s = System.currentTimeMillis();
        Camera.Size size = camera.getParameters().getPreviewSize();
        YuvUtil.yuvToRgb(data, size.width, size.height, displayWidth, displayHeight, orientation, 1,
                0, surface, front);
        Log.d(TAG, "drawByYuv: " + orientation + " ; total = " + (System.currentTimeMillis() - s));
        return true;
    }
}
