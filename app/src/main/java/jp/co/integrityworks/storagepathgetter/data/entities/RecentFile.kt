package jp.co.integrityworks.storagepathgetter.data.entities

import android.net.Uri

data class RecentFile(
    val name: String,
    val size: Long,
    val lastModified: Long,
    val uri: Uri
)
