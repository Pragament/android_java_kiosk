package com.example.lockappforglasses;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


// import org.adblockplus.libadblockplus.android.webview.AdblockWebView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DevicePolicyManager deviceManger;
    ComponentName compName;
    View view;
    Boolean isAppLockEnabledLocal = true;
    Boolean isChromeModeEnabledLocal = true;
    private static final String TAG = "MainActivity";
    private static final String CLASSROOMS_COLLECTION = "classrooms";
    private static final String CLASS_CODE_FIELD = "classCode";
    private static final String CLASS_ENABLED_FIELD = "classEnabled";
    private static final String QUIZ_MODE_ENABLED_FIELD = "quizModeEnabled";
    private static final String QUESTION_BANK_LIST_ID_FIELD = "questionBankListId";
    private static final String RANDOM_QUESTION_TYPE_COUNTS_FIELD = "randomQuestionTypeCounts";
    private static final String STUDENT_DIFFICULTY_LEVELS_FIELD = "studentDifficultyLevels";
    private static final String CLASS_SECTIONS_COLLECTION = "classSections";
    private static final String SECTION_ID_FIELD = "sectionId";
    private static final String STUDENTS_COLLECTION = "students";
    private static final String ADMISSION_NO_FIELD = "admissionNo";
    private static final String PHONE_FIELD = "phone";
    private static final String WHITELISTED_APPS_COLLECTION = "whitelistedApps";
    private static final String WHITELISTED_WEBSITES_COLLECTION = "whitelistedWebsites";
    Boolean mIsKioskEnabled = false;
    WebView mWebView;
    LinearLayout llWhitelistedApps;
    LinearLayout llWhitelistedWebsites;
    ScrollView whitelistedAppsScrollView;
    ScrollView whitelistedWebsitesScrollView;
    TextView txtWhitelistedAppsTitle;
    TextView txtWhitelistedWebsitesTitle;
    Button buttonRefresh;
    Button buttonBack;
    ProgressBar progressBar;
    TextView txtView;
    LinearLayout llTools;
    Button buttonQuiz;
    Boolean isChromeModeEnabled;
    boolean isWhitelistedAppsExpanded = true;
    boolean isWhitelistedWebsitesExpanded = true;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_main);

        isChromeModeEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_opt_chrome_mode), true);
        buttonRefresh = (Button) findViewById(R.id.buttonRefresh);
        buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonQuiz = (Button) findViewById(R.id.buttonQuiz);
        llTools = findViewById(R.id.llTools);
        llWhitelistedApps = findViewById(R.id.llWhitelistedApps);
        llWhitelistedWebsites = findViewById(R.id.llWhitelistedWebsites);
        whitelistedAppsScrollView = findViewById(R.id.whitelistedAppsScrollView);
        whitelistedWebsitesScrollView = findViewById(R.id.whitelistedWebsitesScrollView);
        txtWhitelistedAppsTitle = findViewById(R.id.txtWhitelistedAppsTitle);
        txtWhitelistedWebsitesTitle = findViewById(R.id.txtWhitelistedWebsitesTitle);
        txtWhitelistedAppsTitle.setOnClickListener(v -> toggleWhitelistedApps());
        txtWhitelistedWebsitesTitle.setOnClickListener(v -> toggleWhitelistedWebsites());
        buttonQuiz.setOnClickListener(v -> openQuizActivity());
        setupWebView();
        if (!isChromeModeEnabled) {
            //buttonRefresh.setVisibility(View.VISIBLE);
            buttonRefresh.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mWebView.reload();
                }
            });
            //buttonBack.setVisibility(View.VISIBLE);
            buttonBack.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mWebView.goBack();
                }
            });
        }else{
            llTools.setOrientation(LinearLayout.VERTICAL);
            buttonRefresh.setText("Launch web browser");
            buttonRefresh.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Log.d(TAG, "onClick: 12321");
                    try {
                    //String url = "https://www.freecodecamp.org/learn/2022/responsive-web-design/";
                    //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    //Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    //Intent browserIntent = new Intent("com.android.chrome");
                    Intent browserIntent = getPackageManager().getLaunchIntentForPackage("com.android.chrome");
                     //  Log.d(TAG, "onClick: "+browserIntent.getPackage());
                        if (browserIntent != null) {
                            startActivity(browserIntent);
                        }
                    } catch (ActivityNotFoundException e) {
                        // TODO Auto-generated catch block
                        Log.d(TAG, "onClick: ActivityNotFoundException ");
                    }
                }
            });
            buttonBack.setText("New chrome tab with freecodecamp");
            buttonBack.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String url = "http://levelup.technikh.com/";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    //Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    startActivity(browserIntent);
                }
            });
        }
        Button buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog(MainActivity.this);
                //clearCookies(MainActivity.this);
                //mWebView.reload();
            }
        });
        Spinner websiteSpinner = (Spinner) findViewById(R.id.websiteSpinner);
        websiteSpinner.setOnItemSelectedListener(this);
        view = getWindow().getDecorView();

        if (savedInstanceState == null) {
            showClassCodeDialog();
        }
    }

    private void setupWebView() {
        mWebView = findViewById(R.id.webView);
        mWebView.setVisibility(View.VISIBLE);
        txtView = findViewById(R.id.txtView);
        txtView.setVisibility(View.GONE);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        WebViewClientImpl webViewClient = new WebViewClientImpl(progressBar, txtView, this);
        mWebView.setWebViewClient(webViewClient);
        mWebView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }



    private void showClassCodeDialog() {
        View dialogLayout = View.inflate(this, R.layout.dialog_input_class_code, null);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(dialogLayout)
                .setCancelable(false)
                .setTitle("Quiz Session Code")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Proceed", null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        TextInputEditText etClassCode = dialogLayout.findViewById(R.id.et_class_code);
        TextInputEditText etAdmissionNo = dialogLayout.findViewById(R.id.et_admission_no);
        TextInputEditText etPhone = dialogLayout.findViewById(R.id.et_phone);
        TextView tvPhoneHint = dialogLayout.findViewById(R.id.tv_phone_hint);

        etAdmissionNo.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String code = getTrimmedText(etClassCode);
                String admissionNo = getTrimmedText(etAdmissionNo);
                if (!code.isEmpty() && !admissionNo.isEmpty()) {
                    showStudentPhoneHint(code, admissionNo, etClassCode, etAdmissionNo, tvPhoneHint);
                }
            }
        });

        etPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String code = getTrimmedText(etClassCode);
                String admissionNo = getTrimmedText(etAdmissionNo);
                if (!code.isEmpty() && !admissionNo.isEmpty()) {
                    showStudentPhoneHint(code, admissionNo, etClassCode, etAdmissionNo, tvPhoneHint);
                }
            }
        });

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String code = getTrimmedText(etClassCode);
                    String admissionNo = getTrimmedText(etAdmissionNo);
                    String phone = getTrimmedText(etPhone);
                    if (code.isEmpty()) {
                        Toast.makeText(this, "Please enter code!", Toast.LENGTH_SHORT).show();
                        etClassCode.setError("Required");
                    } else if (admissionNo.isEmpty()) {
                        Toast.makeText(this, "Please enter admission number!", Toast.LENGTH_SHORT).show();
                        etAdmissionNo.setError("Required");
                    } else if (phone.isEmpty()) {
                        Toast.makeText(this, "Please enter phone number!", Toast.LENGTH_SHORT).show();
                        etPhone.setError("Required");
                        showStudentPhoneHint(code, admissionNo, etClassCode, etAdmissionNo, tvPhoneHint);
                    } else {
                        authenticateClassCode(code, admissionNo, phone, dialog, etClassCode, etAdmissionNo, etPhone, tvPhoneHint);
                    }
                });
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener(v -> {
                    finish();
                });
    }

    private String getTrimmedText(TextInputEditText editText) {
        if (editText.getEditableText() == null) {
            return "";
        }
        return editText.getEditableText().toString().trim();
    }

    private void authenticateClassCode(
            String code,
            String admissionNo,
            String phone,
            androidx.appcompat.app.AlertDialog dialog,
            TextInputEditText etClassCode,
            TextInputEditText etAdmissionNo,
            TextInputEditText etPhone,
            TextView tvPhoneHint) {
        Log.d(TAG, "authenticateClassCode: entered code='" + code + "', length=" + code.length());

        if (!hasNetworkConnection()) {
            Log.w(TAG, "authenticateClassCode: no active network connection");
            showClassCodeError(etClassCode, "No internet connection");
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Log.d(TAG, "authenticateClassCode: checking direct classroom path /" + CLASSROOMS_COLLECTION + "/" + code);
        firestore.collection(CLASSROOMS_COLLECTION)
                .document(code)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "authenticateClassCode: direct document id=" + documentSnapshot.getId()
                            + ", exists=" + documentSnapshot.exists()
                            + ", fromCache=" + documentSnapshot.getMetadata().isFromCache()
                            + ", pendingWrites=" + documentSnapshot.getMetadata().hasPendingWrites()
                            + ", data=" + documentSnapshot.getData());
                    if (documentSnapshot.exists()) {
                        authenticateStudentIfClassroomEnabled(documentSnapshot, admissionNo, phone, dialog, etClassCode, etAdmissionNo, etPhone, tvPhoneHint);
                    } else {
                        authenticateClassCodeField(firestore, code, admissionNo, phone, dialog, etClassCode, etAdmissionNo, etPhone, tvPhoneHint, false);
                    }
                })
                .addOnFailureListener(e -> {
                    handleClassCodeLookupFailure(etClassCode, e);
                });
    }

    private void authenticateClassCodeField(
            FirebaseFirestore firestore,
            String code,
            String admissionNo,
            String phone,
            androidx.appcompat.app.AlertDialog dialog,
            TextInputEditText etClassCode,
            TextInputEditText etAdmissionNo,
            TextInputEditText etPhone,
            TextView tvPhoneHint,
            boolean numericQuery) {
        Object classCode = code;
        if (numericQuery) {
            try {
                classCode = Long.parseLong(code);
            } catch (NumberFormatException e) {
                Log.w(TAG, "authenticateClassCodeField: code is not numeric: " + code, e);
                etClassCode.setError("Invalid code");
                return;
            }
        }

        Log.d(TAG, "authenticateClassCodeField: querying /" + CLASSROOMS_COLLECTION
                + " where " + CLASS_CODE_FIELD + " == " + classCode
                + " (" + classCode.getClass().getSimpleName() + ")");
        firestore.collection(CLASSROOMS_COLLECTION)
                .whereEqualTo(CLASS_CODE_FIELD, classCode)
                .get()
                .addOnSuccessListener(querySnapshot -> handleClassCodeQueryResult(
                        firestore,
                        code,
                        admissionNo,
                        phone,
                        dialog,
                        etClassCode,
                        etAdmissionNo,
                        etPhone,
                        tvPhoneHint,
                        numericQuery,
                        querySnapshot))
                .addOnFailureListener(e -> {
                    handleClassCodeLookupFailure(etClassCode, e);
                });
    }

    private void handleClassCodeQueryResult(
            FirebaseFirestore firestore,
            String code,
            String admissionNo,
            String phone,
            androidx.appcompat.app.AlertDialog dialog,
            TextInputEditText etClassCode,
            TextInputEditText etAdmissionNo,
            TextInputEditText etPhone,
            TextView tvPhoneHint,
            boolean numericQuery,
            QuerySnapshot querySnapshot) {
        Log.d(TAG, "handleClassCodeQueryResult: numericQuery=" + numericQuery
                + ", size=" + querySnapshot.size()
                + ", fromCache=" + querySnapshot.getMetadata().isFromCache()
                + ", pendingWrites=" + querySnapshot.getMetadata().hasPendingWrites());
        for (DocumentSnapshot classroom : querySnapshot.getDocuments()) {
            Log.d(TAG, "handleClassCodeQueryResult: matched classroom id=" + classroom.getId()
                    + ", exists=" + classroom.exists()
                    + ", data=" + classroom.getData());
        }

        if (!querySnapshot.isEmpty()) {
            DocumentSnapshot classroom = querySnapshot.getDocuments().get(0);
            authenticateStudentIfClassroomEnabled(classroom, admissionNo, phone, dialog, etClassCode, etAdmissionNo, etPhone, tvPhoneHint);
        } else if (!numericQuery && TextUtils.isDigitsOnly(code)) {
            Log.d(TAG, "handleClassCodeQueryResult: string query empty; retrying as numeric code");
            authenticateClassCodeField(firestore, code, admissionNo, phone, dialog, etClassCode, etAdmissionNo, etPhone, tvPhoneHint, true);
        } else {
            Log.w(TAG, "handleClassCodeQueryResult: no classroom found for code=" + code);
            etClassCode.setError("Invalid code");
        }
    }

    private void authenticateStudentIfClassroomEnabled(
            DocumentSnapshot classroom,
            String admissionNo,
            String phone,
            androidx.appcompat.app.AlertDialog dialog,
            TextInputEditText etClassCode,
            TextInputEditText etAdmissionNo,
            TextInputEditText etPhone,
            TextView tvPhoneHint) {
        if (!Boolean.TRUE.equals(classroom.getBoolean(CLASS_ENABLED_FIELD))) {
            Log.w(TAG, "authenticateStudentIfClassroomEnabled: classroom disabled, classroomId=" + classroom.getId());
            showClassCodeError(etClassCode, "Quiz session is disabled");
            return;
        }

        authenticateStudent(classroom, admissionNo, phone, dialog, etAdmissionNo, etPhone, tvPhoneHint);
    }

    private void authenticateStudent(
            DocumentSnapshot classroom,
            String admissionNo,
            String phone,
            androidx.appcompat.app.AlertDialog dialog,
            TextInputEditText etAdmissionNo,
            TextInputEditText etPhone,
            TextView tvPhoneHint) {
        String classroomId = classroom.getId();
        String sectionId = classroom.getString(SECTION_ID_FIELD);
        boolean quizModeEnabled = Boolean.TRUE.equals(classroom.getBoolean(QUIZ_MODE_ENABLED_FIELD));
        String questionBankListId = classroom.getString(QUESTION_BANK_LIST_ID_FIELD);
        Map<String, Integer> randomQuestionTypeCounts = getRandomQuestionTypeCounts(classroom);
        Map<String, String> studentDifficultyLevels = getStudentDifficultyLevels(classroom);
        if (TextUtils.isEmpty(sectionId)) {
            Log.w(TAG, "authenticateStudent: classroom has no sectionId, classroomId=" + classroomId);
            etAdmissionNo.setError("Unable to verify student section");
            return;
        }

        Log.d(TAG, "authenticateStudent: checking /" + CLASS_SECTIONS_COLLECTION + "/" + sectionId
                + "/" + STUDENTS_COLLECTION + "/" + admissionNo);
        FirebaseFirestore.getInstance()
                .collection(CLASS_SECTIONS_COLLECTION)
                .document(sectionId)
                .collection(STUDENTS_COLLECTION)
                .document(admissionNo)
                .get()
                .addOnSuccessListener(student -> {
                    if (student.exists()) {
                        verifyStudentPhone(classroomId, sectionId, quizModeEnabled, questionBankListId, randomQuestionTypeCounts, studentDifficultyLevels, student, phone, dialog, etPhone, tvPhoneHint);
                    } else {
                        authenticateStudentByAdmissionField(classroomId, sectionId, quizModeEnabled, questionBankListId, randomQuestionTypeCounts, studentDifficultyLevels, admissionNo, phone, dialog, etAdmissionNo, etPhone, tvPhoneHint, false);
                    }
                })
                .addOnFailureListener(e -> handleStudentLookupFailure(etAdmissionNo, e));
    }

    private void authenticateStudentByAdmissionField(
            String classroomId,
            String sectionId,
            boolean quizModeEnabled,
            String questionBankListId,
            Map<String, Integer> randomQuestionTypeCounts,
            Map<String, String> studentDifficultyLevels,
            String admissionNo,
            String phone,
            androidx.appcompat.app.AlertDialog dialog,
            TextInputEditText etAdmissionNo,
            TextInputEditText etPhone,
            TextView tvPhoneHint,
            boolean numericQuery) {
        Object admissionValue = admissionNo;
        if (numericQuery) {
            try {
                admissionValue = Long.parseLong(admissionNo);
            } catch (NumberFormatException e) {
                Log.w(TAG, "authenticateStudentByAdmissionField: admission number is not numeric: " + admissionNo, e);
                etAdmissionNo.setError("Invalid admission number");
                return;
            }
        }

        FirebaseFirestore.getInstance()
                .collection(CLASS_SECTIONS_COLLECTION)
                .document(sectionId)
                .collection(STUDENTS_COLLECTION)
                .whereEqualTo(ADMISSION_NO_FIELD, admissionValue)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        verifyStudentPhone(classroomId, sectionId, quizModeEnabled, questionBankListId, randomQuestionTypeCounts, studentDifficultyLevels, querySnapshot.getDocuments().get(0), phone, dialog, etPhone, tvPhoneHint);
                    } else if (!numericQuery && TextUtils.isDigitsOnly(admissionNo)) {
                        authenticateStudentByAdmissionField(classroomId, sectionId, quizModeEnabled, questionBankListId, randomQuestionTypeCounts, studentDifficultyLevels, admissionNo, phone, dialog, etAdmissionNo, etPhone, tvPhoneHint, true);
                    } else {
                        Log.w(TAG, "authenticateStudentByAdmissionField: no student found for admissionNo=" + admissionNo);
                        etAdmissionNo.setError("Invalid admission number");
                    }
                })
                .addOnFailureListener(e -> handleStudentLookupFailure(etAdmissionNo, e));
    }

    private void verifyStudentPhone(
            String classroomId,
            String sectionId,
            boolean quizModeEnabled,
            String questionBankListId,
            Map<String, Integer> randomQuestionTypeCounts,
            Map<String, String> studentDifficultyLevels,
            DocumentSnapshot student,
            String phone,
            androidx.appcompat.app.AlertDialog dialog,
            TextInputEditText etPhone,
            TextView tvPhoneHint) {
        String registeredPhone = normalizePhone(student.getString(PHONE_FIELD));
        String enteredPhone = normalizePhone(phone);
        showPhoneSuffixHint(registeredPhone, tvPhoneHint);

        if (TextUtils.isEmpty(registeredPhone) || !registeredPhone.equals(enteredPhone)) {
            Log.w(TAG, "verifyStudentPhone: phone mismatch for student=" + student.getId());
            etPhone.setError("Phone number does not match");
            Toast.makeText(this, "Phone number does not match", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(TAG, "verifyStudentPhone: verified classroomId=" + classroomId + ", student=" + student.getId());
        ((MyApp) getApplicationContext()).setCurrentStudent(
                sectionId,
                getStudentAdmissionNo(student),
                student.getString("name"));
        ((MyApp) getApplicationContext()).setCurrentQuizSessionOptions(randomQuestionTypeCounts, studentDifficultyLevels);
        onClassCodeVerified(classroomId, dialog, quizModeEnabled, questionBankListId);
    }

    private Map<String, Integer> getRandomQuestionTypeCounts(DocumentSnapshot classroom) {
        Map<String, Integer> counts = new HashMap<>();
        Object rawCounts = classroom.get(RANDOM_QUESTION_TYPE_COUNTS_FIELD);
        if (!(rawCounts instanceof Map)) {
            return counts;
        }

        Map<?, ?> rawMap = (Map<?, ?>) rawCounts;
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            Integer count = parseNonNegativeInteger(entry.getValue());
            if (count != null) {
                counts.put(entry.getKey().toString(), count);
            }
        }
        return counts;
    }

    private Integer parseNonNegativeInteger(Object value) {
        if (value instanceof Number) {
            return Math.max(0, ((Number) value).intValue());
        }
        if (value instanceof String && TextUtils.isDigitsOnly((String) value)) {
            return Integer.parseInt((String) value);
        }
        return null;
    }

    private Map<String, String> getStudentDifficultyLevels(DocumentSnapshot classroom) {
        Map<String, String> levels = new HashMap<>();
        Object rawLevels = classroom.get(STUDENT_DIFFICULTY_LEVELS_FIELD);
        if (!(rawLevels instanceof Map)) {
            return levels;
        }

        Map<?, ?> rawMap = (Map<?, ?>) rawLevels;
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            String difficulty = entry.getValue().toString().trim();
            if (!TextUtils.isEmpty(difficulty)) {
                levels.put(entry.getKey().toString(), difficulty);
            }
        }
        return levels;
    }

    private String getStudentAdmissionNo(DocumentSnapshot student) {
        Object admissionNo = student.get(ADMISSION_NO_FIELD);
        if (admissionNo instanceof Number) {
            return String.valueOf(((Number) admissionNo).longValue());
        }
        if (admissionNo != null && !TextUtils.isEmpty(admissionNo.toString())) {
            return admissionNo.toString();
        }
        return student.getId();
    }

    private void openQuizActivity() {
        if (TextUtils.isEmpty(((MyApp) getApplicationContext()).getCurrentClassCode())) {
            Toast.makeText(this, "Verify quiz session first", Toast.LENGTH_SHORT).show();
            showClassCodeDialog();
            return;
        }
        startActivity(new Intent(this, QuizActivity.class));
    }

    private void showStudentPhoneHint(
            String code,
            String admissionNo,
            TextInputEditText etClassCode,
            TextInputEditText etAdmissionNo,
            TextView tvPhoneHint) {
        if (!hasNetworkConnection()) {
            showClassCodeError(etClassCode, "No internet connection");
            return;
        }

        tvPhoneHint.setText("Checking registered phone...");
        tvPhoneHint.setVisibility(View.VISIBLE);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(CLASSROOMS_COLLECTION)
                .document(code)
                .get()
                .addOnSuccessListener(classroom -> {
                    if (classroom.exists()) {
                        showStudentPhoneHintForClassroom(classroom, admissionNo, etClassCode, etAdmissionNo, tvPhoneHint);
                    } else {
                        showStudentPhoneHintByClassCodeField(firestore, code, admissionNo, etClassCode, etAdmissionNo, tvPhoneHint, false);
                    }
                })
                .addOnFailureListener(e -> handleClassCodeLookupFailure(etClassCode, e));
    }

    private void showStudentPhoneHintByClassCodeField(
            FirebaseFirestore firestore,
            String code,
            String admissionNo,
            TextInputEditText etClassCode,
            TextInputEditText etAdmissionNo,
            TextView tvPhoneHint,
            boolean numericQuery) {
        Object classCode = code;
        if (numericQuery) {
            try {
                classCode = Long.parseLong(code);
            } catch (NumberFormatException e) {
                etClassCode.setError("Invalid code");
                tvPhoneHint.setVisibility(View.GONE);
                return;
            }
        }

        firestore.collection(CLASSROOMS_COLLECTION)
                .whereEqualTo(CLASS_CODE_FIELD, classCode)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        showStudentPhoneHintForClassroom(querySnapshot.getDocuments().get(0), admissionNo, etClassCode, etAdmissionNo, tvPhoneHint);
                    } else if (!numericQuery && TextUtils.isDigitsOnly(code)) {
                        showStudentPhoneHintByClassCodeField(firestore, code, admissionNo, etClassCode, etAdmissionNo, tvPhoneHint, true);
                    } else {
                        etClassCode.setError("Invalid code");
                        tvPhoneHint.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> handleClassCodeLookupFailure(etClassCode, e));
    }

    private void showStudentPhoneHintForClassroom(
            DocumentSnapshot classroom,
            String admissionNo,
            TextInputEditText etClassCode,
            TextInputEditText etAdmissionNo,
            TextView tvPhoneHint) {
        if (!Boolean.TRUE.equals(classroom.getBoolean(CLASS_ENABLED_FIELD))) {
            showClassCodeError(etClassCode, "Quiz session is disabled");
            tvPhoneHint.setVisibility(View.GONE);
            return;
        }

        String sectionId = classroom.getString(SECTION_ID_FIELD);
        if (TextUtils.isEmpty(sectionId)) {
            etAdmissionNo.setError("Unable to verify student section");
            tvPhoneHint.setVisibility(View.GONE);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(CLASS_SECTIONS_COLLECTION)
                .document(sectionId)
                .collection(STUDENTS_COLLECTION)
                .document(admissionNo)
                .get()
                .addOnSuccessListener(student -> {
                    if (student.exists()) {
                        showPhoneSuffixHint(normalizePhone(student.getString(PHONE_FIELD)), tvPhoneHint);
                    } else {
                        showStudentPhoneHintByAdmissionField(sectionId, admissionNo, etAdmissionNo, tvPhoneHint, false);
                    }
                })
                .addOnFailureListener(e -> handleStudentLookupFailure(etAdmissionNo, e));
    }

    private void showStudentPhoneHintByAdmissionField(
            String sectionId,
            String admissionNo,
            TextInputEditText etAdmissionNo,
            TextView tvPhoneHint,
            boolean numericQuery) {
        Object admissionValue = admissionNo;
        if (numericQuery) {
            try {
                admissionValue = Long.parseLong(admissionNo);
            } catch (NumberFormatException e) {
                etAdmissionNo.setError("Invalid admission number");
                tvPhoneHint.setVisibility(View.GONE);
                return;
            }
        }

        FirebaseFirestore.getInstance()
                .collection(CLASS_SECTIONS_COLLECTION)
                .document(sectionId)
                .collection(STUDENTS_COLLECTION)
                .whereEqualTo(ADMISSION_NO_FIELD, admissionValue)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        showPhoneSuffixHint(normalizePhone(querySnapshot.getDocuments().get(0).getString(PHONE_FIELD)), tvPhoneHint);
                    } else if (!numericQuery && TextUtils.isDigitsOnly(admissionNo)) {
                        showStudentPhoneHintByAdmissionField(sectionId, admissionNo, etAdmissionNo, tvPhoneHint, true);
                    } else {
                        etAdmissionNo.setError("Invalid admission number");
                        tvPhoneHint.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> handleStudentLookupFailure(etAdmissionNo, e));
    }

    private void showPhoneSuffixHint(String phone, TextView tvPhoneHint) {
        if (TextUtils.isEmpty(phone)) {
            tvPhoneHint.setVisibility(View.GONE);
            return;
        }

        String suffix = phone.length() <= 3 ? phone : phone.substring(phone.length() - 3);
        tvPhoneHint.setText("Registered phone ends with " + suffix);
        tvPhoneHint.setVisibility(View.VISIBLE);
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("[^0-9]", "");
    }

    private void handleStudentLookupFailure(TextInputEditText editText, Exception e) {
        Log.w(TAG, "Unable to verify student", e);
        editText.setError("Unable to verify student");
        Toast.makeText(this, "Unable to verify student. Check internet or DNS.", Toast.LENGTH_SHORT).show();
    }

    private void onClassCodeVerified(String classroomId, androidx.appcompat.app.AlertDialog dialog, boolean quizModeEnabled, String questionBankListId) {
        boolean hasQuestionBankList = !TextUtils.isEmpty(questionBankListId);
        Log.i(TAG, "onClassCodeVerified: classroomId=" + classroomId
                + ", quizModeEnabled=" + quizModeEnabled
                + ", questionBankListId=" + questionBankListId);
        Toast.makeText(this, "Verified", Toast.LENGTH_SHORT).show();
        MyApp app = (MyApp) getApplicationContext();
        app.setCurrentClassCode(classroomId);
        app.setCurrentQuizModeEnabled(quizModeEnabled || hasQuestionBankList);
        app.setCurrentQuestionBankListId(questionBankListId);
        dialog.dismiss();
        if (quizModeEnabled || hasQuestionBankList) {
            hideHomepageForQuizMode();
            openQuizActivity();
        } else {
            loadWhitelistedApps(classroomId);
        }
    }

    private void hideHomepageForQuizMode() {
        if (llTools != null) {
            llTools.setVisibility(View.GONE);
        }
        if (txtWhitelistedAppsTitle != null) {
            txtWhitelistedAppsTitle.setVisibility(View.GONE);
        }
        if (whitelistedAppsScrollView != null) {
            whitelistedAppsScrollView.setVisibility(View.GONE);
        }
        if (txtWhitelistedWebsitesTitle != null) {
            txtWhitelistedWebsitesTitle.setVisibility(View.GONE);
        }
        if (whitelistedWebsitesScrollView != null) {
            whitelistedWebsitesScrollView.setVisibility(View.GONE);
        }
        if (mWebView != null) {
            mWebView.setVisibility(View.GONE);
            mWebView.loadUrl("about:blank");
        }
    }

    private void loadWhitelistedApps(String classroomId) {
        showWebViewToolbar();
        showWebViewContent();
        showWhitelistedAppsMessage("Loading apps...");
        showWhitelistedWebsitesMessage("Loading websites...");

        String whitelistPath = "/" + CLASSROOMS_COLLECTION + "/" + classroomId + "/" + WHITELISTED_APPS_COLLECTION;
        Log.i(TAG, "loadWhitelistedApps: querying " + whitelistPath);
        FirebaseFirestore.getInstance()
                .collection(CLASSROOMS_COLLECTION)
                .document(classroomId)
                .collection(WHITELISTED_APPS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> showWhitelistedApps(querySnapshot))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "loadWhitelistedApps: unable to load whitelisted apps from " + whitelistPath, e);
                    showWhitelistedAppsMessage("Unable to load apps. Check internet or DNS.");
                });
        loadWhitelistedWebsites(classroomId);
    }

    private void loadWhitelistedWebsites(String classroomId) {
        String whitelistPath = "/" + CLASSROOMS_COLLECTION + "/" + classroomId + "/" + WHITELISTED_WEBSITES_COLLECTION;
        Log.i(TAG, "loadWhitelistedWebsites: querying " + whitelistPath);
        FirebaseFirestore.getInstance()
                .collection(CLASSROOMS_COLLECTION)
                .document(classroomId)
                .collection(WHITELISTED_WEBSITES_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> showWhitelistedWebsites(querySnapshot))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "loadWhitelistedWebsites: unable to load whitelisted websites from " + whitelistPath, e);
                    showWhitelistedWebsitesMessage("Unable to load websites. Check internet or DNS.");
                });
    }

    private void showWhitelistedApps(QuerySnapshot querySnapshot) {
        Log.i(TAG, "showWhitelistedApps: size=" + querySnapshot.size()
                + ", fromCache=" + querySnapshot.getMetadata().isFromCache()
                + ", pendingWrites=" + querySnapshot.getMetadata().hasPendingWrites());

        llWhitelistedApps.removeAllViews();
        whitelistedAppsScrollView.setVisibility(View.VISIBLE);
        txtWhitelistedAppsTitle.setVisibility(View.VISIBLE);
        updateWhitelistedAppsTitle();

        if (querySnapshot.isEmpty()) {
            Log.w(TAG, "showWhitelistedApps: query returned empty whitelist");
            addWhitelistedAppsMessage("No whitelisted apps found.");
            return;
        }

        for (DocumentSnapshot appDocument : querySnapshot.getDocuments()) {
            Log.i(TAG, "showWhitelistedApps: whitelist package=" + appDocument.getId()
                    + ", exists=" + appDocument.exists()
                    + ", fromCache=" + appDocument.getMetadata().isFromCache()
                    + ", data=" + appDocument.getData());
            addWhitelistedAppButton(appDocument.getId());
        }
    }

    private void showWhitelistedAppsMessage(String message) {
        showWebViewContent();
        whitelistedAppsScrollView.setVisibility(View.VISIBLE);
        txtWhitelistedAppsTitle.setVisibility(View.VISIBLE);
        updateWhitelistedAppsTitle();
        llWhitelistedApps.removeAllViews();
        addWhitelistedAppsMessage(message);
    }

    private void showWhitelistedWebsites(QuerySnapshot querySnapshot) {
        Log.i(TAG, "showWhitelistedWebsites: size=" + querySnapshot.size()
                + ", fromCache=" + querySnapshot.getMetadata().isFromCache()
                + ", pendingWrites=" + querySnapshot.getMetadata().hasPendingWrites());

        llWhitelistedWebsites.removeAllViews();
        whitelistedWebsitesScrollView.setVisibility(View.VISIBLE);
        txtWhitelistedWebsitesTitle.setVisibility(View.VISIBLE);
        updateWhitelistedWebsitesTitle();

        if (querySnapshot.isEmpty()) {
            Log.w(TAG, "showWhitelistedWebsites: query returned empty whitelist");
            addWhitelistedWebsitesMessage("No whitelisted websites found.");
            return;
        }

        for (DocumentSnapshot websiteDocument : querySnapshot.getDocuments()) {
            String url = websiteDocument.getString("url");
            String title = websiteDocument.getString("title");
            String host = websiteDocument.getString("host");
            Log.i(TAG, "showWhitelistedWebsites: website id=" + websiteDocument.getId()
                    + ", url=" + url
                    + ", title=" + title
                    + ", host=" + host
                    + ", fromCache=" + websiteDocument.getMetadata().isFromCache()
                    + ", data=" + websiteDocument.getData());
            addWhitelistedWebsiteButton(websiteDocument.getId(), title, host, url);
        }
    }

    private void showWhitelistedWebsitesMessage(String message) {
        showWebViewContent();
        whitelistedWebsitesScrollView.setVisibility(View.VISIBLE);
        txtWhitelistedWebsitesTitle.setVisibility(View.VISIBLE);
        updateWhitelistedWebsitesTitle();
        llWhitelistedWebsites.removeAllViews();
        addWhitelistedWebsitesMessage(message);
    }

    private void showWebViewContent() {
        if (mWebView == null) {
            return;
        }

        mWebView.setVisibility(View.VISIBLE);
    }

    private void showWebViewToolbar() {
        llTools.setOrientation(LinearLayout.HORIZONTAL);
        buttonRefresh.setVisibility(View.VISIBLE);
        buttonBack.setVisibility(View.VISIBLE);
        buttonRefresh.setText("Refresh");
        buttonBack.setText("Back");

        buttonRefresh.setOnClickListener(v -> {
            if (mWebView != null) {
                mWebView.reload();
            }
        });
        buttonBack.setOnClickListener(v -> {
            if (mWebView != null && mWebView.canGoBack()) {
                mWebView.goBack();
            }
        });
    }

    private void toggleWhitelistedApps() {
        isWhitelistedAppsExpanded = !isWhitelistedAppsExpanded;
        whitelistedAppsScrollView.setVisibility(isWhitelistedAppsExpanded ? View.VISIBLE : View.GONE);
        updateWhitelistedAppsTitle();
    }

    private void toggleWhitelistedWebsites() {
        isWhitelistedWebsitesExpanded = !isWhitelistedWebsitesExpanded;
        whitelistedWebsitesScrollView.setVisibility(isWhitelistedWebsitesExpanded ? View.VISIBLE : View.GONE);
        updateWhitelistedWebsitesTitle();
    }

    private void updateWhitelistedAppsTitle() {
        txtWhitelistedAppsTitle.setText(isWhitelistedAppsExpanded ? "Whitelisted Apps [-]" : "Whitelisted Apps [+]");
    }

    private void updateWhitelistedWebsitesTitle() {
        txtWhitelistedWebsitesTitle.setText(isWhitelistedWebsitesExpanded ? "Whitelisted Websites [-]" : "Whitelisted Websites [+]");
    }

    private void hideWebViewStatus() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (txtView != null) {
            txtView.setVisibility(View.GONE);
        }
    }

    private void addWhitelistedAppsMessage(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(getResources().getColor(R.color.black));
        textView.setTextSize(16);
        textView.setPadding(0, 8, 0, 8);
        llWhitelistedApps.addView(textView);
    }

    private void addWhitelistedWebsitesMessage(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(getResources().getColor(R.color.black));
        textView.setTextSize(16);
        textView.setPadding(0, 8, 0, 8);
        llWhitelistedWebsites.addView(textView);
    }

    private void addWhitelistedAppButton(String packageName) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(getAppDisplayName(packageName));
        button.setOnClickListener(v -> launchWhitelistedApp(packageName));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 8, 0, 8);
        llWhitelistedApps.addView(button, layoutParams);
    }

    private void addWhitelistedWebsiteButton(String documentId, String title, String host, String url) {
        String websiteUrl = getWebsiteUrl(documentId, host, url);
        String buttonText = getWebsiteDisplayName(documentId, title, host, websiteUrl);

        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(buttonText);
        button.setOnClickListener(v -> openWhitelistedWebsite(websiteUrl));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 8, 0, 8);
        llWhitelistedWebsites.addView(button, layoutParams);
    }

    private String getWebsiteUrl(String documentId, String host, String url) {
        if (!TextUtils.isEmpty(url)) {
            return url;
        }

        String websiteHost = !TextUtils.isEmpty(host) ? host : documentId;
        if (websiteHost.startsWith("http://") || websiteHost.startsWith("https://")) {
            return websiteHost;
        }
        return "http://" + websiteHost;
    }

    private String getWebsiteDisplayName(String documentId, String title, String host, String url) {
        if (!TextUtils.isEmpty(title)) {
            return title;
        }
        if (!TextUtils.isEmpty(host)) {
            return host;
        }
        if (!TextUtils.isEmpty(url)) {
            return url;
        }
        return documentId;
    }

    private void openWhitelistedWebsite(String url) {
        Log.i(TAG, "openWhitelistedWebsite: url=" + url);
        showWebViewContent();
        mWebView.loadUrl(url);
    }

    private String getAppDisplayName(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            String appName = packageManager.getApplicationLabel(appInfo).toString();
            Log.d(TAG, "getAppDisplayName: package=" + packageName + ", label=" + appName);
            return appName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "getAppDisplayName: package not installed or not visible: " + packageName, e);
            return packageName;
        }
    }

    private void launchWhitelistedApp(String packageName) {
        Log.d(TAG, "launchWhitelistedApp: package=" + packageName);
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Log.w(TAG, "launchWhitelistedApp: no launch intent for " + packageName);
            Toast.makeText(this, "App not installed: " + packageName, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean connected = activeNetwork != null && activeNetwork.isConnected();
        Log.d(TAG, "hasNetworkConnection: connected=" + connected
                + ", activeNetwork=" + (activeNetwork == null ? "null" : activeNetwork.toString()));
        return connected;
    }

    private void handleClassCodeLookupFailure(TextInputEditText etClassCode, Exception e) {
        Log.w(TAG, "Unable to verify classroom code", e);
        showClassCodeError(etClassCode, "Unable to reach server. Check internet or DNS.");
    }

    private void showClassCodeError(TextInputEditText etClassCode, String message) {
        etClassCode.setError(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String url = "";
        switch (pos) {
            case 0:
                Toast.makeText(parent.getContext(), "Loading freecodecamp!", Toast.LENGTH_LONG).show();
                url = "https://www.freecodecamp.org/learn/2022/responsive-web-design/";
                break;
            case 1:
                Toast.makeText(parent.getContext(), "Loading Personal Webpage!", Toast.LENGTH_LONG).show();
                //Personal Webpage
                url = "https://codepen.io/SquishyAndroid/pen/XjRPVV?editors=1000";
                break;
            case 2:
                Toast.makeText(parent.getContext(), "Loading Portfolio Page!", Toast.LENGTH_LONG).show();
                //Portfolio Page
                url = "https://codepen.io/benthedev/pen/rjZPRG?editors=1000";
                break;
            case 3:
                Toast.makeText(parent.getContext(), "Loading Portfolio Webpage!", Toast.LENGTH_LONG).show();
                //Portfolio Webpage
                url="https://codepen.io/bbrady/pen/WxqPqx?editors=1000";
                break;
            case 4:
                Toast.makeText(parent.getContext(), "Loading Portfolio Webpage!", Toast.LENGTH_LONG).show();
                //Portfolio Webpage
                url="https://codepen.io/mjakal/pen/rLYbLJ?editors=1000";
                break;
            case 5:
                Toast.makeText(parent.getContext(), "Loading Restaurant Webpage!", Toast.LENGTH_LONG).show();
                //Restaurant Webpage
                url="https://codepen.io/AntenaGames/pen/JjKRKmm?editors=1000";
                break;
        }
        if (!url.isEmpty()) {
            Boolean isChromeModeEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(getString(R.string.pref_opt_chrome_mode), true);
            if (!isChromeModeEnabled) {
                whitelistedAppsScrollView.setVisibility(View.GONE);
                txtWhitelistedAppsTitle.setVisibility(View.GONE);
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                if (txtView != null) {
                    txtView.setVisibility(View.VISIBLE);
                }
                mWebView.setVisibility(View.VISIBLE);
                mWebView.loadUrl(url);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void showDialog(Context context) {

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.password_prompt, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);

        final EditText etCurrentPassword = (EditText) promptsView
                .findViewById(R.id.etCurrentPassword);
        final EditText etNewPassword = (EditText) promptsView
                .findViewById(R.id.etNewPassword);
        final EditText etUserName = (EditText) promptsView
                .findViewById(R.id.etUserName);
        Boolean isChromeModeEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_opt_chrome_mode), true);
        final Switch switchChromeMode = (Switch) promptsView
                .findViewById(R.id.switchChromeMode);
        switchChromeMode.setChecked(isChromeModeEnabled);
        switchChromeMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isChromeModeEnabledLocal = true;
                } else {
                    isChromeModeEnabledLocal = false;
                }
            }
        });
        Boolean isAppLockEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_opt_app_lock), true);
        final Switch switchKioskMode = (Switch) promptsView
                .findViewById(R.id.switchKioskMode);
        switchKioskMode.setChecked(isAppLockEnabled);
        switchKioskMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isAppLockEnabledLocal = true;
                } else {
                    isAppLockEnabledLocal = false;
                }
            }
        });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                /** DO THE METHOD HERE WHEN PROCEED IS CLICKED*/
                                String etCurrentPasswordText = (etCurrentPassword.getText()).toString();
                                String etNewPasswordText = (etNewPassword.getText()).toString();
                                String etUserNameText = (etUserName.getText()).toString();

                                if (!etUserNameText.trim().isEmpty()) {
                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                    preferences.edit().putString("userName", etUserNameText).apply();
                                }

                                /** CHECK FOR USER'S INPUT **/
                                SharedPreferences mySharedPreferences;
                                mySharedPreferences = getSharedPreferences("settingspassworddetails", MODE_PRIVATE);
                                String password = mySharedPreferences.getString("password", "");

                                if (etCurrentPasswordText.equals(password)) {
                                    if (!TextUtils.isEmpty(etNewPasswordText)) {
                                        SharedPreferences.Editor myEditor;
                                        myEditor = mySharedPreferences.edit();
                                        myEditor.putString("password", etNewPasswordText);
                                        myEditor.commit();
                                        //dialog.dismiss();
                                        //return;
                                    }
                                    //Log.d(user_text, "HELLO THIS IS THE MESSAGE CAUGHT :)");
                                    //Search_Tips(user_text);
                                    SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                    SharedPreferences.Editor defaultSharedPreferencesEditor = defaultSharedPreferences.edit();
                                    defaultSharedPreferencesEditor.putBoolean(getString(R.string.pref_opt_app_lock), isAppLockEnabledLocal);
                                    defaultSharedPreferencesEditor.putBoolean(getString(R.string.pref_opt_chrome_mode), isChromeModeEnabledLocal);
                                    defaultSharedPreferencesEditor.commit();
                                } else {
                                    //Log.d(user_text,"string is empty");
                                    dialog.dismiss();
                                    finish();
                                }
                            }
                        })
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                finish();
                            }

                        }

                );

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d(TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            Log.d(TAG, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        MyApp app = (MyApp) getApplicationContext();
        if (app.isCurrentQuizModeEnabled() && !TextUtils.isEmpty(app.getCurrentClassCode())) {
            hideHomepageForQuizMode();
            openQuizActivity();
        }
    }

    @Override
    public void onBackPressed() {
        // nothing to do here
        // … really
        //showDialog(MainActivity.this);
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
