package jp.co.integrityworks.storagepathgetter.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import jp.co.integrityworks.storagepathgetter.R
import jp.co.integrityworks.storagepathgetter.data.entities.RecentFile
import jp.co.integrityworks.storagepathgetter.data.entities.StorageVolume
import jp.co.integrityworks.storagepathgetter.ui.components.AdBanner
import jp.co.integrityworks.storagepathgetter.ui.components.PathCard
import jp.co.integrityworks.storagepathgetter.ui.components.RecentFilesCard
import jp.co.integrityworks.storagepathgetter.ui.theme.Dimens
import jp.co.integrityworks.storagepathgetter.ui.theme.StoragePathGetterTheme
import jp.co.integrityworks.storagepathgetter.util.Utils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoragePathScreen(util: Utils, onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 権限ダイアログの表示管理
    var showPermissionDialog by remember { mutableStateOf(false) }

    // ストレージ情報を一括管理
    var internalVolume by remember {
        mutableStateOf(StorageVolume(path = if (isInspection) "/storage/emulated/0" else ""))
    }
    var externalVolume by remember {
        mutableStateOf(StorageVolume(path = if (isInspection) "/storage/1234-5678" else ""))
    }

    var recentFiles by remember { mutableStateOf<List<RecentFile>>(emptyList()) }
    var selectedFolderUri by remember { mutableStateOf<Uri?>(null) }

    // 特定のフォルダを初期位置として開くためのランチャー
    val customFolderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                selectedFolderUri = uri
                recentFiles = util.getRecentFiles(uri)
            }
        }
    }

    val launchFolderPicker = { folderName: String? ->
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        try {
            if (folderName != null) {
                val initialUri = DocumentsContract.buildDocumentUri(
                    "com.android.externalstorage.documents",
                    "primary:$folderName"
                )
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
            }
        } catch (e: Exception) {
            jp.co.integrityworks.storagepathgetter.util.Logger.error(
                "StoragePathScreen",
                "Failed to build initial URI",
                e
            )
        }
        customFolderPickerLauncher.launch(intent)
    }

    val noAppFoundMsg = stringResource(id = R.string.msg_no_app_to_open_folder)
    val innerPathLabel = stringResource(id = R.string.text_inner_path)
    val externalPathLabel = stringResource(id = R.string.text_external_path)

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
                scope.launch {
                    snackbarHostState.showSnackbar(noAppFoundMsg)
                }
            }
        }
    }

    val loadData = {
        if (util.hasUsageStatsPermission()) {
            showPermissionDialog = false // 権限があればダイアログを閉じる
            val iPath = util.getPath(isExternal = false)
            val ePath = util.getPath(isExternal = true)

            internalVolume = StorageVolume(
                path = iPath,
                usageRatio = util.getStorageUsageRatio(path = iPath),
                usageText = util.getStorageUsageText(path = iPath),
                breakdown = util.getStorageBreakdown(path = iPath)
            )

            externalVolume = StorageVolume(
                path = ePath,
                usageRatio = util.getStorageUsageRatio(path = ePath),
                usageText = util.getStorageUsageText(path = ePath),
                breakdown = util.getStorageBreakdown(path = ePath)
            )

            selectedFolderUri?.let {
                recentFiles = util.getRecentFiles(it)
            }
        } else {
            showPermissionDialog = true // 権限がなければダイアログを表示
        }
    }

    // 初回表示時にデータをロード
    LaunchedEffect(Unit) {
        if (!isInspection) {
            loadData()
        }
    }

    // 設定画面から戻ってきた時などにデータを再ロードする
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (!isInspection) {
                    loadData()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = {
                        internalVolume = StorageVolume()
                        externalVolume = StorageVolume()
                        recentFiles = emptyList()
                        selectedFolderUri = null
                    }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = stringResource(id = R.string.text_clear)
                        )
                    }
                    IconButton(onClick = { loadData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(id = R.string.text_reacquire)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        bottomBar = {
            AdBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.navigationBars.asPaddingValues())
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.MarginLarge)
                .padding(top = Dimens.MarginLarge),
        ) {
            StatusMessageCard(internalVolume.usageRatio)

            Spacer(modifier = Modifier.height(Dimens.MarginXLarge))

            PathCard(
                util = util,
                title = innerPathLabel,
                path = internalVolume.path,
                icon = Icons.Default.Smartphone,
                usage = internalVolume.usageRatio,
                usageText = internalVolume.usageText,
                breakdown = internalVolume.breakdown,
                onCopy = {
                    copyToClipboard(context, innerPathLabel, it) { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                },
                onOpen = { openFolder(it) }
            )

            Spacer(modifier = Modifier.height(Dimens.MarginXLarge))

            PathCard(
                util = util,
                title = externalPathLabel,
                path = externalVolume.path,
                icon = Icons.Default.SdStorage,
                usage = externalVolume.usageRatio,
                usageText = externalVolume.usageText,
                breakdown = externalVolume.breakdown,
                onCopy = {
                    copyToClipboard(context, externalPathLabel, it) { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                },
                onOpen = { openFolder(it) }
            )

            Spacer(modifier = Modifier.height(Dimens.MarginXLarge))

            RecentFilesCard(
                util = util,
                files = recentFiles,
                onSelectFolder = { launchFolderPicker(it) }
            )

            Spacer(modifier = Modifier.height(Dimens.MarginMiddle))
        }
    }

    // 権限リクエストダイアログ (Compose版)
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { /* キャンセル不可 */ },
            title = { Text(stringResource(id = R.string.dialog_permission_title)) },
            text = { Text(stringResource(id = R.string.dialog_permission_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onOpenSettings()
                }) {
                    Text(stringResource(id = R.string.dialog_permission_positive))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(id = R.string.dialog_permission_negative))
                }
            }
        )
    }
}

@Composable
private fun StatusMessageCard(usage: Float) {
    val message = when {
        usage <= 0f -> stringResource(id = R.string.storage_status_loading)
        usage > 0.9f -> stringResource(id = R.string.storage_status_full)
        usage > 0.7f -> stringResource(id = R.string.storage_status_low)
        else -> stringResource(id = R.string.storage_status_enough)
    }

    val color = when {
        usage > 0.9f -> MaterialTheme.colorScheme.error
        usage > 0.7f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(Dimens.RadiusMedium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Dimens.MarginLarge,
                vertical = Dimens.MarginXLarge
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(Dimens.IconSmall)
            )
            Spacer(modifier = Modifier.width(Dimens.MarginMedium))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

private fun copyToClipboard(
    context: Context,
    label: String,
    text: String,
    onShowSnackbar: (String) -> Unit
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    onShowSnackbar(context.getString(R.string.msg_copied, label, text))
}

@Preview(showBackground = true, name = "日本語", locale = "ja")
@Preview(showBackground = true, name = "English", locale = "en")
@Composable
fun StoragePathScreenPreview() {
    val context = LocalContext.current
    StoragePathGetterTheme {
        StoragePathScreen(
            util = Utils(context),
            onOpenSettings = {}
        )
    }
}
