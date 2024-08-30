package jp.co.integrityworks.storagepathgetter.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import jp.co.integrityworks.storagepathgetter.BuildConfig
import jp.co.integrityworks.storagepathgetter.Logger
import jp.co.integrityworks.storagepathgetter.R
import jp.co.integrityworks.storagepathgetter.Utils
import jp.co.integrityworks.storagepathgetter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.debug(TAG, "onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.title =
            if (BuildConfig.DEBUG) getString(R.string.app_name) + " (deb)" else getString(R.string.app_name)

        //クリップボードのサービスのインスタンスを取得する
        val mManager: ClipboardManager =
            applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        binding.copyInternalButton.setOnClickListener {
            val myClip: ClipData = ClipData.newPlainText("label", binding.internalPathEditText.text)
            mManager.setPrimaryClip(myClip)
            Toast.makeText(
                applicationContext,
                "内部ストレージのパスをコピーしました。\n" + binding.internalPathEditText.text,
                Toast.LENGTH_LONG
            ).show()
        }
        binding.copyExternalButton.setOnClickListener {
            val myClip: ClipData = ClipData.newPlainText("label", binding.externalPathEditText.text)
            mManager.setPrimaryClip(myClip)
            Toast.makeText(
                applicationContext,
                "外部ストレージのパスをコピーしました。\n" + binding.externalPathEditText.text,
                Toast.LENGTH_LONG
            ).show()
        }

        binding.getPathButton.setOnClickListener { init() }
        binding.clearButton.setOnClickListener {
            binding.internalPathEditText.text?.clear()
            binding.externalPathEditText.text?.clear()
            binding.sizeTextView.text = ""
        }

        MobileAds.initialize(this) { initializationStatus: InitializationStatus ->
            /* get the adapter status */
            val map =
                initializationStatus.adapterStatusMap
            for ((key, adapterStatus) in map) {
                val state = adapterStatus.initializationState
                Logger.debug(TAG,
                    "key = " + key + ", state = " + state.name + ", desc = " + adapterStatus.description
                )
            }
        }

        val adRequest: AdRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        Logger.debug(TAG, "init()")
        val util = Utils(applicationContext)
        val internalPath = util.getPath(false)
        val externalPath = util.getPath(true)
        binding.internalPathEditText.setText(internalPath)
        binding.externalPathEditText.setText(externalPath)
        binding.sizeTextView.text = util.getMemoryInformation(internalPath, externalPath)

        // 使用状況アクセス権限をリクエスト
//        util.requestUsageStatsPermission(this)
//        val appUsageMap = util.getAppStorageUsage(applicationContext)
//        Logger.debug(TAG, appUsageMap.toString())
    }
}