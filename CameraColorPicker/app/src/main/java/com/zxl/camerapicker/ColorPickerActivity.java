package com.zxl.camerapicker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zxl.camerapicker.utils.Cameras;
import com.zxl.camerapicker.utils.ThreadUtils;
import com.zxl.camerapicker.views.CameraColorPickerview;

public class ColorPickerActivity extends AppCompatActivity implements CameraColorPickerview.OnColorSelectedListener {

    private CameraColorPickerview cameraColorPickerview;
    private FrameLayout flPickerContainer;
    private TextView tvPickerColor;
    private View vBlockColor, vPointerRing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        initView();
        initData();
        initCamera();
    }

    private void initView() {
        flPickerContainer = findViewById(R.id.fl_picker_container);
        tvPickerColor = findViewById(R.id.tv_picker_color);
        vBlockColor = findViewById(R.id.v_block_color);
        vPointerRing = findViewById(R.id.v_pointer_ring);
    }

    private void initData() {
        findViewById(R.id.btn_picker_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("保存");
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(ColorPickerActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private Camera camera;

    private void initCamera() {
        ThreadUtils.asynSingleExecuteTask(new Runnable() {
            @Override
            public void run() {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                if (camera == null) {
                    Log.e("zxl", "initCamera is error,camera is null: ");
                }
                Log.w("zxl", "initCamera 打开摄像头: ");
                Camera.Parameters cameraParameters = camera.getParameters();
                Camera.Size bestSize = Cameras.getBestPreviewSize(
                        cameraParameters.getSupportedPreviewSizes(),
                        flPickerContainer.getWidth(),
                        flPickerContainer.getHeight(),
                        true
                );
                cameraParameters.setPreviewSize(bestSize.width, bestSize.height);
                camera.setParameters(cameraParameters);
                //set camera orientation to match with current device orientation
                //将相机方向设置为与当前设备方向匹配
                Cameras.setCameraDisplayOrientation(ColorPickerActivity.this, camera);

                final FrameLayout.LayoutParams mPreviewParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                mPreviewParams.gravity = Gravity.CENTER;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cameraColorPickerview = new CameraColorPickerview(ColorPickerActivity.this, camera);
                        cameraColorPickerview.setOnColorSelectedListener(ColorPickerActivity.this);
                        flPickerContainer.addView(cameraColorPickerview, 0, mPreviewParams);
                    }
                });
            }
        });
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onColorSelected(int[] color) {
        if (color != null && color.length >= 3) {
            String hex = toHex(color[0], color[1], color[2]);
            int rgb = Color.rgb(color[0], color[1], color[2]);
            tvPickerColor.setText(String.format("Hex: %s\nRGB: %d,%d,%d", hex, color[0], color[1], color[2]));
            vPointerRing.getBackground().setColorFilter(rgb, PorterDuff.Mode.SRC_ATOP);
            vBlockColor.getBackground().setColorFilter(rgb, PorterDuff.Mode.SRC_ATOP);
        }
    }

    private String toHex(int r, int g, int b) {
        String hr = Integer.toHexString(r);
        String hg = Integer.toHexString(g);
        String hb = Integer.toHexString(b);
        return String.format("#%s%s%s", hr, hg, hb);
    }

    private boolean mIsFlashOn;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isFlashSupported()) {
            getMenuInflater().inflate(R.menu.menu_flash, menu);
            final MenuItem flashItem = menu.findItem(R.id.menu_color_picker_action_flash);
            int flashIcon = mIsFlashOn ? R.mipmap.ic_action_flash_off : R.mipmap.ic_action_flash_on;
            flashItem.setIcon(flashIcon);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_color_picker_action_flash:
                toggleFlash();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleFlash() {
        if (camera == null) return;
        final Camera.Parameters parameters = camera.getParameters();
        final String flashParameter = mIsFlashOn ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters.FLASH_MODE_TORCH;
        parameters.setFlashMode(flashParameter);

        // Set the preview callback to null and stop the preview
        camera.setPreviewCallback(null);
        camera.stopPreview();

        // Change the parameters
        camera.setParameters(parameters);

        // Restore the preview callback and re-start the preview
        camera.setPreviewCallback(cameraColorPickerview);
        camera.startPreview();

        mIsFlashOn = !mIsFlashOn;
        invalidateOptionsMenu();
    }

    private boolean isFlashSupported() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    private void release() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
        if (flPickerContainer != null) {
            flPickerContainer.removeView(cameraColorPickerview);
        }
    }
}
