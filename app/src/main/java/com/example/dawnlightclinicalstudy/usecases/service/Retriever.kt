package com.example.dawnlightclinicalstudy.usecases.service

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.concurrent.scheduleAtFixedRate


/**
 * This module keeps a list of sequence numbers received from the sensor and issues requests
 * to the sensor to re-transmit lost data, effectively ensuring that the LSPatch library's onData
 * interface will recieve all data include lost sequence. This module doesn't store any data. It
 * calls onFinal once all seq#s in the range are retrieved
 * @param   seqSt       Starting sequence number
 * @param   latestFirst Request retriever order - latest first or earliest first. Currently, the
 * retriever is working only for latest first retrieval
 * @param   requester    The function pointer for generatig the requests for missing sequences
 * @param   onFinal     After all the missed sequences are retrieved, this function pointer is called.
 * @return  void
 */

class Retriever(
        val seqSt: Int,
        val latestFirst: Boolean,
        val requester: (ArrayList<Int>) -> Unit,
        val onFinal: () -> Unit
) {

    private var seqs: IntArray
    private var pendingReqs = ConcurrentHashMap<Int, Long>()
    var seqEnd = 72 * 60 * 60 * 1000 / 400
    private var largestSeq = 0
    private var lastOutstandingTime = 0L

    init {
        seqs = IntArray(seqEnd - seqSt + 1) {0}
    }

    companion object {
        // Following shall be from LSInterface
        private val MAXSEQPERREQ = 100
        val MAXREREQPERIOD = 1         //secs
        private val MAXOUTSTANDING = 100          //Typically, request bufferlength of f/w
    }


    /**
     * This API receives the sequence number retrieved. The method will mark the corresponding data as
     * retrieved.
     * @param seq Incoming data sequence number
     * @return void
     */
    @Synchronized
    fun incoming(seq: Int) {
        if (seq > seqEnd) {

            runRequestCycle()
            return
        }

        if(seq < seqSt)
            return

        seqs[seq - seqSt] = 1  //Mark as arrived (TODO make it bit operation)

        if (seq > this.largestSeq)
            this.largestSeq = seq

        if (this.pendingReqs.containsKey(seq))
            pendingReqs.remove(seq)    // Delete entry from pending request, on arrival of seq

        // Limit sending
        if (this.pendingReqs.size >= MAXOUTSTANDING) {
            if(lastOutstandingTime == 0L)
                lastOutstandingTime = System.currentTimeMillis()
            if((System.currentTimeMillis() - lastOutstandingTime)/1000 <= MAXREREQPERIOD)
                return
        }
        lastOutstandingTime = 0L
        runRequestCycle()
    }

    /**
     * The method ends the retriever prematurely. Any data received larger than the finalized
     * sequence number will be discarded. Once all the missed sequence numbers are received,
     * onFinal callback is called.
     * @param lastSeq The sequence number for ending the retreival
     */
    @Synchronized
    fun finalise(lastSeq: Int) {
        try {
            if (lastSeq > seqSt) {
                this.seqEnd = lastSeq
                this.largestSeq = lastSeq
                this.seqs = seqs.copyOfRange(0, seqEnd - seqSt + 1)
            }
        } catch (e: Exception) {
            println("Exception: end = $seqEnd start = $seqSt  seqsCount = ${seqs.size}")
            e.printStackTrace()
        }
    }

    /*****
    # Look for slots in seqs list from retrievalStart to largestSeq
    #   which are either empty or
     ****/
    @Synchronized
    private fun runRequestCycle() {
        val retrievalList = ArrayList<Int>()
        val now = System.currentTimeMillis()/1000

        try {
            for (indx in this.largestSeq downTo this.seqSt) {
                val entry = this.seqs[indx - this.seqSt]

                if (entry != 0) { // Arrived
                    continue
                }

                if (this.pendingReqs.containsKey(indx) && // Already requested
                        (now - this.pendingReqs.get(indx)!!) <= MAXREREQPERIOD
                )   // Request is new
                    continue

                this.pendingReqs[indx] = now
                retrievalList.add(indx)

                if (retrievalList.size >= MAXSEQPERREQ)  //Limit the size of request list
                    break

            }

            val nReqs = (retrievalList).size
            if (nReqs > 0) {
                retrievalList.sort()
                requester.invoke(retrievalList)
            } else {
                //If no repeat request, and seqEnd is already recieved, call onFinal
                if (this.largestSeq == this.seqEnd)
                    onFinal.invoke()
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }

    }

    fun getLatestSeq(): Int {
        return largestSeq
    }
}

// Test1: In order live data and replying with requests at once received.
fun inorderRetrieverTest(start: Int, end: Int) {

    val rxRequestList = ConcurrentHashMap<Int, Long>()
    var isFinal = false
    val retriever = Retriever(1, true, {
        reqList ->

        //1. Ensure that the requests are not > 100
        val newList = reqList.filter { it > start }
        if(!newList.isEmpty()) {
            println("Test failed. $reqList")
            System.exit(1)
        }
        val now = System.currentTimeMillis()
        for(req in reqList) {

            if(rxRequestList.containsKey(req)) {
                // check if the retry time is reached.
                if(now - rxRequestList[req]!! < 3000) {
                    println("Test failed. Requested Repeat < 3sec for $req")
                    System.exit(1)
                } else {
                    rxRequestList[req] = now
                }
            } else {
                rxRequestList[req] = now
            }
        }
        println("Received Req $reqList")
    }, {
        println("Final called!!!")
        isFinal = true
    })


    retriever.seqEnd = end

    val reqTimer = Timer()
    // schedule at a fixed rate
    reqTimer.scheduleAtFixedRate(100, 50) {

        for(req in rxRequestList) {
            retriever.incoming(req.key)
        }
        rxRequestList.clear()

        if (isFinal) {
            reqTimer.cancel()
            println("Test Completed")
            System.exit(1)
        }
    }

    val timer = Timer()
    var seq1 = start
    // schedule at a fixed rate
    timer.scheduleAtFixedRate(200, 400) {

        retriever.incoming(seq1)
        seq1++

        if(seq1 > end){
            timer.cancel()
            println("Completed sending")
        }
    }
}

fun missingSequenceTest(start: Int, end: Int, liveSkipCount: Int, reqSkipCount: Int, finalizeAbruptly: Boolean) {
    val rxRequestList = ConcurrentHashMap<Int, Long>()
    val sendList = Collections.synchronizedList(ArrayList<Int>())
    var isFinal = false
    val retriever = Retriever(1, true, {
        reqList ->

        val now = System.currentTimeMillis()
        for(req in reqList) {

            if(rxRequestList.containsKey(req)) {
                // check if the retry time is reached.
                if(now - rxRequestList[req]!! < Retriever.MAXREREQPERIOD * 1000) {
                    println("Test failed. Requested Repeat < 3sec for $req")
                    System.exit(1)
                } else {
                    rxRequestList[req] = now
                }
            } else {
                rxRequestList[req] = now
            }
        }
        println("Received Req $reqList")
    }, {
        println("Final called!!!")
        isFinal = true
    })


    retriever.seqEnd = end

    val reqTimer = Timer()
    var reqSkipCnt = 0
    // schedule at a fixed rate
    reqTimer.scheduleAtFixedRate(0, 50) {

        if(reqSkipCnt <= reqSkipCount) {
            reqSkipCnt++

            if(rxRequestList.isNotEmpty()) {
                val dd = rxRequestList.entries.first()
                rxRequestList.remove(dd.key)
            }
            return@scheduleAtFixedRate
        }

        reqSkipCnt = 0
        for(req in rxRequestList) {
            retriever.incoming(req.key)
            if(sendList.contains(req.key)) {
                println("Something wrong!! Sending already sent list.")
                System.exit(1)
            }
            sendList.add(req.key)
        }
        rxRequestList.clear()

        if (isFinal) {
            reqTimer.cancel()
            println("Test Completed")
            System.exit(1)
        }
    }

    val timer = Timer()
    var seq1 = start
    var skipCnt = 0
    // schedule at a fixed rate
    timer.scheduleAtFixedRate(200, 100) {

        if(skipCnt > liveSkipCount) {

            if(sendList.contains(seq1)) {
                println("Something wrong!! Sending already sent list.")
                System.exit(1)
            }

            retriever.incoming(seq1)
            sendList.add(seq1)

            skipCnt = 0
        }
        seq1++
        skipCnt++

        // Finalizing with a lesser sequence number.
        if(end - seq1 < 10) {
            if(end - seq1 == 5) {
                println("Finalizing with $seq1")
                retriever.finalise(seq1)
            }
        }
        if(seq1 > end){
            timer.cancel()
            println("Completed sending")
        }
    }
}

fun main(args: Array<String>) {

    try {
        val testNum = args[0]
        val startSeq = args[1]
        val endSeq = args[2]

        if (testNum.toInt() == 1) {
            inorderRetrieverTest(startSeq.toInt(), endSeq.toInt())
        } else if(testNum.toInt() == 2) {
            val liveSkip = args[3]
            val reqSkip = args[3]
            missingSequenceTest(startSeq.toInt(), endSeq.toInt(), liveSkip.toInt(), reqSkip.toInt(), true)
        }
    }catch (e: Exception) {
        println("Supply testNum(1/2), startSeq, endSeq, liveSkipCount, reqSkipCount")
        System.exit(1)
    }
}