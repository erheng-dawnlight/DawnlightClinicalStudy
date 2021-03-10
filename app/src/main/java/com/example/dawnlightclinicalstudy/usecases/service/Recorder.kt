package com.example.dawnlightclinicalstudy.usecases.service

import android.util.Log
import java.io.File
import java.io.RandomAccessFile
import java.util.*
import kotlin.collections.ArrayList

/**
 * The recorder saves the data over multiple files. The number of data in a particular file can be
 * configured using the maxFileSeqs property defined in the class. Each file is represented by an
 * instance of FHandler class. Recorder keeps a map of active FHandlers (seq# : Fhandler). When reading
 * or writing to a seq, Recorder looks for active FHandler, and creates one if none FHandler calls
 * onTimeout after 1 min of inactivity.Recorder deletes the object after calling Finish (FHandler
 * closes files).
 * @param prefix The prefix to be added for the data and index file names
 * @param directoryPath The directory where the files has to be located
 */

class Recorder(
        val prefix: String,
        var directoryPath : String

) {
    /***
    Recorder module stores all incoming sensor data to local storage,
    and provides API for retrival from local storage.
     ***/
    var noActivityTimeout = 60L //5 * 60
    var maxFileSeqs = 1000       // Number of seq#s per file


    private var fHandlers = HashMap<Int, FHandler>()

    /**
     * The API saves the data in the local file, and keeps the index in the index file, based
     * on the sequence number provided
     * @param seq The sequence number of the data
     * @param data The byte array format of the data
     */
    fun incoming(seq: Int, data: ByteArray) {
        var fh: FHandler? = null
        val headSeq = seq / maxFileSeqs * maxFileSeqs   // Head of the block of seq#s, to which this seq belongs to
        if (!fHandlers.containsKey(headSeq)) { // this block doesn't have an FHandler yet. Create one
            fh = FHandler(prefix, headSeq, this::onFHTimeout)
            this.fHandlers[headSeq] = fh
            fh?.store(seq, data )
        } else {
            fh = fHandlers[headSeq]
            fh?.store(seq, data )
        }
    }

    /**
     * The API gets the data for the sequence number requested. If no data is present, null is returned.
     * @param seq The sequence number of the data requested
     */
    fun get(seq: Int): ByteArray? {
        var headSeq = seq / maxFileSeqs * maxFileSeqs   // Head of the block of seq#s, to which this seq belongs to
//        if (!fHandlers.containsKey(headSeq))                 // this block doesn't have an FHandler yet. Fail
//            return null
//        return this.fHandlers.get(headSeq)?.get(seq)

        var fh: FHandler? = null
        if (!fHandlers.containsKey(headSeq)) { // this block doesn't have an FHandler yet. Create one
            fh = FHandler(prefix, headSeq, this::onFHTimeout)
            this.fHandlers[headSeq] = fh
        } else {
            fh = fHandlers[headSeq]
        }
        var data = fh!!.get(seq)
        return data

    }

    fun storedSeqList(): ArrayList<Int> {
        var sequenceStored  = ArrayList<Int>()
        var sequenceMissed = ArrayList<Int>()


        var fileList = finder( File(directoryPath).path)

        for(file in fileList)
        {
            var ifile = RandomAccessFile(file.path, "rw")
            var seqStartfromfilename = file.name.split("-")
            var seqStart = (seqStartfromfilename[1]).toInt()
            for(newSeq in 1..maxFileSeqs-1)
            {
                var seekTo = newSeq * 4L
                var iFileLen = ifile?.length()
                ifile?.seek(newSeq * 4L)
                var fpos2 = ifile?.readInt()
                if (fpos2 == 0x7FFFFFFF)           // hole ?
                {
                    sequenceMissed.add(seqStart + newSeq - 1)
                }
                else
                {
                    try
                    {
                        sequenceStored.add(seqStart + newSeq - 1)

                    }
                    catch (e:java.lang.Exception)
                    {
                        e.printStackTrace()
                    }
                }
            }

        }
        return sequenceStored!!
    }

    fun finder(dirName: String): Array<File> {
        val dir = File(dirName)

        return dir.listFiles { dir, filename -> filename.endsWith("-indx") }

    }

    private fun onFHTimeout(fh: FHandler) {
        fh.close()
        fHandlers.remove(fh.seqFrom)
    }

    inner class FHandler(
            prefix: String,
            var seqFrom: Int = 0,
            onTimeout: (FHandler) -> Unit

    ) {
        var ifile: RandomAccessFile? = null
        var dfile: RandomAccessFile? = null
        var fileHandler: FHandler? = null;
        val LOG_TAG = "RECORDER"

        //prefix: typcially, the patchID
        //seqFrom: start seq of block of seqs for this instance
        //onTimeout : Callback on no-activity timeout


        //var seqFrom = 0
        var timer: Timer? = null
        var timerCallBack = object : TimerTask() {
            override fun run() {
                fileHandler?.let { onTimeout(it) }
                Log.e(LOG_TAG, "Timer Hit. Cancelling")
                timer?.cancel()
                timer = null
            }
        }

        init {
            fileHandler = this
            // Data file
            //ccreate folder for sensor if not

            val directory = File(directoryPath)

            // have the object build the directory structure, if needed.
            if(!directory.exists())
            {
                directory.mkdirs()
            }
            else
            {
                Log.e(LOG_TAG, "${directory.path} already exists")
            }

            val outDataFile = File(directory, prefix + '-' +seqFrom.toString())
            var dataFileExists =outDataFile .exists()
            if(dataFileExists)
            {
                Log.e(LOG_TAG, "${outDataFile.path} already exists")
            }
            else
            {
                File(outDataFile.path).createNewFile() // Open for read anywhere + write at end
            }

            dfile = RandomAccessFile(outDataFile.path, "rw")

            val outIndxFile = File(directory, prefix + '-' +seqFrom.toString()  + '-' + "indx")
            var indxExists =outIndxFile .exists()

            if(indxExists)
            {
                Log.e(LOG_TAG, "${outDataFile.path} already exists")
            }
            else
            {
                File(outIndxFile.path).createNewFile() // Open for read anywhere + write at end
            }
            this.ifile = RandomAccessFile(outIndxFile.path, "rw")
            if (indxExists == false)
            {
                //hole = struct.pack('i', 0x7FFFFFFF)
                for (i in 0..maxFileSeqs) {
                    ifile!!.writeInt(0x7FFFFFFF)
                }
            }
        }

        // note the offset of data file and enter it at indexFile[seq]
        // append data to the file (prefixed by length of data)
        //
        fun store(seq: Int, data: ByteArray) {
            try {
                var newSeq = seq
                newSeq -= this.seqFrom - 1
                var dfileLen = dfile!!.length()
                this.dfile?.seek(dfileLen)   //seek to the end of file

                var fpos = this.dfile?.getFilePointer()         // Current writing position of the file
                var dataLen = data.size
                this.dfile?.writeShort(dataLen)
                this.dfile?.write(data)// Append len(u16) + data
                this.ifile?.seek(newSeq * 4L)      // Seek to seq position
                var fposIfile = this.ifile?.getFilePointer()
                this.ifile?.writeInt(fpos!!.toInt())

                Log.e(LOG_TAG, "Seq = $seq")
                resetActivityTimer()
            }catch (e : Exception)
            {
                e.printStackTrace()
            }

        }

        // Get the offset from indexFile[seq], and get data from the position
        // Return data, or None if data not available
        fun get(seq: Int): ByteArray? {
            var data : ByteArray? = null
            try {
                var newSeq = seq
                newSeq -= this.seqFrom - 1

                var seekTo = newSeq * 4L
                var iFileLen = ifile?.length()
                this.ifile?.seek(newSeq * 4L)
                var fpos2 = ifile?.readInt()
                if (fpos2 == 0x7FFFFFFF)           // hole ?
                {
                    this.resetActivityTimer()
                    return data
                }
                var dFileLen = dfile?.length()
                this.dfile?.seek(fpos2!! * 1L)
                val dlen = dfile?.readShort()
                var dataBytes = ByteArray(dlen!!.toInt())
                var i = this.dfile?.read(dataBytes)

                data = (dataBytes)
            }
            catch (e : Exception)
            {
                e.printStackTrace()
            }

            return data

        }

        // Close files
        fun close() {
            Log.d(LOG_TAG, "File close")
            this.dfile?.close()
            this.ifile?.close()
        }

        private fun resetActivityTimer() {
            // Reset activity timer
            try {
                if (timer == null) {
                    Log.e(LOG_TAG, "Timer is null")
                    this.timer = Timer()
                    this.timer?.schedule(timerCallBack, noActivityTimeout * 1000)
                }
            } catch (e: Exception) {

            }
        }

    }

}
