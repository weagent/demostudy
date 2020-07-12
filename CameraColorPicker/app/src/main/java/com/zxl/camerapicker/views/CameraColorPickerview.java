package com.zxl.camerapicker.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;

import java.io.IOException;

/**
 * Created by zxl on 2020/7/11.
 */
public class CameraColorPickerview extends TextureView implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {

    private Camera mCamera;
    private Camera.Size mPreviewSize;
    private int[] mSelectedColor;
    /**
     * The size of the pointer (in PIXELS).
     */
    protected static final int POINTER_RADIUS = 5;

    public CameraColorPickerview(Context context, Camera camera) {
        super(context);
        this.mCamera = camera;
        mCamera.getParameters().getPreviewFormat();
        this.setSurfaceTextureListener(this);
        mPreviewSize = mCamera.getParameters().getPreviewSize();
        mSelectedColor = new int[3];
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mColorSelectedListener == null) {
            throw new RuntimeException("OnColorSelectedListener is null !");
        }
        final int midX = mPreviewSize.width / 2;
        final int midY = mPreviewSize.height / 2;
        //reset the selected color
        mSelectedColor[0] = 0;
        mSelectedColor[1] = 0;
        mSelectedColor[2] = 0;
        for (int i = 0; i < POINTER_RADIUS; i++) {
            for (int j = 0; j < POINTER_RADIUS; j++) {
                addColorFromYUV420(data, mSelectedColor, (i * POINTER_RADIUS + j + 1),
                        (midX - POINTER_RADIUS) + i, (midY - POINTER_RADIUS) + j,
                        mPreviewSize.width, mPreviewSize.height);
            }
        }

        mColorSelectedListener.onColorSelected(mSelectedColor);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
            Log.w("zxl", "onSurfaceTextureAvailable 开启预览: ");
        } catch (IOException e) {
            Log.e("zxl", "onSurfaceTextureAvailable is error : " + e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void addColorFromYUV420(byte[] data, int[] averageColor, int count, int x, int y, int width, int height) {
        // The code converting YUV420 to rgb format is highly inspired from this post http://stackoverflow.com/a/10125048
        final int size = width * height;
        final int Y = data[y * width + x] & 0xff;
        final int xby2 = x / 2;
        final int yby2 = y / 2;

        final float V = (float) (data[size + 2 * xby2 + yby2 * width] & 0xff) - 128.0f;
        final float U = (float) (data[size + 2 * xby2 + 1 + yby2 * width] & 0xff) - 128.0f;

        // Do the YUV -> RGB conversion
        float Yf = 1.164f * ((float) Y) - 16.0f;
        int red = (int) (Yf + 1.596f * V);
        int green = (int) (Yf - 0.813f * V - 0.391f * U);
        int blue = (int) (Yf + 2.018f * U);

        // Clip rgb values to [0-255]
        red = red < 0 ? 0 : red > 255 ? 255 : red;
        green = green < 0 ? 0 : green > 255 ? 255 : green;
        blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;

        averageColor[0] += (red - averageColor[0]) / count;
        averageColor[1] += (green - averageColor[1]) / count;
        averageColor[2] += (blue - averageColor[2]) / count;
    }

    private OnColorSelectedListener mColorSelectedListener;

    public void setOnColorSelectedListener(OnColorSelectedListener selectedListener) {
        this.mColorSelectedListener = selectedListener;
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int[] color);
    }
}
