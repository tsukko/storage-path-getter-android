package jp.co.integrityworks.storagepathgetter.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.ads.MobileAds
import jp.co.integrityworks.storagepathgetter.ui.screens.StoragePathScreen
import jp.co.integrityworks.storagepathgetter.ui.theme.StoragePathGetterTheme
import jp.co.integrityworks.storagepathgetter.util.Logger
import jp.co.integrityworks.storagepathgetter.util.Utils

class MainActivity : ComponentActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private val util by lazy { Utils(applicationContext) }

    // 権限設定画面へ遷移するためのランチャー
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // 戻ってきた時の処理はStoragePathScreenのonResumeで自動で行われるため、ここでは何もしません
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
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                            data = Uri.fromParts("package", packageName, null)
                        }
                        startForResult.launch(intent)
                    }
                )
            }
        }
    }
}
