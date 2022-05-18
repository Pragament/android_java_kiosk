package com.example.lockappforglasses;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    DevicePolicyManager deviceManger;
    ComponentName compName;
    View view;
    Boolean mIsKioskEnabled = false;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        deviceManger = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        DeviceAdmin deviceAdmin = new DeviceAdmin();
        compName = deviceAdmin.getComponentName(this);

        if (!deviceManger.isAdminActive(compName)) {
            Toast.makeText(this, getString(R.string.not_device_admin), Toast.LENGTH_SHORT).show();
        }

        if (deviceManger.isDeviceOwnerApp(getPackageName())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String [] packageName = {getPackageName()};
                deviceManger.setLockTaskPackages(compName, packageName);
            }
        } else {
            Toast.makeText(this, getString(R.string.not_device_owner), Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enableKioskMode(true);
        }

        view = getWindow().getDecorView();
    }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            protected void onResume() {
            super.onResume();
                hideSystemUI();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void enableKioskMode(boolean enable) {
            if (enable) {
                if (deviceManger.isLockTaskPermitted(this.getPackageName())) {
                    startLockTask();
                    mIsKioskEnabled = true;
                    Toast.makeText(this, "Your Phone is Locked", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Kiosk Not Permitted", Toast.LENGTH_SHORT).show();
                }
            }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void hideSystemUI() {

        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}