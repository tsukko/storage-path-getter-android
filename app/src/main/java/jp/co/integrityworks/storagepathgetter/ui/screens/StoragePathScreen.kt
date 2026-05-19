package jp.co.integrityworks.storagepathgetter.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import jp.co.integrityworks.storagepathgetter.R
import jp.co.integrityworks.storagepathgetter.ui.components.AdBanner
import jp.co.integrityworks.storagepathgetter.ui.components.PathCard
import jp.co.integrityworks.storagepathgetter.ui.theme.Dimens
import jp.co.integrityworks.storagepathgetter.ui.theme.StoragePathGetterTheme
import jp.co.integrityworks.storagepathgetter.util.Logger
import jp.co.integrityworks.storagepathgetter.util.Utils

@Composable
fun StoragePathScreen(util: Utils, onRequestPermission: () -> Unit) {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current

    var internalPath by remember { mutableStateOf(if (isInspection) "/storage/emulated/0" else "") }
    var externalPath by remember { mutableStateOf(if (isInspection) "/storage/1234-5678" else "") }
    var memoryInfo by remember { mutableStateOf(if (isInspection) "内部メモリーの利用可能容量:\t10 GB\n内部メモリーの総容量:\t\t\t\t128 GB" else "") }

    // データをロードする関数
    val loadData = {
        if (util.hasUsageStatsPermission()) {
            val iPath = util.getPath(false)
            val ePath = util.getPath(true)
            internalPath = iPath
            externalPath = ePath
            memoryInfo = util.getMemoryInformation(iPath, ePath)

            val appUsageList = util.getAppStorageUsage()
            Logger.debug("MainActivity", appUsageList.toString())
        } else {
            onRequestPermission()
        }
    }

    // 初回表示時にデータをロード
    LaunchedEffect(Unit) {
        if (!isInspection) {
            loadData()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.MarginLarge)
                    .padding(bottom = Dimens.MarginXXXLarge)
            ) {
                // ボタン類
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.MarginMiddle)
                ) {
                    Button(
                        onClick = { loadData() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.text_reacquire))
                    }
                    Button(
                        onClick = {
                            internalPath = ""
                            externalPath = ""
                            memoryInfo = ""
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.text_clear))
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.MarginLarge))

                // 広告の表示
                AdBanner(modifier = Modifier.fillMaxWidth())
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(Dimens.MarginLarge)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.MarginXLarge)
        ) {
            Text(
                text = stringResource(id = R.string.text_information),
                style = MaterialTheme.typography.bodyLarge
            )

            // 内部ストレージカード
            PathCard(
                title = stringResource(id = R.string.text_inner_path),
                path = internalPath,
                onCopy = { copyToClipboard(context, "内部ストレージのパス", it) }
            )

            // 外部ストレージカード
            PathCard(
                title = stringResource(id = R.string.text_external_path),
                path = externalPath,
                onCopy = { copyToClipboard(context, "外部ストレージのパス", it) }
            )

            // メモリ情報
            Text(
                text = memoryInfo,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label をコピーしました。\n$text", Toast.LENGTH_LONG).show()
}

@Preview(showBackground = true)
@Composable
fun StoragePathScreenPreview() {
    val context = LocalContext.current
    StoragePathGetterTheme {
        StoragePathScreen(
            util = Utils(context),
            onRequestPermission = {}
        )
    }
}
