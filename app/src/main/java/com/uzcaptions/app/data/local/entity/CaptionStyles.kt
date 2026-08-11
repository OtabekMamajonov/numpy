package com.uzcaptions.app.data.local.entity

enum class CaptionPosition { TOP, MIDDLE, BOTTOM }
enum class CaptionBackground { NONE, PILL, BOX }

data class CaptionStyle(
    val id: String,
    val displayName: String,
    val textColor: Long,
    val highlightColor: Long,
    val backgroundColor: Long?,
    val position: CaptionPosition,
    val uppercase: Boolean,
    val bold: Boolean,
    val background: CaptionBackground,
    val wordHighlight: Boolean
)

object CaptionStyles {
    const val DEFAULT_ID = "classic"

    val ALL: List<CaptionStyle> = listOf(
        CaptionStyle(
            id = "classic",
            displayName = "Klassik",
            textColor = 0xFFFFFFFF,
            highlightColor = 0xFFFFFFFF,
            backgroundColor = 0x99000000,
            position = CaptionPosition.BOTTOM,
            uppercase = false,
            bold = false,
            background = CaptionBackground.PILL,
            wordHighlight = false
        ),
        CaptionStyle(
            id = "bold_yellow",
            displayName = "Qalin sariq",
            textColor = 0xFFFFD700,
            highlightColor = 0xFFFFFFFF,
            backgroundColor = 0xE6000000,
            position = CaptionPosition.BOTTOM,
            uppercase = true,
            bold = true,
            background = CaptionBackground.BOX,
            wordHighlight = false
        ),
        CaptionStyle(
            id = "karaoke",
            displayName = "Karaoke (so'zma-so'z)",
            textColor = 0xFFFFFFFF,
            highlightColor = 0xFF00E676,
            backgroundColor = 0x99000000,
            position = CaptionPosition.MIDDLE,
            uppercase = false,
            bold = true,
            background = CaptionBackground.PILL,
            wordHighlight = true
        ),
        CaptionStyle(
            id = "minimal",
            displayName = "Minimal",
            textColor = 0xFFFFFFFF,
            highlightColor = 0xFFFFFFFF,
            backgroundColor = null,
            position = CaptionPosition.TOP,
            uppercase = false,
            bold = false,
            background = CaptionBackground.NONE,
            wordHighlight = false
        ),
        CaptionStyle(
            id = "neon",
            displayName = "Neon",
            textColor = 0xFF00E5FF,
            highlightColor = 0xFFFF4081,
            backgroundColor = 0xB3000000,
            position = CaptionPosition.BOTTOM,
            uppercase = true,
            bold = true,
            background = CaptionBackground.PILL,
            wordHighlight = true
        ),
        CaptionStyle(
            id = "soft_pink",
            displayName = "Yumshoq pushti",
            textColor = 0xFF212121,
            highlightColor = 0xFF212121,
            backgroundColor = 0xFFFCE4EC,
            position = CaptionPosition.BOTTOM,
            uppercase = false,
            bold = false,
            background = CaptionBackground.BOX,
            wordHighlight = false
        )
    )

    fun byId(id: String): CaptionStyle = ALL.firstOrNull { it.id == id } ?: ALL.first()
}
