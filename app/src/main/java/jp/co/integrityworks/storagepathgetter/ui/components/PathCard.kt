package jp.co.integrityworks.storagepathgetter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.co.integrityworks.storagepathgetter.R
import jp.co.integrityworks.storagepathgetter.data.entities.StorageBreakdown
import jp.co.integrityworks.storagepathgetter.ui.theme.Dimens
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun PathCard(
    title: String,
    path: String,
    icon: ImageVector,
    usage: Float = 0f,
    usageText: String = "",
    breakdown: StorageBreakdown = StorageBreakdown(),
    onCopy: (String) -> Unit,
    onOpen: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val usagePercent = (usage * 100).roundToInt()
    val isCritical = usage > 0.9f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.RadiusExtraLarge),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.MarginXLarge)) {
            // ヘッダー部分
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isCritical) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(Dimens.IconLarge)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isCritical) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.IconNormal)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(Dimens.MarginLarge))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (usage > 0) stringResource(id = R.string.label_usage_percent, usagePercent) else stringResource(id = R.string.label_usage_info_none),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }

                IconButton(onClick = { onCopy(path) }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(id = R.string.text_clear),
                        modifier = Modifier.size(Dimens.IconSmall),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (path.isNotEmpty()) {
                    IconButton(onClick = { onOpen(path) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.IconSmall),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // グラフ部分
            if (usage > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(id = R.string.label_usage),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = usageText,
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                LinearProgressIndicator(
                    progress = { usage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ProgressBarHeight)
                        .clip(CircleShape),
                    color = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // パス部分
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.RadiusMedium))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable { onCopy(path) }
                    .padding(horizontal = Dimens.MarginMedium, vertical = 10.dp)
            ) {
                Text(
                    text = path.ifEmpty { stringResource(id = R.string.label_no_path) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 内訳表示（アコーディオン）
            if (usage > 0) {
                Spacer(modifier = Modifier.height(Dimens.MarginLarge))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.RadiusSmall))
                        .clickable { expanded = !expanded }
                        .padding(vertical = Dimens.MarginSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (expanded) stringResource(id = R.string.action_hide_breakdown) else stringResource(id = R.string.action_show_breakdown),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.IconSmall)
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = Dimens.MarginLarge)) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = Dimens.MarginLarge),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        
                        BreakdownRow(stringResource(id = R.string.category_image), breakdown.imageBytes, MaterialTheme.colorScheme.primary)
                        BreakdownRow(stringResource(id = R.string.category_video), breakdown.videoBytes, MaterialTheme.colorScheme.secondary)
                        BreakdownRow(stringResource(id = R.string.category_audio), breakdown.audioBytes, MaterialTheme.colorScheme.tertiary)
                        BreakdownRow(stringResource(id = R.string.category_apps), breakdown.appBytes, MaterialTheme.colorScheme.error)
                        BreakdownRow(stringResource(id = R.string.category_other), breakdown.otherBytes, MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, bytes: Long, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.MarginSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(Dimens.MarginLarge))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = formatBytes(bytes),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
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
