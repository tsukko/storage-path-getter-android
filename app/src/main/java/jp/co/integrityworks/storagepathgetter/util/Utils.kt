package jp.co.integrityworks.storagepathgetter.util

import android.app.AppOpsManager
import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import androidx.documentfile.provider.DocumentFile
import jp.co.integrityworks.storagepathgetter.data.entities.AppInfo
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

    fun getMemoryInformation(iPath: String, ePath: String): String {
        return """
           |内部メモリーの利用可能容量:${"\t" + getMemorySize(iPath)}
           |内部メモリーの総容量:${"\t\t\t\t" + getMemorySize(iPath, true)}
           |外部メモリーの利用可能容量:${"\t" + getMemorySize(ePath)}
           |外部メモリーの総容量:${"\t\t\t\t" + getMemorySize(ePath, true)}
           """.trimMargin()
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

        val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
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

            val stats = storageStatsManager.queryExternalStatsForUser(uuid, android.os.Process.myUserHandle())
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
                otherBytes = (usedTotal - (stats.imageBytes + stats.videoBytes + stats.audioBytes + stats.appBytes)).coerceAtLeast(0)
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

    private fun getMemorySize(path: String): String {
        return getMemorySize(path, false)
    }

    private fun getMemorySize(path: String, isTotal: Boolean): String {
        if (path.isEmpty()) {
            return "---"
        }

        val dfB = DecimalFormat("#,###.### B")
        val dfKb = DecimalFormat("#,###.### KB")
        val dfMb = DecimalFormat("#,###.### MB")
        val dfGb = DecimalFormat("#,###.### GB")

        val storageSize = if (isTotal) {
            getTotalSize(path)
        } else {
            getAvailableSize(path)
        }

        return when (storageSize) {
            in 0 until 1024 -> dfB.format(storageSize)
            in 1024 until 1024.0.pow(2.0).toLong() -> dfKb.format(storageSize / 1024)
            in 1024.0.pow(2.0).toLong() until 1024.0.pow(3.0).toLong()
                -> dfMb.format(storageSize / 1024.0.pow(2.0))

            else -> dfGb.format(storageSize / 1024.0.pow(3.0))
        }
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

    fun getAppStorageUsage(): List<AppInfo> {
        Logger.debug("getAppStorageUsage", "getAppStorageUsage()")

        val appList = mutableListOf<AppInfo>()
        val user = android.os.Process.myUserHandle()

        val storageStatsManager =
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        val packageManager = context.packageManager
        val packageList = packageManager.getInstalledPackages(0)

        packageList.forEach { packageInfo ->
            val applicationInfo = packageInfo.applicationInfo

            // ユーザーがインストールしたアプリのみを対象にする
            if ((applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM == 0) {
                try {
                    val storageStats: StorageStats = storageStatsManager.queryStatsForPackage(
                        StorageManager.UUID_DEFAULT,
                        packageInfo.packageName,
                        user
                    )

                    val totalBytes =
                        storageStats.dataBytes + storageStats.cacheBytes + storageStats.appBytes
                    val appName = packageManager.getApplicationLabel(applicationInfo!!).toString()

                    // インストール日と最終更新日
                    val installDate = packageInfo.firstInstallTime
                    val lastUpdateDate = packageInfo.lastUpdateTime

                    // 最終起動日 (Android 10 以降)
                    val usageStatsManager =
                        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                    val lastUsedDate = usageStatsManager.queryAndAggregateUsageStats(
                        System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 7,  // 1週間分のデータを取得
                        System.currentTimeMillis()
                    )[packageInfo.packageName]?.lastTimeUsed

                    appList.add(
                        AppInfo(
                            applicationInfo.flags,
                            appName,
                            packageInfo.packageName,
                            totalBytes,
                            installDate,
                            lastUpdateDate,
                            lastUsedDate
                        )
                    )
                } catch (e: Exception) {
                    Logger.error(
                        "StorageUsage",
                        "Error retrieving storage info for package: ${packageInfo.packageName}",
                        e
                    )
                }
            }
        }

        return appList
    }
}
