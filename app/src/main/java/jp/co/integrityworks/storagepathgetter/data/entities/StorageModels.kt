package jp.co.integrityworks.storagepathgetter.data.entities

import android.net.Uri

/**
 * ストレージの内訳（画像、動画、アプリなど）を保持するデータクラス
 */
data class StorageBreakdown(
    val imageBytes: Long = 0L,
    val videoBytes: Long = 0L,
    val audioBytes: Long = 0L,
    val appBytes: Long = 0L,
    val otherBytes: Long = 0L,
    val totalBytes: Long = 0L
)

/**
 * ストレージ全体の状態を一括で管理するデータクラス
 */
data class StorageVolume(
    val path: String = "",
    val usageRatio: Float = 0f,
    val usageText: String = "",
    val breakdown: StorageBreakdown = StorageBreakdown()
)

/**
 * 最近更新されたファイルの情報を保持するデータクラス
 */
data class RecentFile(
    val name: String,
    val size: Long,
    val lastModified: Long,
    val uri: Uri
)
