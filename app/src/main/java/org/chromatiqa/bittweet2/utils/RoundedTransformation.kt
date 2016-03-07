package org.chromatiqa.bittweet2.utils

import android.graphics.*
import com.koushikdutta.ion.bitmap.Transform

class RoundedTransformation(val radius: Float, val margin: Float,
                            val tl: Boolean, val tr: Boolean, val bl: Boolean, val br: Boolean) : Transform {
    override fun key(): String = "rounded"

    override fun transform(src: Bitmap): Bitmap {
        // Create a new paint
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        // Cast bitmap height and width to float
        val width = src.width.toFloat()
        val height = src.height.toFloat()

        // Create the output bitmap and initialize on canvas
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawRoundRect(RectF(margin, margin, src.width - margin, src.height - margin), radius, radius, paint)

        // Create rounded bitmap according to which corners need to be rounded
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        if (!tl) {
            canvas.drawRect(0.0F, 0.0F, width/2, height/2, paint)
        }
        if (!tr) {
            canvas.drawRect(width/2, 0.0F, width, height/2, paint)
        }
        if (!bl) {
            canvas.drawRect(0.0F, height/2, width/2, height, paint)
        }
        if (!br) {
            canvas.drawRect(width/2, height/2, width, height, paint)
        }
        if (src != output) {
            src.recycle()
        }
        return output
    }
}
