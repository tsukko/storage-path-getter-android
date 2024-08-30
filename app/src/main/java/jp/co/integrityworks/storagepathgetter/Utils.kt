package jp.co.integrityworks.storagepathgetter

import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.Settings
import java.text.DecimalFormat
import kotlin.math.pow

data class AppInfo(
    val flags: Int,
    val name: String,
    val packageName: String,
    val size: Long,
    val installDate: Long,
    val lastUpdateDate: Long,
    val lastUsedDate: Long?
)

class Utils(context: Context) {
    private val mContext = context

    /**
     * ストレージのパスを取得する
     *
     */
    fun getPath(isExternal: Boolean): String {
        // Listで用意しているが、一つしかない前提で実装
        // かつ、外部と内部で2回for文を回しているのはいけてない
        val sdCardFilesDirPathList: MutableList<String> = arrayListOf()
        val dirArr = mContext.getExternalFilesDirs(null)

        for (dir in dirArr) {
            if (dir == null) {
                return "dir is null"
            }
            val parentFile = dir.parentFile ?: return "parentFile is null"
            val path = parentFile.parent ?: return "path is null"
            if (Environment.isExternalStorageRemovable(dir) && isExternal) {
                if (!sdCardFilesDirPathList.contains(path)) {
                    sdCardFilesDirPathList.add(path)
                    return path
                }
            } else {
                if (!isExternal) {
                    return path
                }
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

        return when (val storageSize = if (isTotal) {
            getTotalSize(path)
        } else {
            getAvailableSize(path)
        }) {
            in 0 until 1024 -> dfB.format(storageSize)
            in 1024 until 1024.0.pow(2.0).toInt() -> dfKb.format(storageSize / 1024)
            in 1024.0.pow(2.0).toInt() until 1024.0.pow(3.0).toInt()
            -> dfMb.format(storageSize / 1024.0.pow(2.0))
            else -> dfGb.format(storageSize / 1024.0.pow(3.0))
        }
    }

    // 総容量(トータルサイズ)を取得する
    private fun getTotalSize(path: String?): Long {
        val size: Long = -1

        if (path != null) {
            val fs = StatFs(path)
            return fs.totalBytes
        }
        return size
    }

    // 空き容量(利用可能)を取得する
    private fun getAvailableSize(path: String?): Long {
        val size: Long = -1

        if (path != null) {
            val fs = StatFs(path)
            return fs.availableBytes
        }
        return size
    }

    fun checkUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode =
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageStatsPermission(context: Context) {
        if (!checkUsageStatsPermission(context)) {
            AlertDialog.Builder(context)
                .setTitle("権限が必要です")
                .setMessage("アプリが動作するためには、使用状況アクセスの権限が必要です。権限を設定してください。")
                .setPositiveButton("設定を開く") { _, _ ->
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                }
                .setNegativeButton("キャンセル", null)
                .show()
        }
    }

    fun getAppStorageUsage(context: Context): List<AppInfo> {
        Logger.debug("getAppStorageUsage", "getAppStorageUsage()")

        val appList = mutableListOf<AppInfo>()
        val user = android.os.Process.myUserHandle();

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