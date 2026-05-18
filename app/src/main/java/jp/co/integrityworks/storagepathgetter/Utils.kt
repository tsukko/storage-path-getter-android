package jp.co.integrityworks.storagepathgetter

import android.app.AppOpsManager
import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import jp.co.integrityworks.storagepathgetter.model.AppInfo
import java.text.DecimalFormat
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

        val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
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
                    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                    val lastUsedDate = usageStatsManager.queryAndAggregateUsageStats(
                        System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 7,  // 1週間分のデータを取得
                        System.currentTimeMillis()
                    )[packageInfo.packageName]?.lastTimeUsed

                    appList.add(AppInfo(
                        applicationInfo.flags,
                        appName,
                        packageInfo.packageName,
                        totalBytes,
                        installDate,
                        lastUpdateDate,
                        lastUsedDate
                    ))
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
