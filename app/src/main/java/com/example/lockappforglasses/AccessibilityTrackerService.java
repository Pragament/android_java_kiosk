package com.example.lockappforglasses;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

//import com.eschool2go.studentapp.common.AppsDatabaseHelper;
//import com.eschool2go.studentapp.common.DatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;

public class AccessibilityTrackerService extends AccessibilityService {

    static final String TAG = "RecorderService";
    private String current_package_id = "";
    private long current_package_start_time = 0;
    // private AppsDatabaseHelper AppsDB;

    private String getEventType(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                return "TYPE_NOTIFICATION_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                return "TYPE_VIEW_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                return "TYPE_VIEW_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                return "TYPE_VIEW_LONG_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                return "TYPE_VIEW_SELECTED";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "TYPE_WINDOW_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                return "TYPE_VIEW_TEXT_CHANGED";
        }
        return "default";
    }
    /*private DatabaseHelper.EventSaveListener mEventSaveListener = new DatabaseHelper.EventSaveListener() {
        @Override
        public void onEventSaved(long id) {
            LocalBroadcastManager.getInstance(AccessibilityTrackerService.this).sendBroadcast(new Intent(Constants.INTENT_APP_EVENT_ADDED));
        }
    };*/

    private String getEventText(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText()) {
            sb.append(s);
        }
        return sb.toString();
    }

    /*
	Check if App is allowed by Default like current app
	 */
    public Boolean isDefaultApp(String package_id) {
        Log.v(TAG, "checking *" + package_id + "*");
        String[] defaultApps = {
                "com.eschool2go.studentapp",
                "com.android.systemui",
                "com.amazon.firelauncher",
                "com.android.inputmethod.latin",
                "ily.mathricks"
        };

        if (Arrays.asList(defaultApps).contains(package_id)) {
            // true
            Log.v(TAG, "checking true *" + package_id + "*");
            return true;
        }
        Log.v(TAG, "checking false *" + package_id + "*");
        return false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //Log.v(TAG, "onAccessibilityEvent");
        /*Log.v(TAG, String.format(
                "onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
                getEventType(event), event.getClassName(), event.getPackageName(),
                event.getEventTime(), getEventText(event)));*/
        Boolean isAppLockEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_opt_app_lock), true);
        if (isAppLockEnabled) {
            String getPackageName = event.getPackageName().toString();
            Log.v(TAG, "onAccessibilityEvent " + getPackageName);
        /*if(current_package_start_time == 0) {
            current_package_start_time = System.currentTimeMillis();
        }*/
            current_package_id = "com.example.lockappforglasses";
            Boolean isChromeModeEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(getString(R.string.pref_opt_chrome_mode), true);

            //String[] webviewList = {current_package_id, "com.amazon.redstone"};
            ArrayList chromeList = new ArrayList();
            chromeList.add(current_package_id);
            chromeList.add("com.amazon.redstone");
            chromeList.add("com.google.android.inputmethod.latin");
            if (isChromeModeEnabled) {
                chromeList.add("com.android.chrome");
                //String[] chromeList = {current_package_id, "com.amazon.redstone", "com.android.chrome"};
                //bob = chromeList;
            }

            if (!chromeList.contains(getPackageName)) {
                Log.v(TAG, "onAccessibilityEventBlocked " + getPackageName);
                try {
                    String launch_package_id = current_package_id;
                    if (isChromeModeEnabled) {
                        launch_package_id = "com.android.chrome";
                    }
                    Intent browserIntent = getPackageManager().getLaunchIntentForPackage(launch_package_id);
                    if (browserIntent != null) {

                        Log.d(TAG, "onClick: startActivity browserIntent "+launch_package_id);
                        startActivity(browserIntent);
                    }
                } catch (ActivityNotFoundException e) {
                    // TODO Auto-generated catch block
                    Log.d(TAG, "onClick: ActivityNotFoundException ");
                    Intent lockIntent = new Intent(getBaseContext(), MainActivity.class);
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getBaseContext().startActivity(lockIntent);
                }
                //  long tEnd = System.currentTimeMillis();
                //  long tDelta = tEnd - current_package_start_time;
                //  double elapsedSeconds = tDelta / 1000.0;
                //Log.v(TAG, "onAccessibilityEvent New App different than"+current_package_id);
                //  Log.v(TAG, "onAccessibilityEvent "+current_package_id+" elapsedSeconds "+elapsedSeconds);
            /*if(elapsedSeconds > 0.7) {
                AppsDB = new AppsDatabaseHelper(this);
                if (!isDefaultApp(getPackageName)) {
                    Boolean isAppLockEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                            .getBoolean(getString(R.string.pref_opt_app_lock), true);
                    //Log.v(TAG, " isAppLockEnabled "+isAppLockEnabled);
                    if (isAppLockEnabled && !AppsDB.isPackageAllowed(getPackageName)) {
                        Intent lockIntent = new Intent(getBaseContext(), MainActivity.class);
                        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MainActivity.EXTRA_MESSAGE = getPackageName;
                        //lockIntent.putExtra(MainActivity.EXTRA_MESSAGE, getPackageName);
                        Log.v(TAG, "onCreate " + MainActivity.EXTRA_MESSAGE);
                        getBaseContext().startActivity(lockIntent);
                    }
                }
                if (AppsDB.isPackageAllowed(current_package_id)) {
                    //if (recordAnalytics && (elapsedSeconds > 2) && (!isDefaultApp(getPackageName) || (getPackageName == "com.eschool2go.studentapp"))) {
                    if ((elapsedSeconds > 2)) {
                        int elapsedSecondsRounded = (int) Math.round(elapsedSeconds);
                        DatabaseHelper.getInstance().saveEventToDbAsync(current_package_id, current_package_id, Integer.toString(elapsedSecondsRounded),
                                Long.toString(current_package_start_time), null);
                    }
                }
                current_package_start_time = System.currentTimeMillis();
                current_package_id = getPackageName;
            }*/
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.v(TAG, "onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.v(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
    }

}
