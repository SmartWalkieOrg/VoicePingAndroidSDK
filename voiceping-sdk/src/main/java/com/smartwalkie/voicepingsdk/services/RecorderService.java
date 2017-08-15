package com.smartwalkie.voicepingsdk.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.Nullable;

import com.smartwalkie.voicepingsdk.Recorder;
import com.smartwalkie.voicepingsdk.constants.AudioParameters;
import com.smartwalkie.voicepingsdk.events.AudioDataEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

/**
 * Created by kukuhsain on 8/4/17.
 */

public class RecorderService extends IntentService {

    public RecorderService() {
        super("RecorderService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                AudioParameters.SAMPLE_RATE,
                AudioParameters.CHANNEL_CONFIG,
                AudioParameters.AUDIO_FORMAT,
                AudioParameters.RECORD_MIN_BUFFER_SIZE);

        long startRecordingTimestamp;
        try {
            audioRecord.startRecording();
            startRecordingTimestamp = System.currentTimeMillis();
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
            return;
        }

        int numberOfFrames = 0;
        while (Recorder.IS_RECORDING) {
//            Log.d(getClass().getSimpleName(), "isRecording... number of frames: " + numberOfFrames);
            // check if message is too long
            long currentTimestamp = System.currentTimeMillis();
            long distance = currentTimestamp - startRecordingTimestamp;
            if (distance > 60 * 1000 + 5000) {
                break;
            }

            byte[] recordedBytes = new byte[AudioParameters.FRAME_SIZE * 2 * AudioParameters.CHANNEL];
            int numOfFrames = audioRecord.read(recordedBytes, 0, AudioParameters.FRAME_SIZE * 2);
            if (numOfFrames == AudioRecord.ERROR_INVALID_OPERATION) {
                audioRecord.stop();
                audioRecord.release();
                audioManager.stopBluetoothSco();
                stopSelf();
                Recorder.IS_RECORDING = false;
                return;
            }

            numberOfFrames++;
            EventBus.getDefault().post(new AudioDataEvent(Arrays.copyOfRange(recordedBytes, 0, numOfFrames)));
        }

        audioRecord.stop();
        audioRecord.release();
    }
}
