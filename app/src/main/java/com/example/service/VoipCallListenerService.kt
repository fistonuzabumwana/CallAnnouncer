package com.example.service

import android.app.Notification
import android.app.Person
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.BundleCompat
import com.example.data.AppSettings
import com.example.util.CallNotification
import com.example.util.CallingApp
import com.example.util.DebugLog
import com.example.util.MessageNotification
import com.example.util.MessageParser
import com.example.util.MessagingApp
import com.example.util.VoipCallParser
import com.example.util.VoipCallState

/**
 * Reads incoming-call notifications of internet calling apps (WhatsApp, Telegram, Signal…),
 * which never reach Android's phone call APIs. Every other notification is ignored.
 */
class VoipCallListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (CallingApp.fromPackage(sbn.packageName) != null) {
            when (val state = VoipCallParser.parse(sbn.toCallNotification())) {
                is VoipCallState.Ringing -> {
                    IncomingCallHandler.onVoipRinging(this, sbn.key, state.call, allowedByDoNotDisturb(sbn))
                    return
                }
                VoipCallState.NotRinging -> {
                    IncomingCallHandler.onVoipEnded(this, sbn.key)
                    return
                }
                VoipCallState.Unrelated -> Unit
            }
        }
        checkForMessage(sbn)
    }

    private fun checkForMessage(sbn: StatusBarNotification) {
        val smsPackage = try { Telephony.Sms.getDefaultSmsPackage(this) } catch (_: Exception) { null }
        if (MessagingApp.fromPackage(sbn.packageName, smsPackage) == null) return
        if (!AppSettings.getInstance(this).getSettings().announceMessages) return
        // Only fresh notifications; updates to old ones (e.g. marking read) keep their post time
        if (System.currentTimeMillis() - sbn.postTime > FRESH_MESSAGE_MS) return
        val message = MessageParser.parse(sbn.toMessageNotification(), smsPackage) ?: return
        MessageAnnouncer.onMessage(this, message, allowedByDoNotDisturb(sbn))
    }

    private fun StatusBarNotification.toMessageNotification(): MessageNotification {
        val n = notification
        val extras = n.extras
        val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(n)
        val last = style?.messages?.lastOrNull()
        return MessageNotification(
            packageName = packageName,
            category = n.category,
            isGroupSummary = n.flags and Notification.FLAG_GROUP_SUMMARY != 0,
            isOngoing = n.flags and Notification.FLAG_ONGOING_EVENT != 0,
            title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            conversationTitle = style?.conversationTitle?.toString(),
            isGroupConversation = style?.isGroupConversation == true,
            lastSender = last?.person?.name?.toString(),
            lastText = last?.text?.toString()
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (CallingApp.fromPackage(sbn.packageName) == null) return
        IncomingCallHandler.onVoipEnded(this, sbn.key)
    }

    override fun onListenerConnected() {
        DebugLog.d(TAG, "Notification listener connected")
    }

    private fun allowedByDoNotDisturb(sbn: StatusBarNotification): Boolean {
        val ranking = Ranking()
        return try {
            !currentRanking.getRanking(sbn.key, ranking) || ranking.matchesInterruptionFilter()
        } catch (_: Exception) {
            true
        }
    }

    private fun StatusBarNotification.toCallNotification(): CallNotification {
        val n = notification
        val extras = n.extras
        val callType: Int
        val personName: String?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            callType = extras.getInt(Notification.EXTRA_CALL_TYPE, 0)
            personName = BundleCompat.getParcelable(extras, Notification.EXTRA_CALL_PERSON, Person::class.java)
                ?.name?.toString()
        } else {
            callType = 0
            personName = null
        }
        return CallNotification(
            packageName = packageName,
            category = n.category,
            callStyleType = callType,
            hasFullScreenIntent = n.fullScreenIntent != null,
            isOngoingFlag = n.flags and Notification.FLAG_ONGOING_EVENT != 0,
            callPersonName = personName,
            title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        )
    }

    companion object {
        private const val TAG = "VoipCallListener"
        private const val FRESH_MESSAGE_MS = 15_000L

        fun isAccessGranted(context: Context): Boolean =
            NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

        fun componentName(context: Context) = ComponentName(context, VoipCallListenerService::class.java)
    }
}
