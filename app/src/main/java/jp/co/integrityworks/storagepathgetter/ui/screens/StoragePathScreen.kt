package jp.co.integrityworks.storagepathgetter.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import jp.co.integrityworks.storagepathgetter.R
import jp.co.integrityworks.storagepathgetter.data.entities.RecentFile
import jp.co.integrityworks.storagepathgetter.data.entities.StorageBreakdown
import jp.co.integrityworks.storagepathgetter.ui.components.AdBanner
import jp.co.integrityworks.storagepathgetter.ui.components.LegacyPathCard
import jp.co.integrityworks.storagepathgetter.ui.components.PathCard
import jp.co.integrityworks.storagepathgetter.ui.components.RecentFilesCard
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

    // 追加: 使用率情報を保持するState
    var internalUsage by remember { mutableFloatStateOf(0f) }
    var internalUsageText by remember { mutableStateOf("") }
    var internalBreakdown by remember { mutableStateOf(StorageBreakdown()) }
    var externalUsage by remember { mutableFloatStateOf(0f) }
    var externalUsageText by remember { mutableStateOf("") }
    var externalBreakdown by remember { mutableStateOf(StorageBreakdown()) }

    // 最近のファイル用
    var recentFiles by remember { mutableStateOf<List<RecentFile>>(emptyList()) }
    var selectedFolderUri by remember { mutableStateOf<Uri?>(null) }

    // フォルダ選択用ランチャー
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            // 永続的なアクセス権限をリクエスト
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedFolderUri = uri
            recentFiles = util.getRecentFiles(uri)
        }
    }

    // フォルダを開く関数
    val openFolder = { path: String ->
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val uri = path.toUri()
                setDataAndType(uri, "resource/folder")
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    val uri = path.toUri()
                    setDataAndType(uri, "*/*")
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "フォルダを開けるアプリが見つかりませんでした", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // データをロードする関数
    val loadData = {
        if (util.hasUsageStatsPermission()) {
            val iPath = util.getPath(isExternal = false)
            val ePath = util.getPath(isExternal = true)
            internalPath = iPath
            externalPath = ePath
            memoryInfo = util.getMemoryInformation(iPath, ePath)

            // 追加: 使用率データの取得
            internalUsage = util.getStorageUsageRatio(path = iPath)
            internalUsageText = util.getStorageUsageText(path = iPath)
            internalBreakdown = util.getStorageBreakdown(path = iPath)
            externalUsage = util.getStorageUsageRatio(path = ePath)
            externalUsageText = util.getStorageUsageText(path = ePath)
            externalBreakdown = util.getStorageBreakdown(path = ePath)

            // 最近のファイルを更新（既にフォルダが選択されている場合）
            selectedFolderUri?.let {
                recentFiles = util.getRecentFiles(it)
            }

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
                            internalUsage = 0f
                            internalUsageText = ""
                            internalBreakdown = StorageBreakdown()
                            externalUsage = 0f
                            externalUsageText = ""
                            externalBreakdown = StorageBreakdown()
                            recentFiles = emptyList()
                            selectedFolderUri = null
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
                icon = Icons.Default.Smartphone,
                usage = internalUsage,
                usageText = internalUsageText,
                breakdown = internalBreakdown,
                onCopy = { copyToClipboard(context, "内部ストレージのパス", it) },
                onOpen = { openFolder(it) }
            )

            // 外部ストレージカード
            PathCard(
                title = stringResource(id = R.string.text_external_path),
                path = externalPath,
                icon = Icons.Default.SdStorage,
                usage = externalUsage,
                usageText = externalUsageText,
                breakdown = externalBreakdown,
                onCopy = { copyToClipboard(context, "外部ストレージのパス", it) },
                onOpen = { openFolder(it) }
            )

            // メモリ情報
            Text(
                text = memoryInfo,
                style = MaterialTheme.typography.bodyMedium
            )

            // 最近のファイル
            RecentFilesCard(
                files = recentFiles,
                onSelectFolder = { folderPickerLauncher.launch(null) }
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

@Preview(showBackground = true, widthDp = 400)
@Composable
fun PathCardComparisonPreview() {
    val samplePath = "/storage/emulated/0/Download"
    val sampleUsage = 0.75f
    val sampleUsageText = "96 GB / 128 GB"

    StoragePathGetterTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("▼ 修正前 (Legacy)", style = MaterialTheme.typography.labelLarge)
            LegacyPathCard(
                title = "内部ストレージ",
                path = samplePath,
                icon = Icons.Default.Smartphone,
                usage = sampleUsage,
                usageText = sampleUsageText,
                onCopy = {},
                onOpen = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("▼ 修正後 (New Modern)", style = MaterialTheme.typography.labelLarge)
            PathCard(
                title = "内部ストレージ",
                path = samplePath,
                icon = Icons.Default.Smartphone,
                usage = sampleUsage,
                usageText = sampleUsageText,
                onCopy = {},
                onOpen = {}
            )
        }
    }
}
