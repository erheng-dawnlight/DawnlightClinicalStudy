package com.example.dawnlightclinicalstudy.presentation.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object PermissionUtil {
    fun checkPermissionAndRequest(activity: Activity) {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (!hasPermissions(activity, *permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, 1)
        }
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        permissions.forEach {
            if (context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}
