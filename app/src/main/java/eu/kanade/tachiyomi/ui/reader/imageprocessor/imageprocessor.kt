package eu.kanade.tachiyomi.ui.reader.imageprocessor

import java.io.InputStream

abstract class Processor {

  open val name = "Processor"
  open val id = "-1"

  open fun process(stream: InputStream?): InputStream? = stream

}
