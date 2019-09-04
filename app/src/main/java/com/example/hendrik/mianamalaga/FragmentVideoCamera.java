package com.example.hendrik.mianamalaga;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;


// TODO - The size of the surfaceView should be adapted to the camera aspect ratio
// TODO - Buttons on the screen to change the perspective on quality would be nice
// TODO - Limit size of video and duration
// TODO - There must be a cancel button to return to calling activity !!

public class FragmentVideoCamera extends Fragment
        implements View.OnClickListener, FragmentCompat.OnRequestPermissionsResultCallback {

    private MediaRecorder mMediaRecorder;
    private CamcorderProfile mCamcorderProfile;
    private SurfaceView mCameraSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private Activity mActivity;
    private boolean isPreviewRunning;
    private boolean isRecording;
    private File mOutputFile;
    private ImageView mCamButtonView;
    private ImageView mRearCameraButtonView;
    private ImageView mTakePictureButtonView;
    private int mCameraId;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private int mType;


    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    public static FragmentVideoCamera newInstance(String fullFilePathString){
        FragmentVideoCamera fragmentVideoCamera = new FragmentVideoCamera();
        Bundle args = new Bundle();
        //args.putInt("Lesson", Lesson);
        args.putString("FullFilePathString", fullFilePathString);
        //args.putString("TopicPath", topicPath);
        fragmentVideoCamera.setArguments(args);
        return fragmentVideoCamera;
    }
/*
    public static FragmentVideoCamera newInstance(int Lesson, String topicPath, int mediaType, int position){
        FragmentVideoCamera fragmentVideoCamera = new FragmentVideoCamera();
        Bundle args = new Bundle();
        args.putInt("Lesson", Lesson);
        args.putInt("MediaType", mediaType);
        args.putString("TopicPath", topicPath);
        args.putInt("Position", position);
        fragmentVideoCamera.setArguments(args);
        return fragmentVideoCamera;
    }
*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null){
  //          mLesson = getArguments().getInt("Lesson");
  //          mOutputFilePathString = getArguments().getString("TopicPath");
            String filePathString = getArguments().getString("FullFilePathString");
            if( IOUtils.isImageFile( filePathString ) ){
                mType = MEDIA_TYPE_IMAGE;
            } else if ( IOUtils.isVideoFile( filePathString ) ){
                mType = MEDIA_TYPE_VIDEO;
            } else {
                finishCameraSession();
            }
            mOutputFile = createOutputFile(filePathString);
       //     mPosition = getArguments().getInt("Position");
        }

        mActivity = getActivity();
        mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mCameraSurfaceView = view.findViewById(R.id.conversation_surfaceView);
        mCamButtonView = view.findViewById(R.id.camera_fragment_camera_button);
        mCamButtonView.setOnClickListener(this);
        mRearCameraButtonView = view.findViewById(R.id.camera_fragment_camera_rear);
        mRearCameraButtonView.setOnClickListener(this);
        mTakePictureButtonView = view.findViewById(R.id.camera_fragment_picture_button);
        mTakePictureButtonView.setOnClickListener(this);

        if(mType == MEDIA_TYPE_IMAGE){
            mTakePictureButtonView.setVisibility(View.VISIBLE);
            mRearCameraButtonView.setVisibility(View.VISIBLE);
            mCamButtonView.setVisibility(View.INVISIBLE);
        } else if ( mType == MEDIA_TYPE_VIDEO){
            mTakePictureButtonView.setVisibility(View.INVISIBLE);
            mRearCameraButtonView.setVisibility(View.VISIBLE);
            mCamButtonView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mCameraSurfaceView != null) {
            prepareCamera();
        } else {
            Log.v(Constants.TAG, "Camera SurfaceView is null! C'ant create preview!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int i, @NonNull String[] strings, @NonNull int[] ints) {

    }

    @Override
    public void onClick(View v) {
        if( v.equals(mCamButtonView)){
            if (isRecording) {
                finishCameraSession();
                ((ActivityConversation)mActivity).finishCamera();
            } else {
                prepareRecorder();
                isRecording = true;
                mMediaRecorder.start();
                mCamButtonView.setImageResource(R.drawable.ic_video_cam_off);
                Toast.makeText(mActivity, "Recording started!", Toast.LENGTH_SHORT).show();
                Log.v(Constants.TAG, "Recording Started");
            }
        } else if (v.equals(mRearCameraButtonView)){
            setRearCamera();
        } else if (v.equals(mTakePictureButtonView)){
            mCamera.takePicture(null, null, mPicture);
        }

    }

    private void setRearCamera() {
        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        if (isPreviewRunning) {
            mCamera.stopPreview();
        }
        //NB: if you don't release the current camera before switching, you app will crash
        mCamera.release();
        mCamera = getCameraInstance(mCameraId);
        mCamcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        //Code snippet for this method from somewhere on android developers, i forget where
        //setCameraDisplayOrientation(CameraActivity.this, currentCameraId, camera);
        try {
            //this step is critical or preview on new camera will no know where to render to
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.setDisplayOrientation(90);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();

    }
/*
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
*/
    private void prepareCamera(){
        mCamcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        mSurfaceHolder = mCameraSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.v(Constants.TAG, "Surface created!");
                if( mCamera == null){
                    mCamera = getCameraInstance(mCameraId);
                }

                try {
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    mCamera.setDisplayOrientation(90);
                    mCamera.startPreview();
                    isPreviewRunning = true;
                }
                catch (IOException e) {
                    Log.e(Constants.TAG,e.getMessage());
                    e.printStackTrace();
                    mActivity.finish();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.v(Constants.TAG, "surfaceChanged");


/*
                Camera.Parameters cameraParameters = mCamera.getParameters();

                List<Camera.Size> sizeList = cameraParameters.getSupportedPreviewSizes();
                Camera.Size optimalSize = getOptimalPreviewSize( sizeList, width, height);
                cameraParameters.setPreviewSize(optimalSize.width, optimalSize.height);
                mCamera.setParameters(cameraParameters);

                        for (Camera.Size size : sizeList){
                            Log.d(Constants.TAG, "Size : " + size.height + "," + size.width + "!!!!!");
                            if (size.height == size.width){
                                cameraParameters.setPreviewSize(size.width, size.height);
                                continue;
                            }
                        }
                        Camera.Size size = cameraParameters.getPreviewSize();
                        Log.d(Constants.TAG, "Chosen size : " + size.height + "," + size.width + "!!!!!");

                // If your preview can change or rotate, take care of those events here.
                // Make sure to stop the preview before resizing or reformatting it.

                if (mSurfaceHolder.getSurface() == null){
                    // preview surface does not exist
                    return;
                }

                // stop preview before making changes
                try {
                    mCamera.stopPreview();
                } catch (Exception e){
                    // ignore: tried to stop a non-existent preview
                }

                // set preview size and make any resize, rotate or
                // reformatting changes here

                // start preview with new settings
                try {
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    mCamera.startPreview();

                } catch (Exception e){
                    Log.d(Constants.TAG, "Error starting camera preview: " + e.getMessage());
                }

*/



                if (!isRecording ) {
                    if (isPreviewRunning){
                        mCamera.stopPreview();
                    }

                    try {
                        mCamera.lock();
                        Camera.Parameters p = mCamera.getParameters();

                        p.setPreviewSize(mCamcorderProfile.videoFrameWidth, mCamcorderProfile.videoFrameHeight);
                        p.setPreviewFrameRate(20);

                        mCamera.setParameters(p);
                        mCamera.setPreviewDisplay(mSurfaceHolder);
                        mCamera.startPreview();
                        isPreviewRunning = true;
                    } catch (IOException e) {
                        Log.e(Constants.TAG,e.getMessage());
                        e.printStackTrace();
                        mActivity.finish();
                    } catch ( RuntimeException exception) {
                        Log.e(Constants.TAG,exception.getMessage());
                        exception.printStackTrace();
                        mActivity.finish();
                    } catch (Exception exception){
                        Log.e(Constants.TAG,exception.getMessage());
                        exception.printStackTrace();
                        mActivity.finish();
                    }
                }

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                finishCameraSession();
            }
        });

        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCameraSurfaceView.setClickable(true);

    }

    private void finishCameraSession(){
        Log.v(Constants.TAG, "surfaceDestroyed");
        if (isRecording) {
            mMediaRecorder.stop();
            isRecording = false;
        }
        if(mMediaRecorder != null){
            mMediaRecorder.release();
        }

        isPreviewRunning = false;
        if(mCamera != null){
            try{
                mCamera.lock();
                mCamera.release();
                mCamera = null;
            } catch (Exception exception){
                Log.e(Constants.TAG,exception.getMessage());
                exception.printStackTrace();
                mActivity.finish();
            }
        }
    }

    private void prepareRecorder() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mCamera.unlock();

        //mOutputFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);

        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mMediaRecorder.setProfile(mCamcorderProfile);
        mMediaRecorder.setOutputFile(mOutputFile.getAbsolutePath());
        mMediaRecorder.setMaxFileSize(5000000);                                                     // Approximately 5 megabytes

        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        if(mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
            mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
        else
            mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Toast.makeText(mActivity, "A problem while recording occurred!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mActivity, "A problem while recording occurred!", Toast.LENGTH_SHORT).show();
        }
    }


    public Camera getCameraInstance(int CameraId){
        Camera camera = null;
        try {                                               // attempt to get a Camera instance
            if (Camera.getNumberOfCameras() > 0){
                camera = Camera.open(CameraId);
            } else {
                camera = Camera.open(0);
            }

        }
        catch (Exception e){
            Log.e(Constants.TAG, "Camera failed to open: " + e.getLocalizedMessage());
            mActivity.finish();
        }
        return camera;                                      // returns null if camera is unavailable
    }


    private Camera.PictureCallback mPicture = (data, camera) -> {

        //File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        File pictureFile = mOutputFile;
        if (pictureFile == null){
            Log.d(Constants.TAG, "Error creating media file, check storage permissions");
            return;
        }

        try {
            Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(mCameraId, info);
            Bitmap bitmap = IOUtils.rotateBitmap(realImage, info.orientation, info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);

            FileOutputStream fos = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, fos);

            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(Constants.TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(Constants.TAG, "Error accessing file: " + e.getMessage());
        }


        finishCameraSession();
        if( mActivity.getComponentName().getClassName().endsWith("ActivityTopicChoice") )
            ((ActivityTopicChoice)mActivity).onPictureTaken();

        if( mActivity.getComponentName().getClassName().endsWith("ActivityConversation") )
            ((ActivityConversation)mActivity).finishCamera();


    };


    private File createOutputFile(String fullPathString){

        if( !IOUtils.isExternalStorageWritable() ){
            Log.e(Constants.TAG, "External Storage is not writable! ");
            finishCameraSession();
        }

        File outputFile = new File(fullPathString);
        File topicDirectory = outputFile.getParentFile();

        if( !topicDirectory.exists() ){
            if( !topicDirectory.mkdir() ){
                Log.e(Constants.TAG, "Failed to create topic directory! ");
                finishCameraSession();
            }
        }

        if ( outputFile.exists() ){
            if( !outputFile.delete() ){
                Log.e(Constants.TAG, "Failed to delete media file: " + outputFile.toString() );
                finishCameraSession();
            }
        }

        return outputFile;
    }
/*
    private File getOutputMediaFile(int type){
        File TopicDirectory = new File( mOutputFilePathString );

        if( !IOUtils.isExternalStorageWritable() ){
            Log.e(Constants.TAG, "External Storage is not writable! ");
            finishCameraSession();
        }

        if( !TopicDirectory.exists() ){
            if( !TopicDirectory.mkdir() ){
                Log.e(Constants.TAG, "Failed to create topic directory! ");
                finishCameraSession();
            }
        }

        File outputFile;

        if( type == MEDIA_TYPE_IMAGE ){
            outputFile = new File(TopicDirectory, Constants.TopicPictureFileName);
        }  else if ( type == MEDIA_TYPE_VIDEO){
            outputFile = new File(TopicDirectory, Constants.VideoName + mLesson + ".mp4");
        } else {
            return null;
        }

        if ( outputFile.exists() ){
            if( !outputFile.delete() ){
                Log.e(Constants.TAG, "Failed to delete audio file: " + outputFile.toString() );
                finishCameraSession();
            }
        }

        return outputFile;

    }
*/



}
