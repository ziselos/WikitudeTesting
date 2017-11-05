package com.wikitude.samples;

import com.wikitude.WikitudeSDK;
import com.wikitude.common.permission.PermissionManager;
import com.wikitude.common.util.SDKBuildInformation;
import com.wikitude.nativesdksampleapp.R;
import com.wikitude.samples.camera.CameraSettingsActivity;
import com.wikitude.samples.plugins.BarcodePluginActivity;
import com.wikitude.samples.plugins.CustomCameraActivity;
import com.wikitude.samples.plugins.FaceDetectionPluginActivity;
import com.wikitude.samples.plugins.SimpleInputPluginActivity;
import com.wikitude.samples.rendering.external.ExternalRenderingActivity;
import com.wikitude.samples.rendering.internal.InternalRenderingActivity;
import com.wikitude.samples.tracking.cloud.ContinuousCloudRecognitionActivity;
import com.wikitude.samples.tracking.cloud.SingleCloudRecognitionActivity;
import com.wikitude.samples.tracking.image.ExtendedImageTrackingActivity;
import com.wikitude.samples.tracking.image.MultipleTargetsImageTrackingActivity;
import com.wikitude.samples.tracking.image.SimpleImageTrackingActivity;
import com.wikitude.samples.tracking.instant.InstantScenePickingActivity;
import com.wikitude.samples.tracking.instant.InstantTrackingActivity;
import com.wikitude.samples.tracking.object.ObjectTrackingActivity;
import com.wikitude.samples.util.SampleCategory;
import com.wikitude.samples.util.adapter.SamplesExpendableListAdapter;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ExpandableListView.OnChildClickListener {

    private static final int EXPANDABLE_INDICATOR_START_OFFSET = 60;
    private static final int EXPANDABLE_INDICATOR_END_OFFSET = 30;

    private ExpandableListView listView;

    private final List<SampleCategory> sampleCategories = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sampleCategories.add(new SampleCategory(getString(R.string.image_tracking), getResources().getStringArray(R.array.imageTracking_samples)));
        sampleCategories.add(new SampleCategory(getString(R.string.cloud_recognition), getResources().getStringArray(R.array.cloudRecognition_samples)));
        sampleCategories.add(new SampleCategory(getString(R.string.instant_tracking), getResources().getStringArray(R.array.instantTracking_samples)));
        sampleCategories.add(new SampleCategory(getString(R.string.object_tracking), getResources().getStringArray(R.array.objectTracking_samples)));
        sampleCategories.add(new SampleCategory(getString(R.string.rendering), getResources().getStringArray(R.array.rendering_samples)));
        sampleCategories.add(new SampleCategory(getString(R.string.plugins), getResources().getStringArray(R.array.plugins_samples)));
        sampleCategories.add(new SampleCategory(getString(R.string.camera_control), getResources().getStringArray(R.array.cameraControl_samples)));

        SamplesExpendableListAdapter adapter = new SamplesExpendableListAdapter(this, sampleCategories);

        listView = findViewById(R.id.listView);
        moveExpandableIndicatorToRight();
        listView.setOnChildClickListener(this);
        listView.setAdapter(adapter);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showSdkBuildInformation();
                return false;
            }
        });
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, final int groupPosition, final int childPosition, long id) {

        WikitudeSDK.getPermissionManager().checkPermissions(this, new String[]{Manifest.permission.CAMERA}, PermissionManager.WIKITUDE_PERMISSION_REQUEST, new PermissionManager.PermissionManagerCallback() {
            @Override
            public void permissionsGranted(int requestCode) {
                Class activity;
                String itemValue = sampleCategories.get(groupPosition).getSamples()[childPosition];
                switch (itemValue) {
                    case "Simple":
                        activity = SimpleImageTrackingActivity.class;
                        break;
                    case "Multiple Targets":
                        activity = MultipleTargetsImageTrackingActivity.class;
                        break;
                    case "Extended Tracking":
                        activity = ExtendedImageTrackingActivity.class;
                        break;
                    case "Single Recognition":
                        activity = SingleCloudRecognitionActivity.class;
                        break;
                    case "Continuous Recognition":
                        activity = ContinuousCloudRecognitionActivity.class;
                        break;
                    case "Instant Tracking":
                        activity = InstantTrackingActivity.class;
                        break;
                    case "Instant Scene Picking":
                        activity = InstantScenePickingActivity.class;
                        break;
                    case "Object Tracking":
                        activity = ObjectTrackingActivity.class;
                        break;
                    case "External Rendering":
                        activity = ExternalRenderingActivity.class;
                        break;
                    case "Internal Rendering":
                        activity = InternalRenderingActivity.class;
                        break;
                    case "QR & Barcode":
                        activity = BarcodePluginActivity.class;
                        break;
                    case "Face Detection":
                        activity = FaceDetectionPluginActivity.class;
                        break;
                    case "Simple Input Plugin":
                        activity = SimpleInputPluginActivity.class;
                        break;
                    case "Custom Camera":
                        activity = CustomCameraActivity.class;
                        break;
                    case "Camera Settings":
                        activity = CameraSettingsActivity.class;
                        break;
                    default:
                        activity = SimpleImageTrackingActivity.class;
                }

                final Intent intent = new Intent(MainActivity.this, activity);
                startActivity(intent);
            }

            @Override
            public void permissionsDenied(String[] deniedPermissions) {
                Toast.makeText(MainActivity.this, "The Wikitude SDK needs the following permissions to enable an AR experience: " + Arrays.toString(deniedPermissions), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void showPermissionRationale(final int requestCode, final String[] permissions) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Wikitude Permissions");
                alertBuilder.setMessage("The Wikitude SDK needs the following permissions to enable an AR experience: " + Arrays.toString(permissions));
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WikitudeSDK.getPermissionManager().positiveRationaleResult(requestCode, permissions);
                    }
                });

                AlertDialog alert = alertBuilder.create();
                alert.show();
            }
        });
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        WikitudeSDK.getPermissionManager().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void moveExpandableIndicatorToRight() {
        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        listView.setIndicatorBoundsRelative(width - dpToPx(EXPANDABLE_INDICATOR_START_OFFSET), width - dpToPx(EXPANDABLE_INDICATOR_END_OFFSET));
        listView.setIndicatorBoundsRelative(width - dpToPx(EXPANDABLE_INDICATOR_START_OFFSET), width - dpToPx(EXPANDABLE_INDICATOR_END_OFFSET));
    }

    private int dpToPx(int dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void showSdkBuildInformation() {
        final SDKBuildInformation sdkBuildInformation = WikitudeSDK.getSDKBuildInformation();
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.build_information_title)
                .setMessage(
                        getString(R.string.build_information_config) + sdkBuildInformation.getBuildConfiguration() + "\n" +
                                getString(R.string.build_information_date) + sdkBuildInformation.getBuildDate() + "\n" +
                                getString(R.string.build_information_number) + sdkBuildInformation.getBuildNumber() + "\n" +
                                getString(R.string.build_information_version) + WikitudeSDK.getSDKVersion()
                )
                .show();
    }
}
