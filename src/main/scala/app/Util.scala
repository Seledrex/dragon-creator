package app

import scalafx.scene.paint.Color
import javafx.scene.{paint => jfxp}

object Util {

  /**
    * Converts a ScalaFX Color to its hex code.
    * @param color Color to convert.
    * @return Hex code string.
    */
  def colorToRGBCode(color: Either[Color, jfxp.Color]): String = {
    color match {
      case Left(x) => ("%02x%02x%02x" format((x.red * 255).toInt, (x.green * 255).toInt, (x.blue * 255).toInt)).toUpperCase
      case Right(x) => ("%02x%02x%02x" format((x.getRed * 255).toInt, (x.getGreen * 255).toInt, (x.getBlue * 255).toInt)).toUpperCase
    }
  }

  def map(x: Double, inMin: Double, inMax: Double, outMin: Double, outMax: Double): Double = {
    (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
  }

}
