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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import jp.co.integrityworks.storagepathgetter.R
import jp.co.integrityworks.storagepathgetter.data.entities.RecentFile
import jp.co.integrityworks.storagepathgetter.data.entities.StorageVolume
import jp.co.integrityworks.storagepathgetter.ui.components.AdBanner
import jp.co.integrityworks.storagepathgetter.ui.components.PathCard
import jp.co.integrityworks.storagepathgetter.ui.components.RecentFilesCard
import jp.co.integrityworks.storagepathgetter.ui.theme.Dimens
import jp.co.integrityworks.storagepathgetter.ui.theme.StoragePathGetterTheme
import jp.co.integrityworks.storagepathgetter.util.Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoragePathScreen(util: Utils, onRequestPermission: () -> Unit) {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current

    // ストレージ情報を一括管理
    var internalVolume by remember { 
        mutableStateOf(StorageVolume(path = if (isInspection) "/storage/emulated/0" else "")) 
    }
    var externalVolume by remember { 
        mutableStateOf(StorageVolume(path = if (isInspection) "/storage/1234-5678" else "")) 
    }

    var recentFiles by remember { mutableStateOf<List<RecentFile>>(emptyList()) }
    var selectedFolderUri by remember { mutableStateOf<Uri?>(null) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedFolderUri = uri
            recentFiles = util.getRecentFiles(uri)
        }
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
                Toast.makeText(context, noAppFoundMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val loadData = {
        if (util.hasUsageStatsPermission()) {
            val iPath = util.getPath(isExternal = false)
            val ePath = util.getPath(isExternal = true)

            // 内部ストレージ情報の更新
            internalVolume = StorageVolume(
                path = iPath,
                usageRatio = util.getStorageUsageRatio(path = iPath),
                usageText = util.getStorageUsageText(path = iPath),
                breakdown = util.getStorageBreakdown(path = iPath)
            )

            // 外部ストレージ情報の更新
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
            onRequestPermission()
        }
    }

    LaunchedEffect(Unit) {
        if (!isInspection) {
            loadData()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
            verticalArrangement = Arrangement.spacedBy(Dimens.MarginXLarge)
        ) {
            StatusMessageCard(internalVolume.usageRatio)

            PathCard(
                util = util,
                title = stringResource(id = R.string.text_inner_path),
                path = internalVolume.path,
                icon = Icons.Default.Smartphone,
                usage = internalVolume.usageRatio,
                usageText = internalVolume.usageText,
                breakdown = internalVolume.breakdown,
                onCopy = { copyToClipboard(context, innerPathLabel, it) },
                onOpen = { openFolder(it) }
            )

            PathCard(
                util = util,
                title = stringResource(id = R.string.text_external_path),
                path = externalVolume.path,
                icon = Icons.Default.SdStorage,
                usage = externalVolume.usageRatio,
                usageText = externalVolume.usageText,
                breakdown = externalVolume.breakdown,
                onCopy = { copyToClipboard(context, externalPathLabel, it) },
                onOpen = { openFolder(it) }
            )

            RecentFilesCard(
                util = util,
                files = recentFiles,
                onSelectFolder = { folderPickerLauncher.launch(null) }
            )

            Spacer(modifier = Modifier.height(Dimens.MarginMiddle))
        }
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
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(Dimens.RadiusMedium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Dimens.MarginLarge),
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

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(
        context,
        context.getString(R.string.msg_copied, label, text),
        Toast.LENGTH_LONG
    ).show()
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
