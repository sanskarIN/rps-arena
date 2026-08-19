package `in`.sanskar.rpsarena.data

data class BackupImportResult(
    val imported: Boolean,
    val message: String,
) {
    companion object {
        fun success(message: String = "Backup imported") = BackupImportResult(true, message)
        fun failure(message: String) = BackupImportResult(false, message)
    }
}
