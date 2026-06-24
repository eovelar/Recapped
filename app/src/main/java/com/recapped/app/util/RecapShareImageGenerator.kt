package com.recapped.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.recapped.app.R
import com.recapped.app.domain.model.RecapResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RecapShareImageGenerator {

    private const val WIDTH = 1080
    private const val HEIGHT = 1920
    private const val SIDE_PADDING = 72f

    suspend fun generate(
        context: Context,
        recap: RecapResult
    ): File = withContext(Dispatchers.IO) {
        val bitmap = Bitmap.createBitmap(
            WIDTH,
            HEIGHT,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = ResourcesCompat.getFont(
                context,
                R.font.unbounded_extrabold
            )
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = ResourcesCompat.getFont(
                context,
                R.font.syne_semibold
            )
        }

        drawBackground(canvas)
        drawHeader(canvas, recap, titlePaint, textPaint)

        val headlineBottom = drawHeadline(
            canvas = canvas,
            headline = recap.aiHeadline,
            paint = textPaint
        )

        val statisticsBottom = drawStatistics(
            canvas = canvas,
            recap = recap,
            top = headlineBottom + 60f,
            titlePaint = titlePaint,
            textPaint = textPaint
        )

        val artistsBottom = drawTopArtists(
            context = context,
            canvas = canvas,
            recap = recap,
            sectionTop = statisticsBottom + 60f,
            titlePaint = titlePaint,
            textPaint = textPaint
        )

        val tracksBottom = drawTopTracks(
            canvas = canvas,
            recap = recap,
            sectionTop = artistsBottom + 65f,
            titlePaint = titlePaint,
            textPaint = textPaint
        )

        drawGenres(
            canvas = canvas,
            recap = recap,
            sectionTop = tracksBottom + 65f,
            titlePaint = titlePaint,
            textPaint = textPaint
        )

        drawFooter(canvas, titlePaint, textPaint)

        val directory = File(
            context.cacheDir,
            "shared_recaps"
        ).apply {
            mkdirs()
        }

        val outputFile = File(
            directory,
            "recapped_${System.currentTimeMillis()}.jpg"
        )

        FileOutputStream(outputFile).use { output ->
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                94,
                output
            )
        }

        bitmap.recycle()
        outputFile
    }

    private fun drawBackground(
        canvas: Canvas
    ) {
        canvas.drawColor(Color.BLACK)

        val backgroundGradient = LinearGradient(
            0f,
            0f,
            WIDTH.toFloat(),
            HEIGHT.toFloat(),
            intArrayOf(
                Color.rgb(35, 5, 0),
                Color.BLACK,
                Color.rgb(16, 4, 22)
            ),
            floatArrayOf(0f, 0.52f, 1f),
            Shader.TileMode.CLAMP
        )

        canvas.drawRect(
            0f,
            0f,
            WIDTH.toFloat(),
            HEIGHT.toFloat(),
            Paint().apply {
                shader = backgroundGradient
            }
        )

        val orangeGlow = RadialGradient(
            140f,
            260f,
            620f,
            intArrayOf(
                Color.argb(125, 255, 55, 0),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )

        canvas.drawCircle(
            140f,
            260f,
            620f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = orangeGlow
            }
        )

        val purpleGlow = RadialGradient(
            WIDTH.toFloat(),
            1280f,
            600f,
            intArrayOf(
                Color.argb(70, 110, 30, 255),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )

        canvas.drawCircle(
            WIDTH.toFloat(),
            1280f,
            600f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = purpleGlow
            }
        )
    }

    private fun drawHeader(
        canvas: Canvas,
        recap: RecapResult,
        titlePaint: Paint,
        textPaint: Paint
    ) {
        titlePaint.apply {
            color = Color.WHITE
            textSize = 47f
            textAlign = Paint.Align.LEFT
        }

        canvas.drawText(
            "RECAP",
            SIDE_PADDING,
            100f,
            titlePaint
        )

        textPaint.apply {
            color = Color.argb(170, 255, 255, 255)
            textSize = 25f
            textAlign = Paint.Align.RIGHT
        }

        canvas.drawText(
            formatPeriod(recap),
            WIDTH - SIDE_PADDING,
            96f,
            textPaint
        )

        val lineGradient = LinearGradient(
            SIDE_PADDING,
            0f,
            WIDTH - SIDE_PADDING,
            0f,
            intArrayOf(
                Color.rgb(255, 45, 0),
                Color.rgb(255, 195, 0),
                Color.rgb(122, 61, 255)
            ),
            null,
            Shader.TileMode.CLAMP
        )

        canvas.drawLine(
            SIDE_PADDING,
            130f,
            WIDTH - SIDE_PADDING,
            130f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = lineGradient
                strokeWidth = 5f
            }
        )

        textPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawHeadline(
        canvas: Canvas,
        headline: String,
        paint: Paint
    ): Float {
        paint.apply {
            color = Color.WHITE
            textSize = 56f
            textAlign = Paint.Align.LEFT
        }

        val startY = 220f
        val lineHeight = 68f

        val lines = createWrappedLines(
            text = headline,
            maxWidth = WIDTH - 190f,
            maxLines = 3,
            paint = paint
        )

        canvas.drawRoundRect(
            RectF(
                SIDE_PADDING,
                startY - 48f,
                SIDE_PADDING + 8f,
                startY + ((lines.size - 1) * lineHeight) + 12f
            ),
            4f,
            4f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(255, 75, 0)
            }
        )

        lines.forEachIndexed { index, line ->
            canvas.drawText(
                line,
                SIDE_PADDING + 28f,
                startY + index * lineHeight,
                paint
            )
        }

        return startY + ((lines.size - 1) * lineHeight)
    }

    private fun drawStatistics(
        canvas: Canvas,
        recap: RecapResult,
        top: Float,
        titlePaint: Paint,
        textPaint: Paint
    ): Float {
        val cardWidth = 286f
        val cardHeight = 170f
        val gap = 20f

        val statistics = listOf(
            recap.totalScrobbles.toString() to "REPRODUCCIONES",
            recap.uniqueArtists.toString() to "ARTISTAS",
            recap.uniqueTracks.toString() to "CANCIONES"
        )

        statistics.forEachIndexed { index, statistic ->
            val left = SIDE_PADDING + index * (cardWidth + gap)

            val rect = RectF(
                left,
                top,
                left + cardWidth,
                top + cardHeight
            )

            canvas.drawRoundRect(
                rect,
                28f,
                28f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(190, 22, 22, 22)
                }
            )

            canvas.drawRoundRect(
                rect,
                28f,
                28f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    color = if (index == 0) {
                        Color.argb(125, 255, 80, 0)
                    } else {
                        Color.argb(40, 255, 255, 255)
                    }
                }
            )

            titlePaint.apply {
                color = Color.WHITE
                textSize = if (statistic.first.length > 5) {
                    39f
                } else {
                    48f
                }
                textAlign = Paint.Align.CENTER
            }

            canvas.drawText(
                statistic.first,
                rect.centerX(),
                top + 76f,
                titlePaint
            )

            textPaint.apply {
                color = Color.argb(120, 255, 255, 255)
                textSize = 17f
                textAlign = Paint.Align.CENTER
            }

            canvas.drawText(
                statistic.second,
                rect.centerX(),
                top + 123f,
                textPaint
            )
        }

        titlePaint.textAlign = Paint.Align.LEFT
        textPaint.textAlign = Paint.Align.LEFT

        return top + cardHeight
    }

    private fun drawTopArtists(
        context: Context,
        canvas: Canvas,
        recap: RecapResult,
        sectionTop: Float,
        titlePaint: Paint,
        textPaint: Paint
    ): Float {
        drawSectionTitle(
            canvas = canvas,
            title = "TUS ARTISTAS",
            y = sectionTop,
            paint = textPaint
        )

        val imageTop = sectionTop + 35f
        val imageSize = 286f
        val gap = 20f

        recap.topArtists.take(3).forEachIndexed { index, artist ->
            val left = SIDE_PADDING + index * (imageSize + gap)

            val destination = RectF(
                left,
                imageTop,
                left + imageSize,
                imageTop + imageSize
            )

            val artistBitmap = loadBitmap(
                context = context,
                url = artist.imageUrl
            )

            if (artistBitmap != null) {
                drawRoundedBitmap(
                    canvas = canvas,
                    bitmap = artistBitmap,
                    destination = destination,
                    radius = 30f
                )
            } else {
                drawImagePlaceholder(
                    canvas = canvas,
                    rect = destination,
                    rank = artist.rank,
                    paint = titlePaint
                )
            }

            val overlay = LinearGradient(
                0f,
                imageTop + 120f,
                0f,
                imageTop + imageSize,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.argb(235, 0, 0, 0)
                ),
                null,
                Shader.TileMode.CLAMP
            )

            canvas.drawRoundRect(
                destination,
                30f,
                30f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = overlay
                }
            )

            titlePaint.apply {
                color = Color.WHITE
                textSize = 27f
                textAlign = Paint.Align.LEFT
            }

            canvas.drawText(
                ellipsize(
                    text = artist.name,
                    maxWidth = imageSize - 30f,
                    paint = titlePaint
                ),
                left + 15f,
                imageTop + imageSize - 24f,
                titlePaint
            )
        }

        return imageTop + imageSize
    }

    private fun drawTopTracks(
        canvas: Canvas,
        recap: RecapResult,
        sectionTop: Float,
        titlePaint: Paint,
        textPaint: Paint
    ): Float {
        drawSectionTitle(
            canvas = canvas,
            title = "TUS CANCIONES",
            y = sectionTop,
            paint = textPaint
        )

        val firstRowTop = sectionTop + 30f
        val rowHeight = 82f
        val rowGap = 18f
        val tracks = recap.topTracks.take(3)

        tracks.forEachIndexed { index, track ->
            val top = firstRowTop + index * (rowHeight + rowGap)

            canvas.drawRoundRect(
                RectF(
                    SIDE_PADDING,
                    top,
                    WIDTH - SIDE_PADDING,
                    top + rowHeight
                ),
                22f,
                22f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(185, 20, 20, 20)
                }
            )

            titlePaint.apply {
                color = when (index) {
                    0 -> Color.rgb(255, 55, 0)
                    1 -> Color.rgb(255, 145, 0)
                    else -> Color.rgb(255, 205, 0)
                }
                textSize = 25f
                textAlign = Paint.Align.CENTER
            }

            canvas.drawText(
                (index + 1).toString(),
                112f,
                top + 51f,
                titlePaint
            )

            titlePaint.apply {
                color = Color.WHITE
                textSize = 26f
                textAlign = Paint.Align.LEFT
            }

            canvas.drawText(
                ellipsize(
                    text = track.name,
                    maxWidth = 600f,
                    paint = titlePaint
                ),
                155f,
                top + 36f,
                titlePaint
            )

            textPaint.apply {
                color = Color.argb(130, 255, 255, 255)
                textSize = 20f
                textAlign = Paint.Align.LEFT
            }

            canvas.drawText(
                ellipsize(
                    text = track.artistName,
                    maxWidth = 600f,
                    paint = textPaint
                ),
                155f,
                top + 64f,
                textPaint
            )

            textPaint.apply {
                color = Color.argb(105, 255, 255, 255)
                textSize = 19f
                textAlign = Paint.Align.RIGHT
            }

            canvas.drawText(
                track.playcount.toString(),
                WIDTH - 105f,
                top + 51f,
                textPaint
            )
        }

        textPaint.textAlign = Paint.Align.LEFT

        return if (tracks.isEmpty()) {
            sectionTop
        } else {
            firstRowTop +
                    ((tracks.size - 1) * (rowHeight + rowGap)) +
                    rowHeight
        }
    }

    private fun drawGenres(
        canvas: Canvas,
        recap: RecapResult,
        sectionTop: Float,
        titlePaint: Paint,
        textPaint: Paint
    ) {
        drawSectionTitle(
            canvas = canvas,
            title = "TU SONIDO",
            y = sectionTop,
            paint = textPaint
        )

        recap.genres.take(4).forEachIndexed { index, genre ->
            val y = sectionTop + 55f + index * 62f

            val accent = when (index) {
                0 -> Color.rgb(255, 45, 0)
                1 -> Color.rgb(255, 122, 0)
                2 -> Color.rgb(255, 208, 0)
                else -> Color.rgb(122, 61, 255)
            }

            canvas.drawCircle(
                84f,
                y - 9f,
                7f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accent
                }
            )

            titlePaint.apply {
                color = if (index == 0) {
                    Color.WHITE
                } else {
                    Color.argb(185, 255, 255, 255)
                }
                textSize = if (index == 0) 34f else 29f
                textAlign = Paint.Align.LEFT
            }

            canvas.drawText(
                ellipsize(
                    text = genre.name,
                    maxWidth = 690f,
                    paint = titlePaint
                ),
                110f,
                y,
                titlePaint
            )

            textPaint.apply {
                color = Color.argb(115, 255, 255, 255)
                textSize = 23f
                textAlign = Paint.Align.RIGHT
            }

            canvas.drawText(
                "${genre.percentage}%",
                WIDTH - SIDE_PADDING,
                y,
                textPaint
            )
        }

        textPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawFooter(
        canvas: Canvas,
        titlePaint: Paint,
        textPaint: Paint
    ) {
        canvas.drawLine(
            SIDE_PADDING,
            1810f,
            WIDTH - SIDE_PADDING,
            1810f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(45, 255, 255, 255)
                strokeWidth = 2f
            }
        )

        canvas.drawCircle(
            SIDE_PADDING + 7f,
            1857f,
            7f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(255, 75, 0)
            }
        )

        titlePaint.apply {
            color = Color.WHITE
            textSize = 34f
            textAlign = Paint.Align.LEFT
        }

        canvas.drawText(
            "RECAPPED",
            SIDE_PADDING + 26f,
            1870f,
            titlePaint
        )

        textPaint.apply {
            color = Color.argb(105, 255, 255, 255)
            textSize = 20f
            textAlign = Paint.Align.RIGHT
        }

        canvas.drawText(
            "TU MÚSICA, TU HISTORIA",
            WIDTH - SIDE_PADDING,
            1868f,
            textPaint
        )
    }

    private fun drawSectionTitle(
        canvas: Canvas,
        title: String,
        y: Float,
        paint: Paint
    ) {
        canvas.drawRoundRect(
            RectF(
                SIDE_PADDING,
                y - 17f,
                SIDE_PADDING + 6f,
                y + 2f
            ),
            3f,
            3f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(255, 75, 0)
            }
        )

        paint.apply {
            color = Color.argb(135, 255, 255, 255)
            textSize = 20f
            textAlign = Paint.Align.LEFT
        }

        canvas.drawText(
            title,
            SIDE_PADDING + 18f,
            y,
            paint
        )
    }

    private fun createWrappedLines(
        text: String,
        maxWidth: Float,
        maxLines: Int,
        paint: Paint
    ): List<String> {
        val words = text.trim().split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var currentLine = ""

        words.forEach { word ->
            val candidate = if (currentLine.isBlank()) {
                word
            } else {
                "$currentLine $word"
            }

            if (paint.measureText(candidate) <= maxWidth) {
                currentLine = candidate
            } else {
                if (currentLine.isNotBlank()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }

        if (currentLine.isNotBlank()) {
            lines.add(currentLine)
        }

        if (lines.isEmpty()) {
            return listOf("Tu recap musical")
        }

        val visibleLines = lines.take(maxLines).toMutableList()

        if (lines.size > maxLines) {
            visibleLines[visibleLines.lastIndex] = ellipsize(
                text = visibleLines.last(),
                maxWidth = maxWidth,
                paint = paint
            )
        }

        return visibleLines
    }

    private fun drawRoundedBitmap(
        canvas: Canvas,
        bitmap: Bitmap,
        destination: RectF,
        radius: Float
    ) {
        val shader = BitmapShader(
            bitmap,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP
        )

        val scale = maxOf(
            destination.width() / bitmap.width,
            destination.height() / bitmap.height
        )

        val scaledWidth = bitmap.width * scale
        val scaledHeight = bitmap.height * scale

        val matrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(
                destination.left +
                        (destination.width() - scaledWidth) / 2f,
                destination.top +
                        (destination.height() - scaledHeight) / 2f
            )
        }

        shader.setLocalMatrix(matrix)

        canvas.drawRoundRect(
            destination,
            radius,
            radius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.shader = shader
            }
        )
    }

    private fun drawImagePlaceholder(
        canvas: Canvas,
        rect: RectF,
        rank: Int,
        paint: Paint
    ) {
        canvas.drawRoundRect(
            rect,
            30f,
            30f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    rect.left,
                    rect.top,
                    rect.right,
                    rect.bottom,
                    intArrayOf(
                        Color.rgb(255, 45, 0),
                        Color.rgb(122, 61, 255)
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
        )

        paint.apply {
            color = Color.argb(215, 255, 255, 255)
            textSize = 90f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText(
            rank.toString(),
            rect.centerX(),
            rect.centerY() + 30f,
            paint
        )
    }

    private fun ellipsize(
        text: String,
        maxWidth: Float,
        paint: Paint
    ): String {
        if (paint.measureText(text) <= maxWidth) {
            return text
        }

        var result = text

        while (
            result.isNotEmpty() &&
            paint.measureText("$result…") > maxWidth
        ) {
            result = result.dropLast(1)
        }

        return "$result…"
    }

    private fun loadBitmap(
        context: Context,
        url: String?
    ): Bitmap? {
        if (url.isNullOrBlank()) {
            return null
        }

        return try {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .submit()
                .get()
        } catch (_: Exception) {
            null
        }
    }

    private fun formatPeriod(
        recap: RecapResult
    ): String {
        val date = SimpleDateFormat(
            "MMMM yyyy",
            Locale("es", "AR")
        ).format(Date(recap.generatedAt))

        return "${recap.period.title} · $date"
            .uppercase(Locale("es", "AR"))
    }
}