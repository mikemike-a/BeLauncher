package com.example.data

data class WorkspaceItem(
    val page: Int,
    val row: Int,
    val col: Int,
    val identifier: String, // packageName for app, widgetId for widget
    val isWidget: Boolean = false
) {
    fun toPrefString(): String = "$page:$row:$col:$identifier:$isWidget"

    companion object {
        fun fromPrefString(str: String): WorkspaceItem? {
            val parts = str.split(":", limit = 5)
            if (parts.size >= 4) {
                return try {
                    WorkspaceItem(
                        page = parts[0].toInt(),
                        row = parts[1].toInt(),
                        col = parts[2].toInt(),
                        identifier = parts[3],
                        isWidget = if (parts.size == 5) parts[4].toBoolean() else false
                    )
                } catch (e: Exception) { null }
            }
            return null
        }
    }
}
