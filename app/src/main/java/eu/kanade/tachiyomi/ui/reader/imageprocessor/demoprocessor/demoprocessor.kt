package eu.kanade.tachiyomi.ui.reader.imageprocessor.demoprocessor

import eu.kanade.tachiyomi.ui.reader.imageprocessor.Processor
import java.io.InputStream

class DemoProcessor : Processor {

  override val name = "demo"
  override val id = "1"

  override fun process(stream: InputStream?): InputStream? {
    return stream
  }

  constructor()
}
