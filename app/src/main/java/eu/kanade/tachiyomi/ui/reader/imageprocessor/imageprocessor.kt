package eu.kanade.tachiyomi.ui.reader.imageprocessor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

abstract class Processor {

  open val name = "Processor"
  open val id = "-1"

  /**
   * process image
   */
  open fun process(stream: InputStream?): InputStream? = stream

  /**
   * convert bitmap to stream
   */
  fun Bitmap2InputStream(bm: Bitmap,
                         format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
                         quality: Int = 100
  ): InputStream {
    val baos = ByteArrayOutputStream()
    bm.compress(format, quality, baos)
    return ByteArrayInputStream(baos.toByteArray())
  }

  /**
   * convert stream to bitmap
   */
  fun InputStream2Bitmap(stream: InputStream): Bitmap {
    return BitmapFactory.decodeStream(stream)
  }

}
