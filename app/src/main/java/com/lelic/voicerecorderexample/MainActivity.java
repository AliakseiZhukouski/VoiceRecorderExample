package com.lelic.voicerecorderexample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST = 0x33;

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button recordingButton;
    private Button stopButton;
    private Button playButton;

    private File voiceStoragePath;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;

    static final String AB = "abcdefghijklmnopqrstuvwxyz";
    static Random rnd = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hasSDCard();

        File audioVoice = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC) + File.separator + "voices");
        if (!audioVoice.exists()) {
            audioVoice.mkdir();
        }
        voiceStoragePath = new File(audioVoice, generateVoiceFilename(6) + ".3gp");
        System.out.println("Audio path : " + voiceStoragePath);

        recordingButton = (Button) findViewById(R.id.recording_button);
        stopButton = (Button) findViewById(R.id.stop_button);
        playButton = (Button) findViewById(R.id.play_button);

        stopButton.setEnabled(false);
        playButton.setEnabled(false);


        checkPermission();

        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaRecorder == null) {
                    initializeMediaRecord();
                }
                startAudioRecording();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAudioRecording();
            }
        });


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playLastStoredAudioMusic();
                mediaPlayerPlaying();
            }
        });
    }

    private void checkPermission() {

        Log.d(TAG, "checkPermission");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

             Log.d(TAG, "checkPermission case 1");

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else {
            Log.d(TAG, "checkPermission case 2");

            initializeMediaRecord();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "onRequestPermissionsResult MY_PERMISSIONS_REQUEST  = PERMISSION_GRANTED");
                    initializeMediaRecord();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String generateVoiceFilename(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    private void startAudioRecording() {
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recordingButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void stopAudioRecording(){
        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        stopButton.setEnabled(false);
        playButton.setEnabled(true);
    }


    private void playLastStoredAudioMusic() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, Uri.fromFile(voiceStoragePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
        recordingButton.setEnabled(true);
        playButton.setEnabled(false);
    }

    private void stopAudioPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void hasSDCard() {
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (isSDPresent) {
            System.out.println("There is SDCard");
        } else {
            System.out.println("There is no SDCard");
        }
    }

    private void mediaPlayerPlaying() {
        if (!mediaPlayer.isPlaying()) {
            stopAudioPlay();
        }
    }

    private void initializeMediaRecord() {
        Log.d(TAG, "initializeMediaRecord");

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        if (Build.VERSION.SDK_INT >= 26) {
            mediaRecorder.setOutputFile(voiceStoragePath);
        }
        else {
            mediaRecorder.setOutputFile(voiceStoragePath.getPath());

        }
    }

}