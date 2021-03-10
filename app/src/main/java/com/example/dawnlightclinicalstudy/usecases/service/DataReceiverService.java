package com.example.dawnlightclinicalstudy.usecases.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.lifesignals.LSPatch;
import com.lifesignals.sensorproc.SensorProc;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import static androidx.core.app.NotificationCompat.PRIORITY_LOW;

/**
 * Android Service Class.
 * It runs in background, get data using UdpManager and gives to the UI component.
 * Provides public method for 'stopStream','setOnDataReceiveListener'.
 * Interface OnDataReceiveListener should be used for getting callback.
 */

public class DataReceiverService extends Service {

    private static String LOG_TAG = "DRC";
    private IBinder mBinder = new DataReceiverServiceBinder();

    private LSPatch patch;
    private Reorderer reorderer;
    private Retriever retriever;
    private Recorder recorder;

    //Filter Integration
    SensorProc proc;

    private long epochTime = 0L;
    private boolean isConnectionLoss = false;

    private ServiceListener receiverServiceListener = null;
    private JSONObject mBroadcastData = null;

    private FileWriteThread debugLog = null;
    private FileWriteThread orderedDataLog = null;
    private FileWriteThread ecg0Log = null;
    private FileWriteThread respLog = null;
    private FileWriteThread accelLog = null;
    private FileWriteThread tempLog = null;

    public void setRecieverServiceListener(ServiceListener receiverServiceListner) {
        this.receiverServiceListener = receiverServiceListner;
    }

    public DataReceiverService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(LOG_TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.e(LOG_TAG, "Service on destroy called");
        if (orderedDataLog != null)
            orderedDataLog.interrupt();
        orderedDataLog = null;
        if (debugLog != null)
            debugLog.interrupt();
        debugLog = null;
        if (ecg0Log != null)
            ecg0Log.interrupt();
        ecg0Log = null;
        if (respLog != null)
            respLog.interrupt();
        respLog = null;
        if (accelLog != null)
            accelLog.interrupt();
        accelLog = null;
        if (tempLog != null)
            tempLog.interrupt();
        tempLog = null;

        super.onDestroy();
    }


    public void startService() {
        Notification notification = getNotification();
        startForeground(1, notification);

        patch = null;
    }

    public Notification getNotification() {
        String channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = createChannel();
        else {
            channel = "";
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channel)
//                .setSmallIcon(R.drawable.notif_logo)
                .setContentTitle("cardiac service");
        Notification notification = mBuilder
                .setPriority(PRIORITY_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setAutoCancel(true)
                .build();


        return notification;
    }

    @NonNull
    @TargetApi(26)
    private synchronized String createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String name = "Cardiac Service";
        int importance = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_LOW;
        }

        NotificationChannel mChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel("cardiac channel", name, importance);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            } else {
                stopSelf();
            }
        }


        return "cardiac channel";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);
        Log.e(LOG_TAG, "on start method called ");
        return START_NOT_STICKY;
    }


    public class DataReceiverServiceBinder extends Binder {
        public DataReceiverService getService() {
            return DataReceiverService.this;
        }
    }


    /*********** PUBLIC METHODS ******************/

    public void startInterface() {

        if (patch == null) {
            Log.e(LOG_TAG, "Starting Interface");
            patch = new LSPatch(new JSONObject(), (discoveryObject) -> {

                Log.e(LOG_TAG, "Discovery" + discoveryObject);
//                try {
//                    // TODO: Remove this hard code, once the wiring is corrected at Proc lib side.
//                    JSONObject cap = discoveryObject.getJSONObject("Capability");
//                    int[] wir = new int[]{3085, 2829, 4112, 4112, 4112, 4112, 4112};
//                    JSONArray arr = new JSONArray();
//                    for(int i = 0; i < wir.length; i++)
//                        arr.put(wir[i]);
//                    cap.put("ECGWiring", arr);
//                    discoveryObject.put("Capability", cap);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                // Always keep the broadcast object updated.
                if (mBroadcastData != null) {
                    try {
                        String patchID = discoveryObject.getJSONObject("PatchInfo").getString("PatchId");
                        String selectedPatchID = mBroadcastData.getJSONObject("PatchInfo").getString("PatchId");
                        if (selectedPatchID.equals(patchID)) {
                            mBroadcastData = discoveryObject;

                            if (debugLog != null)
                                debugLog.writeLog(mBroadcastData.toString());
                            if (isConnectionLoss) {
                                InetAddress phoneLocalInetAdress = getLocalIpAddress(mBroadcastData.getString("PatchIP"));
                                if (phoneLocalInetAdress != null) {
                                    String patchDestIP = mBroadcastData.getJSONObject("Capability").getString("DestIP");
                                    if (!(phoneLocalInetAdress.toString().replace("/", "").equals(patchDestIP))) {
                                        System.out.println("Patch IP : " + patchDestIP + " Phone IP : " + phoneLocalInetAdress);
                                        redirect(phoneLocalInetAdress.toString().replace("/", ""));
                                    }

                                /*
                                InetAddress phoneLocalInetAdress = getLocalIpAddress(patchID);
                                if (phoneLocalInetAdress != null) {
                                    String patchDestIP = mBroadcastData.getJSONObject("Capability").getString("DestIP");
                                    if (!(phoneLocalInetAdress.toString().replace("/", "").equals(patchDestIP))) {
                                        System.out.println( "Patch IP : " + patchDestIP + " Phone IP : " + phoneLocalInetAdress);
                                        redirect(phoneLocalInetAdress.toString().replace("/", ""));
                                    }*/
                                }

                                int patchStatus = mBroadcastData.getJSONObject("Capability").getInt("PatchStatus");
                                int seq = mBroadcastData.getJSONObject("Capability").getInt("TotalAvailSequence");

                                if ((patchStatus & 16) == 16) {
                                    // proc already completed. The retriever needs a trigger to start the request.
                                    Log.e(LOG_TAG, "ReQ Proc completed ******* ");
                                    retriever.incoming(seq);
//                                    if(isBulkRequestSendAfterProcedureComplete == false)
//                                    {
//                                        if(patch != null)
//                                            patch.requestData(1,seq);
//                                        isBulkRequestSendAfterProcedureComplete = true;
//                                    }

                                }

                            }
                        }
//                    if(recorder != null)
//                        recorder.incoming(seq, streamObject.toString().getBytes());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                receiverServiceListener.onEvent("onDiscovery", discoveryObject);
                return null;
            }, (streamObject) -> {

                Log.e(LOG_TAG, "Data : " + streamObject);

                String patchId = null;
                String selectedPatchID = null;
                try {
                    patchId = streamObject.getJSONObject("SensorData").getString("PatchId");
                    selectedPatchID = mBroadcastData.getJSONObject("PatchInfo").getString("PatchId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!(patchId.equals(selectedPatchID))) {
                    return null;
                }


                if (isConnectionLoss)
                    isConnectionLoss = false;
                // Apply ordering
                if (reorderer != null)
                    reorder(streamObject);

                // Check if anything is missing, and trigger request command if required
                try {
                    if (recorder != null)
                        //  recorder.incoming(seq, streamObject.toString().getBytes());


                        if (debugLog != null)
                            debugLog.writeLog(streamObject.toString());

                    int seq = streamObject.getJSONObject("SensorData").getInt("Seq");

                    if (retriever != null)
                        retriever.incoming(seq);

                    // save to local storage
                    if (recorder != null)
                        recorder.incoming(seq, streamObject.toString().getBytes());

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                receiverServiceListener.onEvent("onData", streamObject);
                return null;
            }, (status) -> {

                if (debugLog != null)
                    debugLog.writeLog(status.toString());
                try {
                    String statusStr = status.getString("status");
                    String val = status.getString("value");

                    if (statusStr.equals("connection")) {
                        if (val.equals("socket-timeout")) {
                            isConnectionLoss = true;
                        }
                    } else if (statusStr.equals("command")) {
                        String cmd = status.getString("command");
                        if (cmd.equals("finish")) {

                        } else if (cmd.equals("start")) {
                            if (val.equals("success") || val.equals("usage-err")) {
                                int patchStatus = mBroadcastData.getJSONObject("Capability").getInt("PatchStatus");
                                int seq = mBroadcastData.getJSONObject("Capability").getInt("TotalAvailSequence");

                                if ((patchStatus & 16) == 16) {
                                    // proc already completed. The retriever needs a trigger to start the request.
                                    if (retriever != null)
                                        retriever.incoming(seq);

                                }
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                receiverServiceListener.onEvent("onStatus", status);
                return null;
            });
        }
    }


    public void select(JSONObject broadcastData) {

        mBroadcastData = broadcastData;
        String patchID;
        try {
            patchID = mBroadcastData.getJSONObject("PatchInfo").getString("PatchId");

            if (patch != null)
                patch.select(patchID);

            // Comment if any of these is not required.
            initializeReorderer();
            initializeRetriever();
            initializeRecorder(patchID);
            initializeLogFileThread();
            initialiseSensorProc();
//
//            try {
//                String configJSONstring = "{\"ConfigurationParams\": {\"DynamicIAgaincomp\": \"Dis\", \"FilterType\": \"Monitoring\", \"HREstimationLead\": \"ECG_LEAD_B\", \"MotionCompensation\": \"Dis\", \"Notch\" : \"Dis\"}}\n";
//                proc.sensorProcConfig(new JSONObject(configJSONstring), mBroadcastData);
//
//                proc = new SensorProc( (dataOut) -> {
//
//                    if(receiverServiceListener != null) {
//                        Log.e(LOG_TAG, "In Proc outpyut " + dataOut);
//                        receiverServiceListener.onEvent("onFilteredData", dataOut);
//                    }
//
//                    return null;
//                });
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void configure(JSONObject sensorConfig) {
        if (patch != null)
            patch.configure(sensorConfig);
    }


    public void start() {
        if (patch != null)
            patch.start();
    }

    public void disableRetriever(boolean disable) {

        retriever = null;
        reorderer = null;
    }

    public void requestInRange(int start, int end) {

        if (patch != null)
            patch.requestData(start, end);
    }

    public void identify() {

        if (patch != null)
            patch.identify();
        getSensorProcVersion();
    }

    public void getSensorProcVersion() {
        String sensorProcVersion = proc.sensorProcVersion();
        Log.d(LOG_TAG, "Sensor proc version :" + sensorProcVersion);
    }

    public String getData(int sequenceNumber) {

        String data = "";
        try {
            if (recorder != null)
                data = new String(recorder.get(sequenceNumber));

        } catch (Exception e) {
            e.printStackTrace();
            data = e.getMessage();
        }
        return data;
    }

    public void stopAcquisition() {
        if (patch != null)
            patch.stopAcquisition();
    }

    public void turnOff(boolean eraseFlash) {
        if (patch != null)
            patch.turnOff(eraseFlash);
    }

    public void getRequest(ArrayList<Integer> sequenceList) {
        if (patch != null) {
            String s = sequenceList.toString();
            debugLog.writeLog(s);
            patch.requestData(sequenceList);
        }

    }

    public void commit(Boolean longSync) {
        if (patch != null)
            patch.commit(longSync);
    }

    public void redirect(String newIP) {
        if (patch != null)
            patch.redirect(newIP);
    }

    public void reConfigure(String ssid, String password) {
        if (patch != null)
            patch.configureSSID(ssid, password);
    }

    public void finish() {
        if (patch != null)
            patch.finish();

        retriever = null;
        reorderer = null;
        recorder = null;

        mBroadcastData = null;
        isConnectionLoss = false;
        epochTime = 0L;

        if (orderedDataLog != null)
            orderedDataLog.interrupt();
        orderedDataLog = null;
        if (debugLog != null)
            debugLog.interrupt();
        debugLog = null;
        if (ecg0Log != null)
            ecg0Log.interrupt();
        ecg0Log = null;
        if (respLog != null)
            respLog.interrupt();
        respLog = null;

    }

    //Private Methods

    private void initialiseSensorProc() {
        proc = new SensorProc(new JSONObject(), mBroadcastData, (dataOut) -> {

            if (receiverServiceListener != null) {
                Log.e(LOG_TAG, "In Proc outpyut " + dataOut);
                receiverServiceListener.onEvent("onFilteredData", dataOut);
            }

            return null;
        });
    }

    private int lastSeq = 0;

    private void initializeReorderer() {
        if (reorderer == null) {
            reorderer = new Reorderer(10, 1000, (orderedData, ts, seq) -> {
                // Ordered data is obtained here. This can be passed to SensorProc module for
                // filtering and HR calculation.
                // Or, can be plotted directly, after extracting the ECG data.
                if (orderedDataLog != null) {
                    orderedDataLog.writeLog(String.valueOf(seq));
                }

               /* try {
                    JSONObject dd = (JSONObject)orderedData;
                    JSONObject sensorData = dd.getJSONObject("SensorData");
                    JSONArray ecg0Array = sensorData.getJSONArray("ECG0");
                    for(int i = 0; i < ecg0Array.length(); i++) {
                        ecg0Log.writeLog(seq + "," + ecg0Array.getInt(i));
                    }
                    if(sensorData.has("Respiration")) {
                        JSONArray respArray = sensorData.getJSONArray("Respiration");
                        for (int i = 0; i < respArray.length(); i++) {
                            respLog.writeLog(seq + "," + respArray.getInt(i));
                        }
                    }
                    if(sensorData.has("Accel")) {
                        JSONArray accelArray = sensorData.getJSONArray("Accel");
                        for (int i = 0; i < accelArray.length(); i++) {
                            accelLog.writeLog(seq + "," + accelArray.getInt(i));
                        }
                    }
                    if(sensorData.has("Temperature")) {
                        int temp = sensorData.getInt("Temperature");
                        tempLog.writeLog(seq + "," + temp);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/

                receiverServiceListener.onEvent("onOrderedData", (JSONObject) orderedData);
//                receiverServiceListener.onEvent("onFilteredData", (JSONObject) orderedData);
                proc.sensorProc((JSONObject) orderedData);
                return null;
            }, 0);
        }
    }

    private void reorder(JSONObject object) {
        long timsestamp = 0;
        long realTime = 0;
        int seq = 0;
        try {

            seq = object.getJSONObject("SensorData").getInt("Seq");
            timsestamp = object.getJSONObject("SensorData").getLong("TsECG");

            if (epochTime == 0L && mBroadcastData != null) {
                epochTime = mBroadcastData.getJSONObject("Capability").getLong("StartTime");
                if (epochTime == 0L)
                    epochTime = (System.currentTimeMillis() * 1000 - timsestamp) / 1000000;
            }

            realTime = (epochTime * 1000000) + timsestamp;

        } catch (JSONException e) {
            Log.e("REORDER", "Exception for " + seq);
            e.printStackTrace();
        }
        if (reorderer != null)
            reorderer.incoming(object, realTime / 1000, seq, true);
    }

    private void initializeRetriever() {
        if (retriever == null) {
            retriever = new Retriever(1, true, (reqList) -> {
                Log.e(LOG_TAG, "ReQ: " + Arrays.toString(new ArrayList[]{reqList}));
                getRequest(reqList);
                return null;

            }, () -> {
                return null;
            });
        }
    }

    private void initializeRecorder(String prefix) {
        if (recorder == null) {
            String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() +
                    File.separator + prefix;
            recorder = new Recorder(prefix, directoryPath);
        }
    }

    private InetAddress getLocalIpAddress(String patchIP) {

        String subIp = StringUtils.substringBeforeLast(patchIP, ".");
        InetAddress localInetAddress = null;
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    String str = inetAddress.toString();
                    if (inetAddress.isSiteLocalAddress() && str.contains(subIp)) {
                        localInetAddress = inetAddress;
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            return null;
        }
        return localInetAddress;
    }

    private void initializeLogFileThread() {
        if (debugLog == null && mBroadcastData != null) {
            String patchId = "NO_NAME_ID";
            try {
                patchId = mBroadcastData.getJSONObject("PatchInfo").getString("PatchId");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String filePathEcg = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                    File.separator +
                    "Log" +
                    File.separator + "Debug_" +
                    patchId;

            debugLog = new FileWriteThread(filePathEcg, "txt");
            debugLog.start();
            debugLog.writeLog("*******************NEW**********************");

        }

        if (orderedDataLog == null && mBroadcastData != null) {
            String patchId = "NO_NAME_ID";
            try {
                patchId = mBroadcastData.getJSONObject("PatchInfo").getString("PatchId");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String filePathEcg = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                    File.separator +
                    "Log" +
                    File.separator + "Ordered_" +
                    patchId;

            orderedDataLog = new FileWriteThread(filePathEcg, "txt");
            orderedDataLog.start();

            orderedDataLog.writeLog("*****************NEW******************");

        }

        if (ecg0Log == null && mBroadcastData != null) {
            String patchId = "NO_NAME_ID";
            try {
                patchId = mBroadcastData.getJSONObject("PatchInfo").getString("PatchId");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String filePathEcg = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                    File.separator +
                    "Log" +
                    File.separator + "ECG0_" +
                    patchId;

            ecg0Log = new FileWriteThread(filePathEcg, "csv");
            ecg0Log.start();
        }

        if (respLog == null && mBroadcastData != null) {
            String patchId = "NO_NAME_ID";
            try {
                patchId = mBroadcastData.getJSONObject("PatchInfo").getString("PatchId");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String filePathEcg = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                    File.separator +
                    "Log" +
                    File.separator + "RESP_" +
                    patchId;

            respLog = new FileWriteThread(filePathEcg, "csv");
            respLog.start();
        }

        if (accelLog == null && mBroadcastData != null) {
            String patchId = "NO_NAME_ID";
            try {
                patchId = mBroadcastData.getJSONObject("PatchInfo").getString("PatchId");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String filePathEcg = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                    File.separator +
                    "Log" +
                    File.separator + "ACCEL_" +
                    patchId;

            accelLog = new FileWriteThread(filePathEcg, "csv");
            accelLog.start();
        }

        if (tempLog == null && mBroadcastData != null) {
            String patchId = "NO_NAME_ID";
            try {
                patchId = mBroadcastData.getJSONObject("PatchInfo").getString("PatchId");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String filePathEcg = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                    File.separator +
                    "Log" +
                    File.separator + "TEMP_" +
                    patchId;

            tempLog = new FileWriteThread(filePathEcg, "csv");
            tempLog.start();
        }
    }
}