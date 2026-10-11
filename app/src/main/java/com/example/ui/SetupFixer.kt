package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.util.AutoStartSettings
import com.example.util.ReadinessCheck
import com.example.util.SetupItem
import com.example.util.SetupState

/**
 * Opens the right Android screen or dialog for each setup problem. In guided mode it moves on to the
 * next problem every time the user comes back, until everything it can fix has been tried once.
 */
class SetupFixer internal constructor(
    private val context: Context,
    private val viewModel: CallAnnouncerViewModel,
    private val permissionLauncher: ActivityResultLauncher<Array<String>>,
    private val activityLauncher: ActivityResultLauncher<Intent>
) {
    var isGuiding by mutableStateOf(false)
        private set
    private val attempted = mutableSetOf<SetupItem>()

    fun fixEverything() {
        attempted.clear()
        isGuiding = true
        continueGuided()
    }

    internal fun onReturned() {
        viewModel.refreshReadiness()
        if (isGuiding) continueGuided()
    }

    private fun continueGuided() {
        val next = viewModel.readiness.value.fixable.firstOrNull { it.item !in attempted }
        if (next == null) {
            isGuiding = false
            return
        }
        attempted += next.item
        // Nothing opened (or it finished instantly): move straight on to the next problem
        if (!fix(next)) continueGuided()
    }

    /** Returns true if an Android screen was opened; the result arrives when the user returns. */
    fun fix(check: ReadinessCheck): Boolean = when (check.item) {
        SetupItem.PHONE_PERMISSIONS -> requestPermissions(
            listOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS)
        )
        SetupItem.CONTACTS -> requestPermissions(listOf(Manifest.permission.READ_CONTACTS))
        SetupItem.NOTIFICATIONS ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPermanentlyDenied(Manifest.permission.POST_NOTIFICATIONS)) {
                requestPermissions(listOf(Manifest.permission.POST_NOTIFICATIONS))
            } else {
                open(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                    || openAppDetails()
            }
        SetupItem.CALLER_ID_ROLE -> {
            val roleIntent = viewModel.createCallScreeningRoleIntent(context)
            if (roleIntent != null) launch(roleIntent) else open(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        }
        SetupItem.BATTERY_OPTIMIZATION ->
            launch(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri()))
                || open(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                || openAppDetails()
        SetupItem.BACKGROUND_RESTRICTION -> openAppDetails()
        SetupItem.AUTO_START -> AutoStartSettings.open(context)
        SetupItem.VOICE ->
            if (!viewModel.isTtsReady.value) {
                open(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$GOOGLE_TTS_PACKAGE")))
                    || open(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$GOOGLE_TTS_PACKAGE")))
            } else {
                open(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
                    || open(Intent("com.android.settings.TTS_SETTINGS"))
            }
        SetupItem.ANNOUNCER_RUNNING -> {
            viewModel.turnOnAnnouncer()
            false
        }
        SetupItem.UNUSED_APP_PAUSE ->
            launch { IntentCompat.createManageUnusedAppRestrictionsIntent(context, context.packageName) }
                || openAppDetails()
        SetupItem.POWER_SAVER -> open(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
        SetupItem.RINGER_ON, SetupItem.REAL_CALL_TEST -> false
    }

    private fun requestPermissions(permissions: List<String>): Boolean {
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) return false
        // After repeated refusals Android stops showing the dialog; only App info can grant it then
        if (missing.all { isPermanentlyDenied(it) }) return openAppDetails()
        SetupState.markPermissionsRequested(context)
        permissionLauncher.launch(missing.toTypedArray())
        return true
    }

    private fun isPermanentlyDenied(permission: String): Boolean {
        val activity = context.findActivity() ?: return false
        return SetupState.werePermissionsRequested(context) &&
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    private fun openAppDetails(): Boolean = open(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri()))

    private fun packageUri(): Uri = Uri.fromParts("package", context.packageName, null)

    // Not checked with resolveActivity first: package visibility rules hide many Settings screens from it
    private fun launch(intent: () -> Intent): Boolean = try {
        activityLauncher.launch(intent())
        true
    } catch (_: Exception) {
        false
    }

    private fun launch(intent: Intent): Boolean = launch { intent }

    private fun open(intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (_: Exception) {
        false
    }

    private companion object {
        const val GOOGLE_TTS_PACKAGE = "com.google.android.tts"
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun rememberSetupFixer(viewModel: CallAnnouncerViewModel): SetupFixer {
    val context = LocalContext.current
    // Results are also picked up by the ON_RESUME refresh below, which drives guided mode
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    val fixer = remember(viewModel) { SetupFixer(context, viewModel, permissionLauncher, activityLauncher) }

    var resumedOnce by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (resumedOnce) fixer.onReturned() else viewModel.refreshReadiness()
        resumedOnce = true
    }
    return fixer
}
