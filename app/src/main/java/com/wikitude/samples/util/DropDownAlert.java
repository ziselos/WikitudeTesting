package com.wikitude.samples.util;

import com.google.android.flexbox.FlexboxLayout;

import com.wikitude.nativesdksampleapp.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public final class DropDownAlert {

    public static final String TAG = "DropDownAlert";

    private final Activity mActivity;
    private View mAlertView;
    private final FlexboxLayout mAlertImagesLayout;
    private final TextView mAlertText;
    private int mMarginTop;
    private boolean shown;

    public DropDownAlert(Activity activity) {
        mActivity = activity;

        LayoutInflater factory = LayoutInflater.from(mActivity);
        mAlertView = factory.inflate(R.layout.drop_down_alert, null);
        mAlertImagesLayout = (FlexboxLayout) mAlertView.findViewById(R.id.dropdown_alert_images);
        mAlertText = (TextView) mAlertView.findViewById(R.id.dropdown_alert_text);
    }

    public void show() {
        this.show(null);
    }

    public void show(ViewGroup parentView) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        mAlertView.post(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mAlertView.getLayoutParams();
                layoutParams.setMargins(0, mMarginTop, 0, 0);
                mAlertView.setLayoutParams(layoutParams);

                TranslateAnimation down = new TranslateAnimation(0, 0, -mAlertView.getHeight(), 0);
                down.setDuration(500);
                down.setInterpolator(new LinearInterpolator());

                mAlertView.startAnimation(down);
                shown = true;
            }
        });
        if (parentView == null) {
            if (mAlertView.getParent() == null) {
                mActivity.addContentView(mAlertView, lp);
            }
        } else {
            parentView.addView(mAlertView, lp);
        }
    }

    public void dismiss() {
        if (mAlertView != null && shown) {
            shown = false;
            mAlertView.post(new Runnable() {
                @Override
                public void run() {
                    TranslateAnimation up = new TranslateAnimation(0, 0, 0, -mAlertView.getHeight());
                    up.setDuration(500);
                    up.setInterpolator(new LinearInterpolator());
                    mAlertView.startAnimation(up);

                    if (mAlertView.getParent() != null) {
                        ((ViewManager) mAlertView.getParent()).removeView(mAlertView);
                        mAlertView = null;
                    }
                }
            });
        }
    }

    public void setText(String text) {
        mAlertText.setText(text);
    }

    public void addImages(String... paths) {
        for (String path : paths) {
            addImage(path);
        }
    }

    public void removeAllImages(){
        mAlertImagesLayout.removeAllViews();
    }

    public void setTextWeight(float textToImageWidthRatio) {
        LinearLayout.LayoutParams textLayoutparams = (LinearLayout.LayoutParams) mAlertText.getLayoutParams();
        textLayoutparams.weight = textToImageWidthRatio;
        LinearLayout.LayoutParams imagesLayoutparams = (LinearLayout.LayoutParams) mAlertImagesLayout.getLayoutParams();
        imagesLayoutparams.weight = 1 - textToImageWidthRatio;
    }

    public void setMarginTop(int marginTop) {
        mMarginTop = marginTop;
    }

    private void addImage(String path) {
        FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(40));
        layoutParams.setMargins(0, 0, dpToPx(2), dpToPx(2));
        ImageView imageView = new ImageView(mActivity);
        imageView.setLayoutParams(layoutParams);
        imageView.setImageBitmap(getBitmapFromAsset(path));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        imageView.setAlpha(0.7f);
        mAlertImagesLayout.addView(imageView);
    }

    private Bitmap getBitmapFromAsset(String filePath) {
        AssetManager assetManager = mActivity.getAssets();

        InputStream inputStream;
        Bitmap bitmap = null;
        try {
            inputStream = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "getBitmapFromAsset: ", e);
        }

        return bitmap;
    }

    private int dpToPx(int dp) {
        final float scale = mActivity.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
