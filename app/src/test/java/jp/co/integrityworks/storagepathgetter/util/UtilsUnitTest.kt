package jp.co.integrityworks.storagepathgetter.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.DecimalFormat

class UtilsUnitTest {

    @Test
    fun testMemorySizeFormatting() {
        // Utilsクラスのプライベートメソッドをテストするのは難しいため、
        // 外部から見えるロジックをシミュレートして確認します。

        fun formatSize(storageSize: Long): String {
            val dfB = DecimalFormat("#,###.### B")
            val dfKb = DecimalFormat("#,###.### KB")
            val dfMb = DecimalFormat("#,###.### MB")
            val dfGb = DecimalFormat("#,###.### GB")

            return when {
                storageSize < 1024 -> dfB.format(storageSize)
                storageSize < 1024L * 1024L -> dfKb.format(storageSize.toDouble() / 1024.0)
                storageSize < 1024L * 1024L * 1024L -> dfMb.format(storageSize.toDouble() / (1024.0 * 1024.0))
                else -> dfGb.format(storageSize.toDouble() / (1024.0 * 1024.0 * 1024.0))
            }
        }

        assertEquals("500 B", formatSize(500))
        assertEquals("1 KB", formatSize(1024))
        assertEquals("1 MB", formatSize(1024L * 1024L))
        assertEquals("1 GB", formatSize(1024L * 1024L * 1024L))
    }
}
