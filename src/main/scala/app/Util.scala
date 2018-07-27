package app

import scalafx.scene.paint.Color

object Util {

  /**
    * Converts a ScalaFX Color to its hex code.
    * @param color Color to convert.
    * @return Hex code string.
    */
  def colorToRGBCode(color: Color): String = {
    "#%02x%02x%02x" format ((color.red * 255).toInt, (color.green * 255).toInt, (color.blue * 255).toInt)
  }

}
