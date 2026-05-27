package jp.co.integrityworks.storagepathgetter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.co.integrityworks.storagepathgetter.R
import jp.co.integrityworks.storagepathgetter.data.entities.StorageBreakdown
import jp.co.integrityworks.storagepathgetter.ui.theme.Dimens
import jp.co.integrityworks.storagepathgetter.util.Utils
import kotlin.math.roundToInt

@Composable
fun PathCard(
    util: Utils,
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
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationSmall)
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
                }

                if (statusEmoji.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = statusEmoji, fontSize = 20.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.MarginLarge))

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
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.MarginMiddle))

                LinearProgressIndicator(
                    progress = { usage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ProgressBarHeight)
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

            // パス部分の改善（安心感優先：ボタン化）
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

                if (path.isEmpty()) {
                    // パスがない（SDカード未挿入など）時の親切な表示
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.RadiusMedium))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                            .padding(Dimens.MarginLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (title.contains("SD") || title.contains("SDカード"))
                                stringResource(id = R.string.msg_sd_card_not_found)
                            else stringResource(id = R.string.label_no_path),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Surface(
                        onClick = { onCopy(path) },
                        shape = RoundedCornerShape(Dimens.RadiusMedium),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimens.MarginLarge),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = path,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = Dimens.TextSizeXXSmall
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(Dimens.MarginSmall))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(id = R.string.action_copy_path),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // アクションの階層化：フォルダを開くボタンを OutlinedButton へ
            if (path.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.MarginMiddle))
                OutlinedButton(
                    onClick = { onOpen(path) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.RadiusMedium)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.action_view_folder),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // 内訳表示（さらに控えめに）
            if (usage > 0) {
                Spacer(modifier = Modifier.height(Dimens.MarginLarge))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.RadiusSmall))
                        .clickable { expanded = !expanded }
                        .padding(vertical = Dimens.MarginMiddle),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (expanded) stringResource(id = R.string.action_hide_breakdown) else stringResource(
                                id = R.string.action_show_breakdown
                            ),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
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
                            util,
                            stringResource(id = R.string.category_image),
                            breakdown.imageBytes,
                            MaterialTheme.colorScheme.primary
                        )
                        BreakdownRow(
                            util,
                            stringResource(id = R.string.category_video),
                            breakdown.videoBytes,
                            MaterialTheme.colorScheme.secondary
                        )
                        BreakdownRow(
                            util,
                            stringResource(id = R.string.category_audio),
                            breakdown.audioBytes,
                            MaterialTheme.colorScheme.tertiary
                        )
                        BreakdownRow(
                            util,
                            stringResource(id = R.string.category_apps),
                            breakdown.appBytes,
                            MaterialTheme.colorScheme.error
                        )
                        BreakdownRow(
                            util,
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
private fun BreakdownRow(util: Utils, label: String, bytes: Long, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.MarginMiddle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.MarginMiddle + Dimens.MarginSmall / 2)
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
            text = util.formatBytes(bytes),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
