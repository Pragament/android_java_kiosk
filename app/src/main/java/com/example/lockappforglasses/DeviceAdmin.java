package com.example.lockappforglasses;

import android.app.admin.DeviceAdminReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

public class DeviceAdmin extends DeviceAdminReceiver {

    public ComponentName getComponentName(Context context) {

        Log.d("Component Name", new ComponentName(context, DeviceAdmin.class).toString());

        return new ComponentName(context, DeviceAdmin.class);

    }

    
}
