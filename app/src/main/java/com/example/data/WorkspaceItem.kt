package com.example.data

data class WorkspaceItem(
    val page: Int,
    val row: Int,
    val col: Int,
    val packageName: String
) {
    fun toPrefString(): String = "$page:$row:$col:$packageName"

    companion object {
        fun fromPrefString(str: String): WorkspaceItem? {
            val parts = str.split(":", limit = 4)
            if (parts.size == 4) {
                return try {
                    WorkspaceItem(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3])
                } catch (e: Exception) { null }
            }
            return null
        }
    }
}
