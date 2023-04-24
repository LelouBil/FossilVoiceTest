package net.leloubil.fossilvoicetest

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.*
import java.util.concurrent.LinkedTransferQueue

class VoiceDataReceiverService: Service(){
    private var messenger: Messenger? = null
    private lateinit var serviceLooper: Looper
    private val buffer: Queue<Message> = LinkedList()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected: connected")
            messenger = Messenger(service)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(TAG, "onServiceDisconnected: disconnected")
        }
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate: service created")
        val intent = Intent(this, VoiceListenerService::class.java)
        ContextCompat.startForegroundService(this, intent)
        // bind to the service

        val res = bindService(intent, connection,0)
        if(!res){
            throw Exception("Failed to bind to service")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind: binding")
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
        }

        return Messenger(object : Handler(Looper.getMainLooper()){
            override fun handleMessage(msg: Message) {
                buffer.add(Message().apply { copyFrom(msg) })
                messenger?.let {
                    while(buffer.isNotEmpty()){
                        it.send(buffer.remove())
                    }
                }
            }
        }).binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind: unbinding")
        if(buffer.isNotEmpty()){
            Log.d(TAG, "onUnbind: buffer not empty")
        }
        unbindService(connection)
        return super.onUnbind(intent)
    }

}
