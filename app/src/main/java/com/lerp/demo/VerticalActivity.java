package com.lerp.demo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SDCardUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lerp.pano.ImagesStitch;
import com.lxj.xpopup.XPopup;
import com.shizhefei.view.largeimage.LargeImageView;
import com.shizhefei.view.largeimage.factory.FileBitmapDecoderFactory;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class VerticalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.large_view);
        TextView tvMessage = findViewById(R.id.tv_message);
        tvMessage.setText("将竖直拍摄的几张照片拼接成一张");
        requestPermission();
    }

    public void requestPermission() {
        PermissionUtils.permission(PermissionConstants.STORAGE)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        init();
                    }

                    @Override
                    public void onDenied() {
                        new XPopup.Builder(VerticalActivity.this)
                                .asConfirm(
                                        "提示",
                                        "请授予存储文件权限",
                                        () -> init(),
                                        () -> requestPermission()
                                )
                                .show();
                    }
                })
                .request();
    }

    private void init() {
        final String result = ActivityMain.DIR + File.separator + "vertical.jpg";

        final LargeImageView largeImageView = findViewById(R.id.activity_layout);
        final View progressBar = findViewById(R.id.progress_bar);
        ThreadUtils.executeByIo(new ThreadUtils.Task<int[]>() {
            @Override
            public int[] doInBackground() throws Exception {
                Bitmap[] bitmaps = new Bitmap[4];
                bitmaps[0] = BitmapUtils.getBitmap(VerticalActivity.this, "medium10.jpg");
                bitmaps[1] = BitmapUtils.getBitmap(VerticalActivity.this, "medium11.jpg");
                bitmaps[2] = BitmapUtils.getBitmap(VerticalActivity.this, "medium12.jpg");
                bitmaps[3] = BitmapUtils.getBitmap(VerticalActivity.this, "medium19.jpg");

                boolean resultDir = FileUtils.createOrExistsDir(ActivityMain.DIR);
                if (!resultDir) {
                    throw new Exception("创建文件夹失败\n" + ActivityMain.DIR);
                }
                boolean resultFile = FileUtils.createOrExistsFile(result);
                if (!resultFile) {
                    throw new Exception("创建文件失败\n" + result);
                }
                return ImagesStitch.stitchImagesFromBitmaps(bitmaps, result,
                        ImagesStitch.TYPE_SPHERICAL, ImagesStitch.CORRECTION_VERT,
                        0.2f, 0.03f, 100, 1f);
            }

            @Override
            public void onSuccess(int[] ints) {
                if (ints[0] == 0 && !VerticalActivity.this.isDestroyed()) {
                    ThreadUtils.runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        largeImageView.setImage(new FileBitmapDecoderFactory(result));
                    });
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable throwable) {
                ToastUtils.showShort(throwable.getMessage());
            }
        });
    }
}
