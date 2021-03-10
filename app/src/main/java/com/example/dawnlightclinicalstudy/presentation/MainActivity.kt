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
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dawnlightclinicalstudy.presentation.navigation.Screen
import com.example.dawnlightclinicalstudy.presentation.ui.subject_input.SubjectInputScreen
import com.example.dawnlightclinicalstudy.presentation.ui.subject_input.SubjectInputViewModel
import com.example.dawnlightclinicalstudy.presentation.utils.PermissionUtil
import com.example.dawnlightclinicalstudy.usecases.service.DataReceiverService
import com.example.dawnlightclinicalstudy.usecases.service.ServiceListener
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalMaterialApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var mDataReceiverService: DataReceiverService? = null
    private val dataReceiverServiceListener = createServiceListener()
    private val mServiceConnection = createServiceConnection()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Screen.SubjectId.route) {
                composable(route = Screen.SubjectId.route) { backStackEntry ->
                    val factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
                    val viewModel: SubjectInputViewModel = viewModel("SubjectIdViewModel", factory)
                    SubjectInputScreen(viewModel = viewModel, context = LocalContext.current)
                }
            }
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().permitAll().build())

        PermissionUtil.checkPermissionAndRequest(this)
        startHotspotService()
    }

    private fun startHotspotService() {
        try {
            val serviceIntent = Intent(applicationContext, DataReceiverService::class.java)
            serviceIntent.putExtra(packageName, "start")
            Log.e(LOG_TAG, "Start service")
            applicationContext.startService(serviceIntent)
            applicationContext.bindService(
                serviceIntent,
                mServiceConnection,
                BIND_AUTO_CREATE,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(LOG_TAG, "Ex" + e.message)
        }
    }

    fun stopService() {
        val serviceIntent = Intent(applicationContext, DataReceiverService::class.java)
        applicationContext.unbindService(mServiceConnection)
        applicationContext.stopService(serviceIntent)
        mDataReceiverService?.stopForeground(true)
        mDataReceiverService = null
    }

    private fun createServiceListener(): ServiceListener {
        return ServiceListener { event, json ->

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