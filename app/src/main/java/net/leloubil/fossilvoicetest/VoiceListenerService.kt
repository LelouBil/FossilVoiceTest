package net.leloubil.fossilvoicetest

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.theeasiestway.opus.Constants.Channels.Companion.mono
import com.theeasiestway.opus.Constants.FrameSize.Companion._960
import com.theeasiestway.opus.Constants.SampleRate.Companion._48000
import com.theeasiestway.opus.Opus
import kotlinx.coroutines.*
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechStreamService
import org.vosk.android.StorageService
import java.lang.Exception
import java.util.LinkedList
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val RECOGNITION_NOTIFICATION_CHANNEL_ID = "RECOGNITION_NOTIFICATION_CHANNEL_ID"

class VoiceListenerService : Service() {
    private val buf: LinkedList<Byte> = LinkedList()
    private lateinit var speechService: SpeechStreamService
    private lateinit var serviceLooper: Looper

    private lateinit var opusDecoder: Opus


    private val speechOutputStream: BlockingByteArrayOutputStream = BlockingByteArrayOutputStream()


    override fun onCreate() {
        val channel = NotificationChannelCompat.Builder(RECOGNITION_NOTIFICATION_CHANNEL_ID,NotificationManager.IMPORTANCE_LOW)
            .setDescription("Listening for voice commands")
            .setName("Fossil Voice Recognition")
            .setVibrationEnabled(false)
            .setShowBadge(false)
            .build()
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, RECOGNITION_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Fossil Voice Recognition")
            .setContentText("Listening for voice commands")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
        Log.d(TAG, "onCreate: service created")
        opusDecoder = Opus()
        opusDecoder.decoderInit(_48000(), mono())

        CoroutineScope(Dispatchers.IO).launch {
                speechService = SpeechStreamService(
                    Recognizer(unpackModel(), 48000f),
                    speechOutputStream,
                    48000f
                )
                speechService.start(RecogListener())
            }

        Log.d(TAG, "runRecognition: speech service started")
    }


    private inner class RecogListener : org.vosk.android.RecognitionListener {
        private val builder = StringBuilder()
        private var partial : String? = null
        override fun onPartialResult(hypothesis: String) {
            Log.d(TAG, "onPartialResult: $hypothesis")
            partial = JSONObject(hypothesis).getString("partial")
        }

        override fun onResult(hypothesis: String) {
            Log.d(TAG, "onResult: $hypothesis")
            builder.append(JSONObject(hypothesis).getString("text")).append(" ")
            partial = null
//            builder.append(JSONObject(hypothesis).getString("text")).append(" ")
        }

        override fun onFinalResult(hypothesis: String) {
            val text = JSONObject(hypothesis).getString("text")
            builder.append(partial)
//            Log.d(TAG, "onFinalResult: $text")
            Log.d(TAG, "onFinalResult builder: $builder")
            sendBroadcast(Intent("net.leloubil.fossilvoicetest.VOICE").putExtra("TEXT", text))
            builder.clear()
            stopSelf()
        }

        override fun onError(exception: Exception) {
            throw exception
        }

        override fun onTimeout() {
            throw TimeoutException("Timeout")
        }

    }

    private suspend fun unpackModel() = suspendCancellableCoroutine<Model> { cont ->

        val value = getSharedPreferences("prefs", MODE_PRIVATE)
            .getString("language", "English")
        val model = when (value){
            "English" -> "vosk-model-small-en-us-0.15"
            "French" -> "vosk-small-fr"
            else -> throw IllegalArgumentException("Unknown language $value")
        }
        Log.d(TAG, "runRecognition: unpacking model $model")
        StorageService.unpack(
            applicationContext,
            model,
            "model",
            {
                Log.d(TAG, "runRecognition: model callback")
                cont.resume(it)
            }
        ) {
            Toast.makeText(applicationContext, "Error unpacking the model", Toast.LENGTH_LONG).show()
            cont.resumeWithException(it)
        }

    }

    private inner class VoiceListenerServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == 0) {
                val voiceData = msg.data.getByteArray("VOICE_DATA")
                if (voiceData != null) {
                    val decoded = opusDecoder.decode(voiceData, _960())
                    if (decoded != null) {
                        speechOutputStream.write(decoded)
                        buf.addAll(decoded.toList())
                    } else {
                        Log.d(TAG, "handleVoicePacket: null decoded")
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {


        Log.d(TAG, "onBind: $intent")
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
        }

        return Messenger(VoiceListenerServiceHandler(serviceLooper)).binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        //run cleanup method in 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({ cleanup() }, 2000)
//        cleanup()
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    fun cleanup(){
        speechOutputStream.closeInput()
            speechService.stop()
            serviceLooper.quit()
        Log.d(TAG, "cleanup: closing input")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

}
