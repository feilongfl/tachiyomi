package eu.kanade.tachiyomi.ui.reader.imageprocessor.loopprocessor

import eu.kanade.tachiyomi.ui.reader.imageprocessor.Processor
import java.io.InputStream

class LoopProcessor : Processor {

  override val name = "loop test"
  override val id = "2"

  override fun process(stream: InputStream?): InputStream? {
    return stream
  }

  constructor()
}
