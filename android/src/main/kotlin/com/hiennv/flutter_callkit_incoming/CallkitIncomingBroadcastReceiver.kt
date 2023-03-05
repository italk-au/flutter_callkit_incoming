package com.hiennv.flutter_callkit_incoming

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.io.File

class CallkitIncomingBroadcastReceiver : BroadcastReceiver() {

    companion object {
        var sessionId: String? = null
        var token: String? = null
        const val ACTION_CALL_INCOMING =
                "com.hiennv.flutter_callkit_incoming.ACTION_CALL_INCOMING"
        const val ACTION_CALL_START = "com.hiennv.flutter_callkit_incoming.ACTION_CALL_START"
        const val ACTION_CALL_ACCEPT =
                "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT"
        const val ACTION_CALL_DECLINE =
                "com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE"
        const val ACTION_CALL_ENDED =
                "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED"
        const val ACTION_CALL_TIMEOUT =
                "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT"
        const val ACTION_CALL_CALLBACK =
                "com.hiennv.flutter_callkit_incoming.ACTION_CALL_CALLBACK"


        const val EXTRA_CALLKIT_INCOMING_DATA = "EXTRA_CALLKIT_INCOMING_DATA"

        const val EXTRA_CALLKIT_ID = "EXTRA_CALLKIT_ID"
        const val EXTRA_CALLKIT_NAME_CALLER = "EXTRA_CALLKIT_NAME_CALLER"
        const val EXTRA_CALLKIT_APP_NAME = "EXTRA_CALLKIT_APP_NAME"
        const val EXTRA_CALLKIT_HANDLE = "EXTRA_CALLKIT_HANDLE"
        const val EXTRA_CALLKIT_TYPE = "EXTRA_CALLKIT_TYPE"
        const val EXTRA_CALLKIT_AVATAR = "EXTRA_CALLKIT_AVATAR"
        const val EXTRA_CALLKIT_DURATION = "EXTRA_CALLKIT_DURATION"
        const val EXTRA_CALLKIT_TEXT_ACCEPT = "EXTRA_CALLKIT_TEXT_ACCEPT"
        const val EXTRA_CALLKIT_TEXT_DECLINE = "EXTRA_CALLKIT_TEXT_DECLINE"
        const val EXTRA_CALLKIT_TEXT_MISSED_CALL = "EXTRA_CALLKIT_TEXT_MISSED_CALL"
        const val EXTRA_CALLKIT_TEXT_CALLBACK = "EXTRA_CALLKIT_TEXT_CALLBACK"
        const val EXTRA_CALLKIT_EXTRA = "EXTRA_CALLKIT_EXTRA"
        const val EXTRA_CALLKIT_HEADERS = "EXTRA_CALLKIT_HEADERS"
        const val EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION = "EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION"
        const val EXTRA_CALLKIT_IS_SHOW_LOGO = "EXTRA_CALLKIT_IS_SHOW_LOGO"
        const val EXTRA_CALLKIT_IS_SHOW_MISSED_CALL_NOTIFICATION = "EXTRA_CALLKIT_IS_SHOW_MISSED_CALL_NOTIFICATION"
        const val EXTRA_CALLKIT_IS_SHOW_CALLBACK = "EXTRA_CALLKIT_IS_SHOW_CALLBACK"
        const val EXTRA_CALLKIT_RINGTONE_PATH = "EXTRA_CALLKIT_RINGTONE_PATH"
        const val EXTRA_CALLKIT_BACKGROUND_COLOR = "EXTRA_CALLKIT_BACKGROUND_COLOR"
        const val EXTRA_CALLKIT_BACKGROUND_URL = "EXTRA_CALLKIT_BACKGROUND_URL"
        const val EXTRA_CALLKIT_ACTION_COLOR = "EXTRA_CALLKIT_ACTION_COLOR"
        const val EXTRA_CALLKIT_INCOMING_CALL_NOTIFICATION_CHANNEL_NAME = "EXTRA_CALLKIT_INCOMING_CALL_NOTIFICATION_CHANNEL_NAME"
        const val EXTRA_CALLKIT_MISSED_CALL_NOTIFICATION_CHANNEL_NAME = "EXTRA_CALLKIT_MISSED_CALL_NOTIFICATION_CHANNEL_NAME"

        const val EXTRA_CALLKIT_ACTION_FROM = "EXTRA_CALLKIT_ACTION_FROM"

        fun getIntentIncoming(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = ACTION_CALL_INCOMING
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }

        fun getIntentStart(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = ACTION_CALL_START
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }

        fun getIntentAccept(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = ACTION_CALL_ACCEPT
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }

        fun getIntentDecline(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = ACTION_CALL_DECLINE
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }

        fun getIntentEnded(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = ACTION_CALL_ENDED
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }

        fun getIntentTimeout(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = ACTION_CALL_TIMEOUT
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }

        fun getIntentCallback(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = ACTION_CALL_CALLBACK
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }
    }


    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val callkitNotificationManager = CallkitNotificationManager(context)
        val action = intent.action ?: return
        val data = intent.extras?.getBundle(EXTRA_CALLKIT_INCOMING_DATA) ?: return
        val endcallStatus = intent.getExtras()?.containsKey("isfromEndAllCalls") 
        if (endcallStatus != null && endcallStatus)    
            return

        Cache.updateLatestEvent(action, data.toData())

        when (action) {
            ACTION_CALL_INCOMING -> {
                try {

                    val callType = Data.fromBundle(data).extra["callType"]
                    if(callType == "video") {
                        val data2 = data.toData()
                        val extra = data2["extra"] as Map<String, Any>
                        sessionId = extra["sessionid"] as String
                        token = extra["token"] as String
                    }

                    callkitNotificationManager.showIncomingNotification(data)
                    sendEventFlutter(ACTION_CALL_INCOMING, data)
                    addCall(context, Data.fromBundle(data))

                    if (callkitNotificationManager.incomingChannelEnabled()) {
                        val soundPlayerServiceIntent =
                            Intent(context, CallkitSoundPlayerService::class.java)
                        soundPlayerServiceIntent.putExtras(data)
                        context.startService(soundPlayerServiceIntent)
                    }
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_START -> {
                try {
                    sendEventFlutter(ACTION_CALL_START, data)
                    addCall(context, Data.fromBundle(data), true)
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_ACCEPT -> {
                try {
                    val callType = Data.fromBundle(data).extra["callType"]

                    if (callType == "voice") {
                        val appDirectory = context.filesDir
                        val file = File(appDirectory, "notifcheckeraccept.txt")
                        file.writeText("notifcheckeraccept")
                    }

                    sendEventFlutter(ACTION_CALL_ACCEPT, data)
                    context.stopService(Intent(context, CallkitSoundPlayerService::class.java))
                    callkitNotificationManager.clearIncomingNotification(data)
                    addCall(context, Data.fromBundle(data), true)
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_DECLINE -> {
                try {
                    val callType = Data.fromBundle(data).extra["callType"]
                    val number = Data.fromBundle(data).handle
                    if (callType == "video") {
                        val url =
                            URL("https://r0u8gr0ge5.execute-api.ap-southeast-2.amazonaws.com/env-dev/sendcallevent?")
                        // add parameter
                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val jsonObject = JSONObject()
                        try {
                            jsonObject.put("eventtype", "reject")
                            jsonObject.put("sessionid", sessionId)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        val body = jsonObject.toString().toRequestBody(mediaType)
                        // creating request
                        var request = Request.Builder().url(url)
                            .post(body)
                            .addHeader("Content-Type", "application/json")
                            .build()

                        var client = OkHttpClient();
                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: okhttp3.Call, e: IOException) {
                                println("fail here")

                            }

                            override fun onResponse(call: okhttp3.Call, response: Response) {
                                println(response.body?.string())
                            }
                        })
                        println("marc here")
                    } else if (callType == "voice") {
                        val appDirectory = context.filesDir
                        val file = File(appDirectory, "notifcheckerdecline.txt")
                        file.writeText("notifcheckerdecline")
                        val url =
                            URL("https://r0u8gr0ge5.execute-api.ap-southeast-2.amazonaws.com/env-dev/sendvoicecallevent?")
                        // // add parameter
                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val jsonObject = JSONObject()
                        try {
                            jsonObject.put("eventtype", "reject")
                            jsonObject.put("number", number)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        val body = jsonObject.toString().toRequestBody(mediaType)
                        // creating request
                        var request = Request.Builder().url(url)
                            .post(body)
                            .addHeader("Content-Type", "application/json")
                            .build()

                        var client = OkHttpClient();
                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: okhttp3.Call, e: IOException) {
                                println("fail here")

                            }

                            override fun onResponse(call: okhttp3.Call, response: Response) {
                                println(response.body?.string())
                            }
                        })
                    } else {
                        sendEventFlutter(ACTION_CALL_DECLINE, data)
                    }
                    context.stopService(Intent(context, CallkitSoundPlayerService::class.java))
                    callkitNotificationManager.clearIncomingNotification(data)
                    removeCall(context, Data.fromBundle(data))
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_ENDED -> {
                try {
                    sendEventFlutter(ACTION_CALL_ENDED, data)
                    context.stopService(Intent(context, CallkitSoundPlayerService::class.java))
                    callkitNotificationManager.clearIncomingNotification(data)
                    removeCall(context, Data.fromBundle(data))
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_TIMEOUT -> {
                try {
                    sendEventFlutter(ACTION_CALL_TIMEOUT, data)
                    context.stopService(Intent(context, CallkitSoundPlayerService::class.java))
                    if (data.getBoolean(EXTRA_CALLKIT_IS_SHOW_MISSED_CALL_NOTIFICATION, true)) {
                        callkitNotificationManager.showMissCallNotification(data)
                    }
                    removeCall(context, Data.fromBundle(data))
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_CALLBACK -> {
                try {
                    callkitNotificationManager.clearMissCallNotification(data)
                    sendEventFlutter(ACTION_CALL_CALLBACK, data)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        val closeNotificationPanel = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                        context.sendBroadcast(closeNotificationPanel)
                    }
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun sendEventFlutter(event: String, bundle: Bundle) {
        FlutterCallkitIncomingPlugin.sendEvent(event, bundle.toData())
    }

    @Suppress("UNCHECKED_CAST")
    private fun Bundle.toData(): Map<String, Any> {
        val android = mapOf(
            "isCustomNotification" to getBoolean(EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION, false),
            "ringtonePath" to getString(EXTRA_CALLKIT_RINGTONE_PATH, ""),
            "backgroundColor" to getString(EXTRA_CALLKIT_BACKGROUND_COLOR, ""),
            "backgroundUrl" to getString(EXTRA_CALLKIT_BACKGROUND_URL, ""),
            "actionColor" to getString(EXTRA_CALLKIT_ACTION_COLOR, ""),
            "incomingCallNotificationChannelName" to getString(
                EXTRA_CALLKIT_INCOMING_CALL_NOTIFICATION_CHANNEL_NAME,
                ""
            ),
            "missedCallNotificationChannelName" to getString(
                EXTRA_CALLKIT_MISSED_CALL_NOTIFICATION_CHANNEL_NAME,
                ""
            ),
        )
        return mapOf(
            "id" to getString(EXTRA_CALLKIT_ID, ""),
            "nameCaller" to getString(EXTRA_CALLKIT_NAME_CALLER, ""),
            "avatar" to getString(EXTRA_CALLKIT_AVATAR, ""),
            "number" to getString(EXTRA_CALLKIT_HANDLE, ""),
            "type" to getInt(EXTRA_CALLKIT_TYPE, 0),
            "duration" to getLong(EXTRA_CALLKIT_DURATION, 0L),
            "textAccept" to getString(EXTRA_CALLKIT_TEXT_ACCEPT, ""),
            "textDecline" to getString(EXTRA_CALLKIT_TEXT_DECLINE, ""),
            "textMissedCall" to getString(EXTRA_CALLKIT_TEXT_MISSED_CALL, ""),
            "textCallback" to getString(EXTRA_CALLKIT_TEXT_CALLBACK, ""),
            "extra" to getSerializable(EXTRA_CALLKIT_EXTRA) as HashMap<String, Any?>,
            "android" to android
        )
    }
}