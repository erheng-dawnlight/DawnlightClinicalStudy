package com.example.dawnlightclinicalstudy.usecases.service;

import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FileWriteThread internal constructor(

        _filePath: String,
        _ext: String
) : Thread() {

    var strList: ArrayList<String> = ArrayList()
    var filePath = _filePath
    var fileExt = _ext
    var finalPath = ""

    override fun run() {
        try
        {
            while (true) {

                if (finalPath.isEmpty()) {
                    finalPath = filePath + getDateTime(System.currentTimeMillis()) + "." + fileExt
                }

                var file = File(finalPath)
                file.parentFile?.mkdirs()
                file.createNewFile()

                val sizeInBytes = file.length();
                val sizeInMb = sizeInBytes / (1024 * 1024 * 1024)

                if (sizeInMb > 3) {
                    finalPath = filePath + getDateTime(System.currentTimeMillis()) + "." + fileExt
                    file = File(finalPath)
                    file.parentFile?.mkdirs()
                    file.createNewFile()
                }
                if (strList.isNotEmpty()) {
                    FileOutputStream(file, true).bufferedWriter().use {
                        it.appendLine(strList[0])
                    }
                    strList.removeAt(0)
                }


            }
        }
        catch ( e : Exception)
        {
            e.printStackTrace()
        }
    }

    fun writeLog(str: String) {
        strList.add(str)
    }

    private fun getDateTime(s: Long): String? {
        try {
            val sdf = SimpleDateFormat("MM_dd_yyyy_HH_mm_ss")
            val netDate = Date(s)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }
}