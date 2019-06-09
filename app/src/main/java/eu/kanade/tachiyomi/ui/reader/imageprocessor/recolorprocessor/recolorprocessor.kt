package eu.kanade.tachiyomi.ui.reader.imageprocessor.recolorprocessor

import eu.kanade.tachiyomi.ui.reader.imageprocessor.Processor
import java.io.InputStream

class RecolorProcessor : Processor {

  override val name = "recolor"
  override val id = "2"

  override fun process(stream: InputStream?): InputStream? {
    if (stream == null) return stream

    val bmp = InputStream2Bitmap(stream)

    return Bitmap2InputStream(bmp)
  }

  constructor()
}

