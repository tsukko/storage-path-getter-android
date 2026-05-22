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
    val isWarning = usage > 0.7f

    // 状況に応じたメッセージとアイコン
    val statusEmoji = when {
        usage <= 0f -> ""
        isCritical -> "😱"
        isWarning -> "😮"
        else -> "😊"
    }

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
                    color = when {
                        isCritical -> MaterialTheme.colorScheme.errorContainer
                        isWarning -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    },
                    modifier = Modifier.size(Dimens.IconLarge)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = when {
                                isCritical -> MaterialTheme.colorScheme.onErrorContainer
                                isWarning -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.size(Dimens.IconNormal)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Dimens.MarginLarge))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (usage > 0) stringResource(
                                id = R.string.label_usage_percent,
                                usagePercent
                            ) else stringResource(id = R.string.label_usage_info_none),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = when {
                                isCritical -> MaterialTheme.colorScheme.error
                                isWarning -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        if (statusEmoji.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(Dimens.MarginSmall))
                            Text(text = statusEmoji, fontSize = Dimens.TextSizeNormal)
                        }
                    }
                }

                if (path.isNotEmpty()) {
                    Row {
                        IconButton(onClick = { onCopy(path) }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(id = R.string.text_clear),
                                modifier = Modifier.size(Dimens.IconSmall),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = { onOpen(path) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.IconSmall),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.RadiusLarge))

            // グラフ部分
            if (usage > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(id = R.string.label_usage),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = usageText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum" // 数字を等幅にして読みやすく
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.MarginMiddle))

                LinearProgressIndicator(
                    progress = { usage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ProgressBarHeight + 2.dp) // ほんの少しだけ太くして見やすく
                        .clip(CircleShape),
                    color = when {
                        isCritical -> MaterialTheme.colorScheme.error
                        isWarning -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(Dimens.MarginXLarge))

            // パス部分（「保存場所」というラベルを付けて分かりやすく）
            Column {
                Text(
                    text = stringResource(id = R.string.label_path_prefix),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(
                        start = Dimens.MarginSmall,
                        bottom = Dimens.MarginSmall
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.RadiusMedium))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .then(if (path.isNotEmpty()) Modifier.clickable { onCopy(path) } else Modifier)
                        .padding(horizontal = Dimens.MarginLarge, vertical = Dimens.MarginMedium)
                ) {
                    Text(
                        text = path.ifEmpty { stringResource(id = R.string.label_no_path) },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, // ここだけ少しエンジニアのこだわり（等幅）
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 内訳表示（アコーディオン）
            if (usage > 0) {
                Spacer(modifier = Modifier.height(Dimens.MarginLarge))

                Surface(
                    onClick = { expanded = !expanded },
                    shape = RoundedCornerShape(Dimens.RadiusSmall),
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = Dimens.MarginMiddle),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (expanded) stringResource(id = R.string.action_hide_breakdown) else stringResource(
                                id = R.string.action_show_breakdown
                            ),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.IconNormal)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = Dimens.MarginMiddle)
                            .clip(RoundedCornerShape(Dimens.RadiusMedium))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .padding(Dimens.MarginLarge)
                    ) {
                        BreakdownRow(
                            stringResource(id = R.string.category_image),
                            breakdown.imageBytes,
                            MaterialTheme.colorScheme.primary
                        )
                        BreakdownRow(
                            stringResource(id = R.string.category_video),
                            breakdown.videoBytes,
                            MaterialTheme.colorScheme.secondary
                        )
                        BreakdownRow(
                            stringResource(id = R.string.category_audio),
                            breakdown.audioBytes,
                            MaterialTheme.colorScheme.tertiary
                        )
                        BreakdownRow(
                            stringResource(id = R.string.category_apps),
                            breakdown.appBytes,
                            MaterialTheme.colorScheme.error
                        )
                        BreakdownRow(
                            stringResource(id = R.string.category_other),
                            breakdown.otherBytes,
                            MaterialTheme.colorScheme.outline
                        )
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
            .padding(vertical = Dimens.MarginSmall + 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(Dimens.MarginMedium))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatBytes(bytes),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            ),
            color = MaterialTheme.colorScheme.onSurface
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
