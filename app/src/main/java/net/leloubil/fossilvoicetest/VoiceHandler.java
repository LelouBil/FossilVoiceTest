package net.leloubil.fossilvoicetest;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.annotation.NonNull;

public class VoiceHandler extends Handler {
    private static final String TAG = "VoiceHandler";
    private final VoiceService voiceService;


    public VoiceHandler(VoiceService voiceService) {
        this.voiceService = voiceService;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (msg.what == 0) {
            byte[] voiceData = msg.getData().getByteArray("VOICE_DATA");

            if (voiceData != null) {
                // Do something with voiceData
                Log.d(TAG, "handleMessage: " + voiceData.length);
                voiceService.handleVoicePacket(voiceData);
            }
        }
    }
}
