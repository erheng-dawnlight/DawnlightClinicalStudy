package com.example.dawnlightclinicalstudy.presentation

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.State
import androidx.compose.ui.ExperimentalComposeUiApi
import com.example.dawnlightclinicalstudy.presentation.utils.PermissionUtil
import com.example.dawnlightclinicalstudy.usecases.service.DataReceiverService
import com.example.dawnlightclinicalstudy.usecases.service.ServiceListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalMaterialApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var mDataReceiverService: DataReceiverService? = null
    lateinit var dataReceiverServiceListener: ServiceListener
    lateinit var mServiceConnection: ServiceConnection

    private val viewModel: MainActivityViewModel by viewModels()

    @ExperimentalComposeUiApi
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContent {
            Navigation()
            render(viewModel.state)
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().permitAll().build())

        PermissionUtil.checkPermissionAndRequest(this)

        dataReceiverServiceListener = createServiceListener()
        mServiceConnection = createServiceConnection()
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }

    private fun render(state: State<MainActivityViewModel.State>) {
        state.value.selectPatch?.maybeConsume { (json, shouldConfigure) ->
            mDataReceiverService?.select(json)
            if (shouldConfigure) {
                mDataReceiverService?.configure(json)
                mDataReceiverService?.start()
            } else {
                mDataReceiverService?.start()
                mDataReceiverService?.commit(false)
            }
        }

        state.value.startHotspotService?.maybeConsume {
            startHotspotService()
        }

        state.value.errorText?.maybeConsume {
            Toast.makeText(this, it.getText(this), Toast.LENGTH_LONG).show()
        }
    }

    private fun startHotspotService() {
        val serviceIntent = Intent(this, DataReceiverService::class.java)
        startService(serviceIntent)
        bindService(
            serviceIntent,
            mServiceConnection,
            BIND_AUTO_CREATE,
        )
    }

    fun stopService() {
        mDataReceiverService?.stopAcquisition()
        mDataReceiverService?.stopForeground(true)
        mDataReceiverService?.stopSelf()

        val serviceIntent = Intent(this, DataReceiverService::class.java)
        unbindService(mServiceConnection)
        stopService(serviceIntent)
        mDataReceiverService = null
    }

    private fun createServiceListener(): ServiceListener {
        return ServiceListener { event, json ->
            runOnUiThread {
                viewModel.lifeSignalDataReceived(event, json)
            }
        }
    }

    private fun createServiceConnection(): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName) {
                Log.e(LOG_TAG, "onServiceDisconnected Called !!!!!")
            }

            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                try {
                    val myBinder = service as DataReceiverService.DataReceiverServiceBinder
                    myBinder.service?.apply {
                        setRecieverServiceListener(dataReceiverServiceListener)
                        startService()
                        startInterface()
                        mDataReceiverService = this
                    }

                    Log.e(LOG_TAG, "onServiceConnected Called !!!!!")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        val LOG_TAG: String = MainActivity::class.java.name
    }
}