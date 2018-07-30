package app

import res.Res

@SerialVersionUID(100L)
class ImageLayerSerialization(layer: ImageLayer) extends Serializable {
  val resource: Res.Resource = layer.resource
  val color: String = Util.colorToRGBCode(Right(layer.color()))
  val xPos: Double = layer.getTranslateX
  val yPos: Double = layer.getTranslateY
}
