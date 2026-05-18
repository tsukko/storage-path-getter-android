package jp.co.integrityworks.storagepathgetter.model

data class AppInfo(
    val flags: Int,
    val name: String,
    val packageName: String,
    val size: Long,
    val installDate: Long,
    val lastUpdateDate: Long,
    val lastUsedDate: Long?
)
