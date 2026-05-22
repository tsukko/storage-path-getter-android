package jp.co.integrityworks.storagepathgetter.data.entities

data class StorageBreakdown(
    val imageBytes: Long = 0L,
    val videoBytes: Long = 0L,
    val audioBytes: Long = 0L,
    val appBytes: Long = 0L,
    val otherBytes: Long = 0L,
    val totalBytes: Long = 0L
)
