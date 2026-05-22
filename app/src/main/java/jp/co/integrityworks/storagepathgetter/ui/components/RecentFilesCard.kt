package jp.co.integrityworks.storagepathgetter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jp.co.integrityworks.storagepathgetter.R
import jp.co.integrityworks.storagepathgetter.data.entities.RecentFile
import jp.co.integrityworks.storagepathgetter.ui.theme.Dimens
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.pow

@Composable
fun RecentFilesCard(
    files: List<RecentFile>,
    onSelectFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.RadiusExtraLarge),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.MarginXLarge)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.IconNormal)
                )
                Spacer(modifier = Modifier.width(Dimens.MarginLarge))
                Text(
                    text = stringResource(id = R.string.title_recent_files),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.MarginLarge))

            if (files.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.msg_select_folder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Dimens.MarginLarge))
                    Button(onClick = onSelectFolder) {
                        Text(stringResource(id = R.string.action_select_folder))
                    }
                }
            } else {
                files.take(5).forEach { file ->
                    RecentFileItem(file)
                    Spacer(modifier = Modifier.height(Dimens.MarginMiddle))
                }

                if (files.size > 5) {
                    Text(
                        text = stringResource(id = R.string.label_other_files, files.size - 5),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = Dimens.MarginSmall)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentFileItem(file: RecentFile) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusMedium))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(Dimens.MarginMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
            contentDescription = null,
            modifier = Modifier.size(Dimens.IconSmall),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(Dimens.MarginLarge))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row {
                Text(
                    text = formatBytes(file.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(Dimens.MarginMiddle))
                Text(
                    text = SimpleDateFormat(
                        "HH:mm",
                        locale
                    ).format(Date(file.lastModified)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val dfGb = DecimalFormat("#,###.## GB")
    val dfMb = DecimalFormat("#,###.## MB")
    val dfKb = DecimalFormat("#,###.## KB")

    return when {
        bytes >= 1024.0.pow(3.0) -> dfGb.format(bytes / 1024.0.pow(3.0))
        bytes >= 1024.0.pow(2.0) -> dfMb.format(bytes / 1024.0.pow(2.0))
        else -> dfKb.format(bytes / 1024.0)
    }
}
