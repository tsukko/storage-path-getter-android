package jp.co.integrityworks.storagepathgetter.util

import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import androidx.documentfile.provider.DocumentFile
import jp.co.integrityworks.storagepathgetter.data.entities.RecentFile
import jp.co.integrityworks.storagepathgetter.data.entities.StorageBreakdown
import java.text.DecimalFormat
import java.util.UUID
import kotlin.math.pow

class Utils(private val context: Context) {

    /**
     * ストレージのパスを取得する
     *
     */
    fun getPath(isExternal: Boolean): String {
        val dirArr = context.getExternalFilesDirs(null)

        for (dir in dirArr) {
            if (dir == null) continue
            val parentFile = dir.parentFile ?: continue
            val path = parentFile.parent ?: continue
            if (Environment.isExternalStorageRemovable(dir) && isExternal) {
                return path
            } else if (!Environment.isExternalStorageRemovable(dir) && !isExternal) {
                return path
            }
        }
        return ""
    }

    /**
     * 使用率（0.0 ~ 1.0）を取得する
     */
    fun getStorageUsageRatio(path: String): Float {
        if (path.isEmpty()) return 0f
        val total = getTotalSize(path)
        if (total <= 0) return 0f
        val available = getAvailableSize(path)
        return (total - available).toFloat() / total.toFloat()
    }

    /**
     * 使用量テキスト（例: 50GB / 128GB）を取得する
     */
    fun getStorageUsageText(path: String): String {
        if (path.isEmpty()) return ""
        val total = getTotalSize(path)
        val available = getAvailableSize(path)
        val used = total - available
        return "${getMemorySizeFromBytes(used)} / ${getMemorySizeFromBytes(total)}"
    }

    private fun getMemorySizeFromBytes(size: Long): String {
        val dfGb = DecimalFormat("#,###.## GB")
        val dfMb = DecimalFormat("#,###.## MB")
        return when {
            size >= 1024.0.pow(3.0) -> dfGb.format(size / 1024.0.pow(3.0))
            else -> dfMb.format(size / 1024.0.pow(2.0))
        }
    }

    /**
     * ストレージの内訳（画像、動画、アプリなど）を取得する
     */
    fun getStorageBreakdown(path: String): StorageBreakdown {
        if (path.isEmpty()) return StorageBreakdown()

        val storageStatsManager =
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        return try {
            val uuid: UUID = if (path.contains("emulated/0")) {
                StorageManager.UUID_DEFAULT
            } else {
                // 外部SDカードなどのUUIDを取得
                val volumes = storageManager.storageVolumes
                var foundUuid = StorageManager.UUID_DEFAULT
                for (volume in volumes) {
                    val volumePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        volume.directory?.absolutePath
                    } else {
                        // API 29用のフォールバック（リフレクションまたは簡易判定）
                        null
                    }
                    if (volumePath != null && path.startsWith(volumePath)) {
                        val uuidStr = volume.uuid
                        if (uuidStr != null) {
                            foundUuid = UUID.fromString(uuidStr.replace("-", ""))
                        }
                        break
                    }
                }
                foundUuid
            }

            val stats = storageStatsManager.queryExternalStatsForUser(
                uuid,
                android.os.Process.myUserHandle()
            )
            val total = getTotalSize(path)
            val available = getAvailableSize(path)
            val usedTotal = total - available

            // statsから取得できる各項目のサイズ
            // appsは別途取得した方が正確な場合があるが、ここでは簡易的に計算
            StorageBreakdown(
                imageBytes = stats.imageBytes,
                videoBytes = stats.videoBytes,
                audioBytes = stats.audioBytes,
                appBytes = stats.appBytes,
                totalBytes = total,
                otherBytes = (usedTotal - (stats.imageBytes + stats.videoBytes + stats.audioBytes + stats.appBytes)).coerceAtLeast(
                    0
                )
            )
        } catch (e: Exception) {
            Logger.error("Utils", "Error getting storage breakdown", e)
            StorageBreakdown()
        }
    }

    /**
     * 指定されたディレクトリから直近24時間以内に更新されたファイルを取得する
     */
    fun getRecentFiles(directoryUri: Uri): List<RecentFile> {
        val recentFiles = mutableListOf<RecentFile>()
        val root = DocumentFile.fromTreeUri(context, directoryUri) ?: return emptyList()

        val now = System.currentTimeMillis()
        val twentyFourHoursAgo = now - (24 * 60 * 60 * 1000L)

        // 再帰的に探索せず、直下のファイルのみを対象とする
        root.listFiles().forEach { file ->
            if (file.isFile && file.lastModified() >= twentyFourHoursAgo) {
                recentFiles.add(
                    RecentFile(
                        name = file.name ?: "Unknown",
                        size = file.length(),
                        lastModified = file.lastModified(),
                        uri = file.uri
                    )
                )
            }
        }

        return recentFiles.sortedByDescending { it.lastModified }
    }

    // 総容量(トータルサイズ)を取得する
    private fun getTotalSize(path: String?): Long {
        if (path != null) {
            val fs = StatFs(path)
            return fs.totalBytes
        }
        return -1
    }

    // 空き容量(利用可能)を取得する
    private fun getAvailableSize(path: String?): Long {
        if (path != null) {
            val fs = StatFs(path)
            return fs.availableBytes
        }
        return -1
    }

    /**
     * 使用状況アクセスの権限があるか確認する
     */
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
