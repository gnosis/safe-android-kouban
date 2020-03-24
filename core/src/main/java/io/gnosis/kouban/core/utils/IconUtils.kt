package io.gnosis.kouban.core.utils

import android.graphics.*
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.core.view.setPadding
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import io.gnosis.kouban.core.R

fun ImageView.setTransactionIcon(picasso: Picasso, icon: String?) {
    setPadding(0)
    background = null
    setImageDrawable(null)
    colorFilter = null
    when {
        icon == "local::ethereum" -> {
            setImageResource(R.drawable.ic_ethereum_logo)
        }
        icon?.startsWith("local::") == true -> {
            setImageResource(R.drawable.circle_background)
        }
        !icon.isNullOrBlank() ->
            picasso
                .load(icon)
                .placeholder(R.drawable.circle_background)
                .error(R.drawable.circle_background)
                .transform(CircleTransformation)
                .into(this)
        else ->
            setImageResource(R.drawable.circle_background)
    }
}

fun RemoteViews.setTransactionIcon(@IdRes imageViewId: Int, picasso: Picasso, icon: String?, appWidgetIds: IntArray) {
    setViewPadding(imageViewId, 0, 0, 0, 0)
    when {
        icon == "local::ethereum" -> {
            setImageViewResource(imageViewId, R.drawable.ic_ethereum_logo)
        }
        icon?.startsWith("local::") == true -> {
            setImageViewResource(imageViewId, R.drawable.circle_background)
        }
        !icon.isNullOrBlank() ->
            picasso
                .load(icon)
                .placeholder(R.drawable.circle_background)
                .error(R.drawable.circle_background)
                .transform(CircleTransformation)
                .into(this, imageViewId, appWidgetIds)
        else ->
            setImageViewResource(imageViewId, R.drawable.circle_background)
    }
}


object CircleTransformation: Transformation {
    override fun key() = "Circle Transformation"

    override fun transform(source: Bitmap?): Bitmap? {
        source ?: return null
        val size = source.width.coerceAtMost(source.height)

        val x = (source.width - size) / 2
        val y = (source.height - size) / 2

        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
        if (squaredBitmap != source) {
            source.recycle()
        }

        val bitmap = Bitmap.createBitmap(size, size, source.config)

        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.shader = BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.isAntiAlias = true

        val r = size / 2f
        canvas.drawCircle(r, r, r, paint)

        squaredBitmap.recycle()
        return bitmap
    }

}
