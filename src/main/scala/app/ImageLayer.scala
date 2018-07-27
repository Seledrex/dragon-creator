package app

//======================================================================================================================
// Imports
//======================================================================================================================

import javafx.scene.{effect => jfxe, layout => jfxl}
import res.Res
import scalafx.beans.property.ObjectProperty
import scalafx.scene.CacheHint
import scalafx.scene.effect.ColorInput
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.Color

//======================================================================================================================
// ImageLayer
//======================================================================================================================

class ImageLayer(val resource: Res.Resource) extends jfxl.Pane {

  //====================================================================================================================
  // Variable Definitions
  //====================================================================================================================

  private final val DefaultColor: Color = Color.White

  private val fillImg = new Image(resource.fill)
  private val outlineImg = new Image(resource.outline)

  private val clipView: ImageView = new ImageView(image = fillImg)
  private val fillView: ImageView = new ImageView(image = fillImg) {
    cache = true
    cacheHint = CacheHint.Speed
  }
  private val outlineView: ImageView = new ImageView(image = outlineImg)

  private val effectProp = new ObjectProperty[jfxe.Effect](this, null,
    new ColorInput(0, 0, fillImg.getWidth.toInt, fillImg.getHeight.toInt, DefaultColor)
  )

  var color: Color = DefaultColor

  //====================================================================================================================
  // Construction
  //====================================================================================================================

  fillView.clip = clipView
  fillView.effect <== effectProp
  getChildren.addAll(fillView, outlineView)

  //====================================================================================================================
  // Public methods
  //====================================================================================================================

  def changeColor(color: Color): Unit = {
    effectProp.value = new ColorInput(
      0, 0, fillImg.getWidth.toInt, fillImg.getHeight.toInt, color
    )
    this.color = color
  }

  def changeSize(scaleFactor: Double): Unit = {
    clipView.fitWidth = fillImg.getWidth / scaleFactor
    clipView.fitHeight = fillImg.getHeight / scaleFactor
    fillView.fitWidth = fillImg.getWidth / scaleFactor
    fillView.fitHeight = fillImg.getHeight / scaleFactor
    outlineView.fitWidth = fillImg.getWidth / scaleFactor
    outlineView.fitHeight = fillImg.getHeight / scaleFactor
  }

}