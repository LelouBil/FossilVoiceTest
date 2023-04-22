package net.leloubil.fossilvoicetest;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import androidx.annotation.Nullable;
import com.theeasiestway.opus.Constants;
import com.theeasiestway.opus.Opus;
import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoiceService extends Service {
    public static final String TAG = "VoiceService";
    Opus opus;
    private Messenger messenger;

    private AudioTrack audioTrack;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        opus = new Opus();
        opus.decoderInit(Constants.SampleRate.Companion._48000(), Constants.Channels.Companion.mono());

        audioTrack = new AudioTrack(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(), new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(48000)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(), AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT), AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
        audioTrack.setVolume(AudioTrack.getMaxVolume());
        audioTrack.play();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: " + intent);
        messenger = new Messenger(new VoiceHandler(this));
        return messenger.getBinder();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        opus.decoderRelease();
        audioTrack.stop();
        audioTrack.release();

    }

    public void handleVoicePacket(byte[] voiceData) {
        byte[] decoded = opus.decode(voiceData, Constants.FrameSize.Companion._960());
        Byte[] array = new Byte[decoded.length];

        audioTrack.write(decoded, 0, decoded.length);
    }
}
