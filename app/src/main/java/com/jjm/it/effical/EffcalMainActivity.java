package com.jjm.it.effical;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import androidx.annotation.NonNull;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

//recorder
import com.jjm.it.effical.R;
import com.hbisoft.hbrecorder.HBRecorderListener;
import com.hbisoft.hbrecorder.HBRecorder;

//androidX
import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
//import java.sql.Date;
import java.util.Locale;

public class EffcalMainActivity extends AppCompatActivity implements HBRecorderListener{

    private static final int SCREEN_RECORD_REQUEST_CODE = 100;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 101;
    private static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = 102;
    HBRecorder hbRecorder;
    ImageButton btnStart,btnStop;
    boolean hasPermissions;
    ContentValues contentValues;
    ContentResolver resolver;
    Uri mUri;

    private static final int RECORD_REQUEST_CODE = 101;
    private static final int STORAGE_REQUEST_CODE = 102;
    private static final int AUDIO_REQUEST_CODE = 103;
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;
    private Button startBtn;


    //Old Define
    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    private TextureView mTextureView;
    private float ef = 0;
    private boolean isstart = false;
    private boolean isRecording = false;
    private float totalTime = 0;
    private float ActualSMV = 0;
    private int totalPCs = 0;
    private long totalTimeElaps = 0;
    private String smv;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgrounHandler;
    private String mCameraId;
    private Size mPreviewSize;
    private Size mVideoSize;
    private MediaRecorder mMediaRecorder;
    private Chronometer mChronometer;
    private int mTotalRotation;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private EditText mEditText;

    private ImageButton mRecordStartButton;
    private ImageButton mRecordStopButton;
    private ImageButton mCountStartButton;
    private ImageButton mCountStopButton;

    private File mVideoFolder;
    private String mVideoFileName;

    private static SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    EditText epfNo_text,opr_text,smv_value,lap_ps,lap_eff,lap_smv,lap_time;

    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effcal_main);
        hbRecorder = new HBRecorder(this, this);
        btnStart=findViewById(R.id.btnStart);
        btnStop=findViewById(R.id.btnStop);
        hbRecorder.setVideoEncoder("H264");

        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        setContentView(R.layout.activity_effcal_main);

        //createVideoFolder();

        mMediaRecorder = new MediaRecorder();
        mChronometer = (Chronometer) findViewById(R.id.chronometer2);
        mTextureView = (TextureView) findViewById(R.id.textureView);
        mRecordStartButton = (ImageButton) findViewById(R.id.btnStart);
        mRecordStopButton = (ImageButton) findViewById(R.id.btnStop);
        epfNo_text=findViewById(R.id.epfNo_text);
        opr_text=findViewById(R.id.opr_text);
        smv_value=findViewById(R.id.editText);
        lap_ps=findViewById(R.id.lap_Pc);
        lap_eff=findViewById(R.id.lap_eff);
        lap_smv=findViewById(R.id.lap_smv);
        lap_time=findViewById(R.id.lap_time);
        mRecordStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //first check if permissions was granted
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE)) {
                        hasPermissions = true;
                    }
                    if (hasPermissions) {
                        startRecordingScreen();
                    }
                } else {
                    Toast.makeText(EffcalMainActivity.this, "Permission Error", Toast.LENGTH_SHORT).show();
                    //showLongToast("This library requires API 21>");
                }
            }
        });
        mRecordStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Record Stopped", Toast.LENGTH_SHORT).show();
                hbRecorder.stopScreenRecording();
            }
        });

        mCountStartButton = (ImageButton) findViewById(R.id.imageButton3);
        mCountStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smv = mEditText.getText().toString();
                try {
                    ActualSMV = Float.parseFloat(smv);
                } catch (Exception e) {
                    // This will catch any exception, because they are all descended from Exception
                    System.out.println("Error " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Enter SMV Value", Toast.LENGTH_SHORT).show();
                    //return null;
                }

                if (isstart) {
                    smv = mEditText.getText().toString();
                    totalPCs = totalPCs + 1;
                    totalTimeElaps = SystemClock.elapsedRealtime() - mChronometer.getBase();
                    totalTime = (((float) totalTimeElaps / (float) 60000));
                    ef = ((ActualSMV * totalPCs) / totalTime) * 100;
                    //Toast.makeText(getApplicationContext(), " Min-" + totalTime + " PCs-" + totalPCs + " SMV-" + smv + " Eff-" + ef, Toast.LENGTH_SHORT).show();

                    String convertTime=String.format("%.2f", totalTime);

                    String lpsPcsStr= String.valueOf(totalPCs);
                    String lpsEffStr= String.valueOf(ef);
                    String lapSmvStr=String.valueOf(smv);
                    String laptimeStr=convertTime;

                    lap_ps.setText("Pcs:"+lpsPcsStr);
                    lap_eff.setText("Eff:"+lpsEffStr);
                    lap_smv.setText("SMV:"+lapSmvStr);
                    lap_time.setText("Time:"+laptimeStr+"(Min)");
                } else {
                    isstart = true;
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.setVisibility(View.VISIBLE);
                    mChronometer.start();

                }
            }
        });

        mCountStopButton = (ImageButton) findViewById(R.id.imageButton4);
        mCountStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isstart) {

                    Toast.makeText(getApplicationContext(), "Total Time-" + totalTime + " Total PCs-" + totalPCs + " Efficiency-" + ef, Toast.LENGTH_LONG).show();
                    mChronometer.stop();
                    totalPCs = 0;
                    totalTime = 0;
                    totalTimeElaps = 0;
                    isstart = false;
                } else {
                    Toast.makeText(getApplicationContext(), "All Values Initialed to Zero", Toast.LENGTH_LONG).show();

                }
                lap_ps.setText("");
                lap_eff.setText("");
                lap_smv.setText("");
                lap_time.setText("");

                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.stop();
            }
        });

        mEditText = (EditText) findViewById(R.id.editText);
    }



    ///////////////////////////HB Recorder Start
    @Override
    public void HBRecorderOnStart() {
        Toast.makeText(this, "Record Started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void HBRecorderOnComplete() {
        Toast.makeText(this, "Record Completed, File Saved", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Update gallery depending on SDK Level
            if (hbRecorder.wasUriSet()) {
                updateGalleryUri();
            }else{
                refreshGalleryFile();
            }
        }
    }

    @Override
    public void HBRecorderOnError(int errorCode, String reason) {
        Toast.makeText(this, errorCode+": "+reason, Toast.LENGTH_SHORT).show();
    }
    private void startRecordingScreen() {
        quickSettings();
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
        startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setOutputPath();
                //Start screen recording
                hbRecorder.startScreenRecording(data, resultCode, this);

            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setOutputPath() {

        String filename = generateFileName2();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver = getContentResolver();
            contentValues = new ContentValues();
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "EffiCal");
            contentValues.put(MediaStore.Video.Media.TITLE, filename);
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            mUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            //FILE NAME SHOULD BE THE SAME
            hbRecorder.setFileName(filename);
            hbRecorder.setOutputUri(mUri);
        }else{
            createFolder();
            hbRecorder.setOutputPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) +"/EffiCal");
        }
    }

    //quickSettings
    private void quickSettings() {
        //hbRecorder.setScreenDimensions(1280, 720);
        hbRecorder.setScreenDimensions(854, 480);
        hbRecorder.setVideoFrameRate(15);
        hbRecorder.recordHDVideo(true);
        hbRecorder.isAudioEnabled(false);
        hbRecorder.setVideoBitrate(7500000);
    }

    //Generate a timestamp to be used as a file name
    private String generateFileName() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate).replace(" ", "");

    }
    public String generateFileName2() {
       String epf_no = epfNo_text.getText().toString();
       String oper = opr_text.getText().toString();
       String smv = smv_value.getText().toString();
       String last_name= epf_no+"_"+oper+"_"+smv;
        return last_name;
    }
    //Check if permissions was granted
    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }
    private void updateGalleryUri(){
        contentValues.clear();
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
        getContentResolver().update(mUri, contentValues, null, null);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void refreshGalleryFile() {
        MediaScannerConnection.scanFile(this,
                new String[]{hbRecorder.getFilePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    //drawable to byte[]
    private byte[] drawable2ByteArray(@DrawableRes int drawableId) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), drawableId);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
    //Create Folder
    private void createFolder() {
        File f1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "EffiCal");
        if (!f1.exists()) {
            if (f1.mkdirs()) {
                Log.i("Folder ", "created");
            }
        }
    }

    ////////////////////////HB Recorder End







    private TextureView.SurfaceTextureListener mSurfaceTextureListner = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            /*Toast.makeText(getApplicationContext(),"TextureView is available",Toast.LENGTH_SHORT).show();

             */
            setupCamera(width, height);
            connectCamera();

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
    };
    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            if (isRecording) {
//                try {
//                    createVideoFileName();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                startRecord();
//                mMediaRecorder.start();
            } else {
                startPreview();
            }
            // Toast.makeText(getApplicationContext(),"Camera connection made",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;

        }
    };


    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size o1, Size o2) {
            return Long.signum((long) o1.getWidth() * o1.getHeight() /
                    (long) o2.getWidth() * o2.getHeight());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        if (mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListner);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application Will not Run without Camera Service", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == STORAGE_REQUEST_CODE || requestCode == AUDIO_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }

    }

    @Override
    protected void onPause() {

        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(cameraCharacteristics.LENS_FACING) ==
                        cameraCharacteristics.LENS_FACING_FRONT) {

                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                int mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if (swapRotation) {

                    rotatedWidth = height;
                    rotatedHeight = width;
                }

                mPreviewSize = chooseOpimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mVideoSize = chooseOpimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                mCameraId = cameraId;
                return;

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgrounHandler);
                } else {
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "Video Requires Access to Camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_RESULT);
                }

            } else {

                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgrounHandler);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }



    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                session.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                        null, mBackgrounHandler);

                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(getApplicationContext(), "Unable to setup camera Preview", Toast.LENGTH_SHORT).show();


                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void startBackgroundThread() {

        mBackgroundHandlerThread = new HandlerThread("EffCal");
        mBackgroundHandlerThread.start();
        mBackgrounHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgrounHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {

        int seneorOrientation = cameraCharacteristics.get(cameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (seneorOrientation + deviceOrientation + 360) % 360;
    }

    private static Size chooseOpimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());

        } else {
            return choices[0];
        }

    }



//        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                isRecording = true;
//                Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_SHORT).show();
//                try {
//                    createVideoFileName();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                Toast.makeText(this, "Permission successfully Granted", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "App needs to save Video to Run", Toast.LENGTH_SHORT).show();
//            }
//        }







//    private void startRecord() {
//
//
//        try {
//            setupMediaRecorder();
//            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
//            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//            Surface previewSurface = new Surface(surfaceTexture);
//            Surface recordSurface = mMediaRecorder.getSurface();
//            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
//            mCaptureRequestBuilder.addTarget(previewSurface);
//            mCaptureRequestBuilder.addTarget(recordSurface);
//
//            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
//                    new CameraCaptureSession.StateCallback() {
//                        @Override
//                        public void onConfigured(@NonNull CameraCaptureSession session) {
//                            try {
//                                session.setRepeatingRequest(
//                                        mCaptureRequestBuilder.build(), null, null
//                                );
//
//                            } catch (CameraAccessException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
//
//                        }
//                    }, null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


//    private void createVideoFolder() {
//        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
//        mVideoFolder = new File(movieFile, "EfficiencyCalculator");
//        if (!mVideoFolder.exists()) {
//            mVideoFolder.mkdirs();
//        }
//
//    }

//    private File createVideoFileName() throws IOException {
//        String timestamp = new SimpleDateFormat("yyyyMMDD_HHmmss").format(new Date());
//        String prepend = "Eff_" + timestamp + "_";
//        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
//        mVideoFileName = videoFile.getAbsolutePath();
//        return videoFile;
//    }

//    private void checkWriteStoragePermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED) {
//                isRecording = true;
//                Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_SHORT).show();
//                try {
//                    createVideoFileName();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                startRecord();
//                mMediaRecorder.start();
//
//            } else {
//
//                if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                    Toast.makeText(this, "App needs to be save Videos", Toast.LENGTH_SHORT).show();
//                }
//                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
//            }
//        } else {
//            isRecording = true;
//            Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_SHORT).show();
//            try {
//                createVideoFileName();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            startRecord();
//            mMediaRecorder.start();
//        }
//
//    }

//    private void setupMediaRecorder() throws IOException {
//        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setOutputFile(mVideoFileName);
//        mMediaRecorder.setVideoEncodingBitRate(1000000);
//        mMediaRecorder.setVideoFrameRate(30);
//        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mMediaRecorder.setOrientationHint(mTotalRotation);
//        mMediaRecorder.prepare();
//
//    }

}
