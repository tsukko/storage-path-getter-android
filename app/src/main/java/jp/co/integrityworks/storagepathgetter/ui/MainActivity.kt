package jp.co.integrityworks.storagepathgetter.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.ads.MobileAds
import jp.co.integrityworks.storagepathgetter.R
import jp.co.integrityworks.storagepathgetter.ui.screens.StoragePathScreen
import jp.co.integrityworks.storagepathgetter.ui.theme.StoragePathGetterTheme
import jp.co.integrityworks.storagepathgetter.util.Logger
import jp.co.integrityworks.storagepathgetter.util.Utils

class MainActivity : ComponentActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private val util by lazy { Utils(applicationContext) }

    // 権限設定画面から戻ってきた時の処理
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (util.hasUsageStatsPermission()) {
                // 権限が取得できたらUIの状態を更新するためのトリガーを引く
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        Logger.debug(TAG, "onCreate")
        enableEdgeToEdge()

        MobileAds.initialize(this) {}

        setContent {
            StoragePathGetterTheme {
                StoragePathScreen(
                    util = util,
                    onRequestPermission = { showPermissionDialog() }
                )
            }
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_permission_title))
            .setMessage(getString(R.string.dialog_permission_message))
            .setPositiveButton(getString(R.string.dialog_permission_positive)) { _, _ ->
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startForResult.launch(intent)
            }
            .setNegativeButton(getString(R.string.dialog_permission_negative), null)
            .show()
    }
}
