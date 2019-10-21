package jp.co.integrityworks.storagepathgetter.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import jp.co.integrityworks.storagepathgetter.R
import jp.co.integrityworks.storagepathgetter.Utils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //クリップボードのサービスのインスタンスを取得する
        val mManager: ClipboardManager =
            applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        copyInternalButton.setOnClickListener {
            mManager.primaryClip = ClipData.newPlainText("label", internalPathEditText.text)
            Toast.makeText(
                applicationContext,
                "内部ストレージのパスをコピーしました。\n" + internalPathEditText.text,
                Toast.LENGTH_LONG
            ).show()
        }
        copyExternalButton.setOnClickListener {
            mManager.primaryClip = ClipData.newPlainText("label", externalPathEditText.text)
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
        clearButton.setOnClickListener { }
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
