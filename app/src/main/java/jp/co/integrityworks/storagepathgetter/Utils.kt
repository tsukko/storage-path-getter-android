package jp.co.integrityworks.storagepathgetter

import android.content.Context
import android.os.Environment
import android.os.StatFs
import java.text.DecimalFormat
import kotlin.math.pow

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
            if (dir != null) {
                val path = dir.parentFile.parent
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
}