package com.example.dawnlightclinicalstudy.usecases.service

import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.scheduleAtFixedRate

/**
 * Reorderer module takes data, and re-orders a possibly out-of order stream of data
 * @param   _age        Max age of data in seconds. Incoming data older than this age iwll be rejected.
 * If the data in the pipeline is older than this, it will be delivered immediately without waiting
 * further for filling the gap
 * @param   queueDepth   The buffer size for the data in the queue (age * seq-per-sec)
 * @param   reordered    The function pointer for the reordered output data
 * @param   fakeTime     Not used now
 * @return  void
 */
class Reorderer(
        age: Int,
        val queueDepth: Int,
        val reordered: (Any, Long, Int) -> Unit,
        val faketime: Long = 0
) {
    val LOG_TAG = "REORDER"

    private var _age: Long = age * 1000L // in milli seconds
    private var nextSeq = 0
    private var firstSeq = 0
    private var dlist = Stack<ReorderTuple>()
    private var timer: Timer? = null

    data class ReorderTuple(val data: Any?, val ts: Long, val seq: Int)

    init {
        dlist.setSize(queueDepth)
        for (i in 0 until queueDepth)
            dlist[i] = ReorderTuple(null, 0, 0)
    }

    /**
     * This API is to be called whenever a sensor data is received. The method will check if
     * the data is live and will deliver to the app at once if the expected ordered one is received.
     * If not the expected one, it will wait for the expected one to arrive, until the age of the
     * received one is exceeded
     * @param   sensorData  The data to be re-ordered
     * @param   timeStamp   Real time in milliseconds accuracy
     * @param   seq         Sequence number of the sensorData
     * @param   lock        Not used now
     * @return  void
     */
    @Synchronized
    fun incoming(sensorData: Any, timeStamp: Long, seq: Int, lock: Boolean = true) {

//        println("Incoming $seq")
        var now = System.currentTimeMillis()

        // If too old, ignore
        if (now - timeStamp > this._age) {
            println("Seq too old $seq")
            return
        }
        //Guess the starting point:
        //When this is started while the streaming/retrieval is already in progress,
        // the incoming stream will be mixed with live and buffered data.
        // Consider the starting point as 1st seq with TS between now and now-age
        if (firstSeq == 0) //1st live reception
        {
            this.firstSeq = seq
            this.callback(sensorData, timeStamp, seq)


            val f= dlist.get(0)
            dlist.removeAt(0)
            dlist.add(ReorderTuple(null, 0, 0))

            return
        }

        if (seq < this.firstSeq) //Ignore if newer than newest (can happen only for first few seqs)
            return

        if (seq < nextSeq - dlist.size) { //Ignore if older than queueDepth
            println("Ignoring $seq due to maxQueueDepth reached!!")
            return
        }

        //If very next in line, deliver it
        if (seq == this.nextSeq) {
            callback(sensorData, timeStamp, seq)
            dlist.removeAt(0)
            dlist.add(ReorderTuple(null, 0, 0))

            ageTimer()
            return
        }

        // Candidate for the queing ; Enter in the queue
        ageTimer()
        var pos = seq - this.nextSeq

        if (pos >= dlist.size) {
            dlist.removeAllElements()
            dlist.setSize(queueDepth)
            for (i in 0 until queueDepth)
                dlist[i] = ReorderTuple(null, 0, 0)
            this.callback(sensorData, timeStamp, seq)
//            println("Discarding : " + seq)
            return
        }

        if(pos < 0) {
            println("Pos < 0 for $seq")
            return
        }

        this.dlist[pos] = ReorderTuple(sensorData, timeStamp, seq)

        //Start the aging timer
        if(timer == null) {
            val cAge = this._age - (now - timeStamp)
            timer = Timer()

            timer!!.scheduleAtFixedRate(this._age, this._age) {
                ageTimer()
            }
        }

    }

    private fun callback(sensorData: Any, timeStamp: Long, seq: Int, lock: Boolean = true) {
        //To ensure ageTimer doesn't run and call the callback when
        //a call beack is yet to return from client, use dlistLock for
        //callback
        if (lock) {
            //self.dlistLock.acquire()
        }

        reordered(sensorData, timeStamp, seq)
        nextSeq = seq + 1
        if (lock) {
            //self.dlistLock.release()
        }
    }

    private fun timenow(): Long {
        if (faketime != 0L) {
            return faketime
        }
        return System.currentTimeMillis()
    }

    @Synchronized
    private fun ageTimer() {
        val now = System.currentTimeMillis()
        var index = 0
        var iterationCount = 0
        for (i in 0 until queueDepth) {
            val d = dlist[index]
            if(d.seq != 0) {
                val newAge = now - d.ts
                if(d.seq == nextSeq || newAge > _age){
                    d.data?.let { callback(it, d.ts, d.seq) }
                    for (j in 0..index) {

                        dlist.removeAt(0)
                        dlist.add(ReorderTuple(null, 0, 0))
                    }
                    index = 0
                    continue
                }
            }else {
                index += 1
                iterationCount++
            }
        }

        if(iterationCount >= dlist.size) {
            timer?.cancel()
            timer = null
        }
    }
}

/********************************* UNIT TEST METHODS ************************************/

//1. Test1: Inorder Test TestNum 1
fun inOrderTest1(startSeq: Int, endSeq: Int, age: Int, queueDepth: Int) {
    // Supply in order data from 1 to 1000.
    // The returned data should also be from 1 to 1000

    var expSeq = startSeq

    // Start time will be the real time for first seq. This is required to send the Timestap for other sequences
    val startTS = System.currentTimeMillis() - (400 * startSeq)

    val reorderer = Reorderer(age,
            queueDepth,
            { data, ts, seq ->

                // The output received should b in order, with no holes.
                if(expSeq == seq)
                    println("Received $seq")
                else {
                    println("Error Exp: $expSeq , Received: $seq")
                    System.exit(1)
                }

                expSeq++

                if(expSeq > endSeq) {
                    println("Test successfully completed!")
                    System.exit(1)
                }
            }, 0)

    var pktTS: Long

    val timer = Timer()
    var curSeq = startSeq
    // schedule at a fixed rate
    timer.scheduleAtFixedRate(0, 400) {
        val dataJson = ByteArray(4)
        pktTS = startTS + 400 * curSeq
        reorderer.incoming(dataJson, pktTS, curSeq)
        curSeq++
    }
}

//1. Test1: Inorder Test with large sequence diff in between TestNum 5
fun inOrderTest2(startSeq: Int, endSeq: Int, age: Int, queueDepth: Int) {
    // Supply in order data from 1 to 1000.
    // The returned data should also be from 1 to 1000

    var expSeq = startSeq

    // Start time will be the real time for first seq. This is required to send the Timestap for other sequences
    val startTS = System.currentTimeMillis() - (200 * startSeq)

    val reorderer = Reorderer(age,
            queueDepth,
            { data, ts, seq ->

                // The output received should b in order, with no holes.
                if(expSeq == seq)
                    println("Received $seq")
                else {
                    println("Error Exp: $expSeq , Received: $seq")
                    System.exit(1)
                }

                expSeq++

                if(expSeq > endSeq) {
                    println("Test successfully completed!")
                    System.exit(1)
                }
            }, 0)

    var pktTS: Long

    val timer = Timer()
    var curSeq = startSeq
    // schedule at a fixed rate
    timer.scheduleAtFixedRate(0, 200) {
        val dataJson = ByteArray(4)
        pktTS = startTS + 200 * curSeq
        if(!(curSeq - startSeq in 101 downTo 49)) {
            expSeq = curSeq
            reorderer.incoming(dataJson, pktTS, curSeq)
        } else {

        }
        curSeq++
    }
}

//Test Num 2
fun outOfOrderTest(startSeq: Int, endSeq: Int, expiryAge: Int, queueDepth: Int) {
    val expiredList = ArrayList<Int>()
    val finalList = ArrayList<Int>()
    var rxList = ArrayList<Int>()
    var startSeq1 = startSeq

    // Send every 50 sequences shuffled. First 4 of the 50 will be in order.
    for(i in 1..(endSeq-startSeq)/queueDepth){
        finalList.add(startSeq1)
        finalList.add(startSeq1+1)
        finalList.add(startSeq1+2)
        finalList.add(startSeq1+3)

        val randomList: ArrayList<Int> = (startSeq1+4..startSeq1+queueDepth-1).shuffled() as ArrayList<Int>
        startSeq1=startSeq1+queueDepth

        finalList.addAll(randomList)

    }
    // The remaining sequences are also shuffled and added
    val secondlist: ArrayList<Int> = (startSeq1..endSeq).shuffled() as ArrayList<Int>
    finalList.addAll(secondlist)

    // Start time of first sequence,ie Seq = 1
    val startTS = System.currentTimeMillis() - 200*startSeq

    var prevSeq = startSeq - 1
    val reorderer = Reorderer(expiryAge,
            queueDepth,
            { data, ts, seq ->
                println("Received: $seq")

                // Sequences should be in order. Sequences can be missed.
                if(prevSeq > seq) {
                    println("Error occurred: $seq prev: $prevSeq")
                    System.exit(1)
                }

                // Checks if the sequence is present in Expired. If yes, Test fail.
                if(expiredList.contains(seq)) {
                    println("Test Failed. Received Expired Seq: $seq")
                    System.exit(1)
                }
                prevSeq = seq

                if(rxList.contains(seq))
                    rxList.remove(seq)


                if(rxList.equals(expiredList)) {
                    println("Test successfully completed! Expired list equal")
                    println("Expired List $expiredList")
                    System.exit(1)
                }

            }, 0)

    // Trigger the incoming() every 400ms
    var pktTS: Long

    rxList = finalList.clone() as ArrayList<Int>

    println("Data to send: $finalList")
    val timer = Timer()
    // schedule at a fixed rate
    timer.scheduleAtFixedRate(0, 200) {
        if(finalList.size > 0) {
            val dataJson = ByteArray(4)
            pktTS = startTS + finalList.get(0) * 200
            reorderer.incoming(dataJson, pktTS, finalList.get(0))

            // Need to check the time of sending, to add to expired list. The sequences in this expiredList
            // should not be received.
            if(System.currentTimeMillis() - pktTS > expiryAge * 1000) {
                expiredList.add(finalList.get(0))
            }
            finalList.removeAt(0)

        } else {
            println("Completed Sending")
            println("Expired : $expiredList")
            println("Rx List: $rxList")
            timer.cancel()
        }
    }
}

// Out of order with skipping a range of sequences in between Test Num 6
fun outOfOrderTest2(startSeq: Int, endSeq: Int, expiryAge: Int, queueDepth: Int) {
    val expiredList = ArrayList<Int>()
    val finalList = ArrayList<Int>()
    var rxList = ArrayList<Int>()
    var startSeq1 = startSeq

    // Send every 50 sequences shuffled. First 4 of the 50 will be in order.
    for(i in 1..(endSeq-startSeq)/queueDepth){
        finalList.add(startSeq1)
        finalList.add(startSeq1+1)
        finalList.add(startSeq1+2)
        finalList.add(startSeq1+3)

        val randomList: ArrayList<Int> = (startSeq1+4..startSeq1+queueDepth-1).shuffled() as ArrayList<Int>
        startSeq1=startSeq1+queueDepth

        finalList.addAll(randomList)

    }
    // The remaining sequences are also shuffled and added
    val secondlist: ArrayList<Int> = (startSeq1..endSeq).shuffled() as ArrayList<Int>
    finalList.addAll(secondlist)

    // Start time of first sequence,ie Seq = 1
    val startTS = System.currentTimeMillis() - 200*startSeq

    var prevSeq = startSeq - 1
    val reorderer = Reorderer(expiryAge,
            queueDepth,
            { data, ts, seq ->
                println("Received: $seq")

                // Sequences should be in order. Sequences can be missed.
                if(prevSeq > seq) {
                    println("Error occurred: $seq prev: $prevSeq")
                    System.exit(1)
                }

                // Checks if the sequence is present in Expired. If yes, Test fail.
                if(expiredList.contains(seq)) {
                    println("Test Failed. Received Expired Seq: $seq")
                    System.exit(1)
                }
                prevSeq = seq

                if(rxList.contains(seq))
                    rxList.remove(seq)


                if(rxList.equals(expiredList)) {
                    println("Test successfully completed! Expired list equal")
                    println("Expired List $expiredList")
                    System.exit(1)
                }

            }, 0)

    // Trigger the incoming() every 400ms
    var pktTS: Long

    rxList = finalList.clone() as ArrayList<Int>

    println("Data to send: $finalList")
    val timer = Timer()
    var cc = 0
    // schedule at a fixed rate
    timer.scheduleAtFixedRate(0, 200) {
        if(finalList.size > 0) {
            val dataJson = ByteArray(4)

            if(cc >= 100 && cc <= 150){
                cc++
                finalList.removeAt(0)

                return@scheduleAtFixedRate
            }

            cc++
            if(cc > 150)
                cc = 0

            pktTS = startTS + finalList.get(0) * 200
            reorderer.incoming(dataJson, pktTS, finalList.get(0))

            // Need to check the time of sending, to add to expired list. The sequences in this expiredList
            // should not be received.
            if(System.currentTimeMillis() - pktTS > expiryAge * 1000) {
                expiredList.add(finalList.get(0))
            }
            finalList.removeAt(0)

        } else {
            println("Completed Sending")
            println("Expired : $expiredList")
            println("Rx List: $rxList")
            timer.cancel()
        }
    }
}

//Test3: Buggy. With this random seq list, there was an issue of out of order sequence came.
fun buggyTest(age: Int, queueDepth: Int) {
//    val randomList = arrayListOf(1, 24, 18, 9, 33, 45, 5, 21, 29, 2, 50, 12, 28, 48, 3, 6, 46, 26, 31, 15, 22, 10, 32, 13, 42, 41, 30, 23, 34, 16, 39, 17, 40, 4, 8, 37, 36, 14, 27, 47, 19, 7, 44, 35, 20, 43, 25, 11, 38, 49)
    val randomList = arrayListOf(1, 59, 87, 8, 70, 99, 88, 11, 53, 9, 92, 100, 96, 84, 32, 65, 3, 4, 56, 94, 48, 7, 64, 58, 10, 44, 52, 89, 63, 38, 97, 60, 57, 81, 51, 45, 23, 61, 13, 66, 5, 2, 24, 90, 34, 95, 77, 39, 37, 49, 18, 46, 76, 19, 27, 35, 6, 71, 30, 83, 28, 31, 12, 79, 67, 75, 21, 14, 74, 42, 22, 78, 41, 26, 47, 16, 29, 33, 85, 55, 91, 62, 15, 80, 73, 72, 36, 98, 40, 54, 43, 68, 25, 86, 69, 82, 20, 50, 17, 93)
    val timer = Timer()
    val startTS = System.currentTimeMillis()
    var pktTS = startTS
    var prevSeq = 0
    println("Data to send: $randomList")

    val reorderer = Reorderer(age,
            queueDepth,
            { data, ts, seq ->
                println("Received: $seq")

                if(prevSeq > seq) {
                    println("Error occurred: $seq prev: $prevSeq")
                    System.exit(1)
                }
                prevSeq = seq

                if(seq == 100) {
                    println("Test successfully completed!")
                    System.exit(1)
                }

            }, 0)


    // schedule at a fixed rate
    timer.scheduleAtFixedRate(0, 400) {
        if(randomList.size > 0) {
            val seq = randomList.get(0)
            val dataJson = ByteArray(4)
            pktTS = startTS + seq * 400
            randomList.removeAt(0)
            reorderer.incoming(dataJson, pktTS, seq)
        }
        else {
            println("Completed Sending")
            timer.cancel()
        }
    }
}

//Test4: History seq test, along with live data ::: Test Num 4
fun hugePacketDiffTest(startSeq: Int, endSeq: Int, age: Int, queueDepth: Int) {
    val randomList: ArrayList<Int> = (startSeq+1..endSeq).shuffled() as ArrayList<Int>
    val startTS = System.currentTimeMillis() - 400 * startSeq

    var prevSeq = startSeq - 1
    val reorderer = Reorderer(age,
            queueDepth,
            { data, ts, seq ->
                println("Received: $seq")

                if(prevSeq > seq) {
                    println("Error occurred: $seq prev: $prevSeq")
                    System.exit(1)
                }
                prevSeq = seq

                if(seq == endSeq) {
                    println("Test successfully completed!")
                    System.exit(1)
                }

            }, 0)

    // Trigger the incoming() every 400ms
    var pktTS: Long

    // Send first packet as such, because the Reorderer module will assume first packet as its reference.

    println("Data to send: $randomList")
    val dataJson = ByteArray(4)
    pktTS = startTS + startSeq * 400
    reorderer.incoming(dataJson, pktTS, startSeq)

    val timer = Timer()
    // schedule at a fixed rate
    timer.scheduleAtFixedRate(0, 400) {
        if(randomList.size > 0) {
            val dataJson = ByteArray(4)
            pktTS = startTS + randomList.get(0) * 400
            reorderer.incoming(dataJson, pktTS, randomList.get(0))
            randomList.removeAt(0)

        } else {
            println("Completed Sending")
            timer.cancel()
        }
    }

    val timer1 = Timer()
    var historySeq = startSeq - 1
    // schedule at a fixed rate
    timer1.scheduleAtFixedRate(100, 100) {
        val dataJson = ByteArray(4)
        pktTS = startTS + historySeq * 400
        reorderer.incoming(dataJson, pktTS, historySeq)
        historySeq--

        if(historySeq == 1)
            timer1.cancel()
    }
}

fun main(args: Array<String>) {

    try {
        val testNum = args[0]
        val startSeq = args[1]
        val endSeq = args[2]
        val age = args[3]
        val queueDepth = args[4]

        if (testNum.toInt() == 1) {// in order test
            inOrderTest1(startSeq.toInt(), endSeq.toInt(), age.toInt(), queueDepth.toInt())
        } else if (testNum.toInt() == 2) { // out of order test.
            outOfOrderTest(startSeq.toInt(), endSeq.toInt(), age.toInt(), queueDepth.toInt())
        } else if(testNum.toInt() == 3) { // Buggy test.
            buggyTest(age.toInt(), queueDepth.toInt())
        } else if(testNum.toInt() == 4) { // huge packet diff
            // This require live data of range more than 10000
            if(startSeq.toInt() < 10000) {
                println("Supply startSeq > 10000")
                System.exit(1)
            }

            hugePacketDiffTest(startSeq.toInt(), endSeq.toInt(), age.toInt(), queueDepth.toInt())
        }else if (testNum.toInt() == 5) { // out of order test.
            inOrderTest2(startSeq.toInt(), endSeq.toInt(), age.toInt(), queueDepth.toInt())
        }else if (testNum.toInt() == 6) { // out of order test.
            outOfOrderTest2(startSeq.toInt(), endSeq.toInt(), age.toInt(), queueDepth.toInt())
        }

    }catch (e: Exception){
//        println(e.localizedMessage)
        println("Supply testNum(1/2/3/4), startSeq, endSeq, age, queueDepth")
        System.exit(1)
    }
}