package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

enum class AkoIconShape(val title: String, val description: String) {
    SQUIRCLE("Classique", "Carré aux bords arrondis standards"),
    CAURI("Cauri", "Forme organique inspirée des coquillages cauris"),
    ASASE("Asase", "Forme asymétrique de feuille/terre"),
    SHIELD("Bouclier", "Forme géométrique de protection")
}

fun getShapeFor(akoIconShape: AkoIconShape): Shape {
    return when (akoIconShape) {
        AkoIconShape.SQUIRCLE -> RoundedCornerShape(20.dp)
        AkoIconShape.CAURI -> CauriShape()
        AkoIconShape.ASASE -> AsaseShape()
        AkoIconShape.SHIELD -> ShieldShape()
    }
}

class CauriShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val w = size.width
            val h = size.height
            // Cauri shape: narrower top, wider bottom, smooth organic curves
            moveTo(w * 0.5f, h * 0.05f)
            cubicTo(w * 0.85f, h * 0.1f, w * 0.95f, h * 0.6f, w * 0.8f, h * 0.85f)
            cubicTo(w * 0.65f, h * 1.05f, w * 0.35f, h * 1.05f, w * 0.2f, h * 0.85f)
            cubicTo(w * 0.05f, h * 0.6f, w * 0.15f, h * 0.1f, w * 0.5f, h * 0.05f)
            close()
        }
        return Outline.Generic(path)
    }
}

class AsaseShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val w = size.width
            val h = size.height
            // Asymmetrical organic leaf shape
            moveTo(w * 0.15f, h * 0.15f)
            cubicTo(w * 0.5f, h * 0.05f, w * 0.8f, h * 0.1f, w * 0.85f, h * 0.3f)
            cubicTo(w * 0.9f, h * 0.6f, w * 0.95f, h * 0.85f, w * 0.7f, h * 0.85f)
            cubicTo(w * 0.3f, h * 0.85f, w * 0.1f, h * 0.8f, w * 0.05f, h * 0.6f)
            cubicTo(0f, h * 0.3f, w * 0.05f, h * 0.2f, w * 0.15f, h * 0.15f)
            close()
        }
        return Outline.Generic(path)
    }
}

class ShieldShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val w = size.width
            val h = size.height
            // African shield inspired: flat top, slight bulge sides, pointed bottom
            moveTo(w * 0.15f, h * 0.1f)
            quadraticTo(w * 0.5f, 0f, w * 0.85f, h * 0.1f)
            cubicTo(w, h * 0.3f, w, h * 0.6f, w * 0.5f, h * 0.95f)
            cubicTo(0f, h * 0.6f, 0f, h * 0.3f, w * 0.15f, h * 0.1f)
            close()
        }
        return Outline.Generic(path)
    }
}
