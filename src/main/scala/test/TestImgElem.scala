package test

import javafx.scene.{effect => jfxe}
import res.Res
import scalafx.beans.property.ObjectProperty
import scalafx.scene.effect.ColorInput
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.Color
import scalafx.scene.{CacheHint, Group}

/**
  * Class definition for an image element that appears on the interface. Consists
  * of two images, one containing the fill and the other containing the border.
  *
  * @param resource Resource to turn into image element.
  */
class TestImgElem(val resource: Res.Resource) extends Group {

    private val fillImg = new Image(resource.fill)
    private val outlineImg = new Image(resource.outline)

    private val clipView: ImageView = new ImageView(image = fillImg)

    private val fillView: ImageView = new ImageView(image = fillImg) {
        clip = clipView
        cache = true
        cacheHint = CacheHint.Speed
    }

    private val outlineView: ImageView = new ImageView(image = outlineImg)

    children = Seq(
        fillView, outlineView
    )

    // Color of element
    var color: Color = Color.White

    // Name of resource
    val name: String = resource.name

    // Effect property
    val effectProp = new ObjectProperty[jfxe.Effect](this, null,
        new ColorInput(
            0, 0, fillImg.getWidth.toInt, fillImg.getHeight.toInt, Color.White
        )
    )

    // Bind fill image effect
    fillView.effect <== effectProp

    /**
      * Changes the color of the image's fill.
      *
      * @param color Color to change to.
      */
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