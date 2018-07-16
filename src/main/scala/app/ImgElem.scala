package app

import javafx.scene.{effect => jfxe}
import res.{Prop, Res}
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Pos
import scalafx.scene.CacheHint
import scalafx.scene.effect.ColorInput
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

/**
  * Class definition for an image element that appears on the interface. Consists
  * of two images, one containing the fill and the other containing the border.
  *
  * @param resource Resource to turn into image element.
  */
class ImgElem(val resource: Res.Resource) {

    // Clip image
    private val clipImg: ImageView = new ImageView(image = new Image(resource.fill)) {
        fitWidth = Prop.imgRes._1
        fitHeight = Prop.imgRes._2
        visible = false
    }

    // Fill image
    var fillImg: ImageView = new ImageView(image = new Image(resource.fill)) {
        fitWidth = Prop.imgRes._1
        fitHeight = Prop.imgRes._2
        visible = false
        clip = clipImg
        cache = true
        cacheHint = CacheHint.Speed
    }

    // Border image
    var borderImg: ImageView = new ImageView(image = new Image(resource.border)) {
        fitWidth = Prop.imgRes._1
        fitHeight = Prop.imgRes._2
        visible = false
    }

    // Visible boolean
    var isVisible: Boolean = false

    // Color of element
    var color: Color = Color.White

    // Save dimensions of image
    private val width = fillImg.getImage.getWidth.toInt
    private val height = fillImg.getImage.getHeight.toInt

    // Name of resource
    val name: String = resource.name

    // Effect property
    val effectProp = new ObjectProperty[jfxe.Effect](this, null,
        new ColorInput(
            0, 0, width, height, Color.White
        )
    )

    // Bind fill image effect
    fillImg.effect <== effectProp

    /**
      * Creates a pane that combines the fill and border. The fill is
      * set bellow the border.
      *
      * @return Pane containing the fill and border.
      */
    def create: Pane = new Pane() {
        children = Seq(fillImg, borderImg)
        alignmentInParent = Pos.TopLeft
    }

    /**
      * Sets the visibility of the image element.
      *
      * @param v True to be show, false otherwise.
      */
    def visible(v: Boolean): Unit = {
        clipImg.visible = v
        fillImg.visible = v
        borderImg.visible = v
        isVisible = v
    }

    /**
      * Changes the color of the image's fill.
      *
      * @param color Color to change to.
      */
    def changeColor(color: Color): Unit = {
        effectProp.value = new ColorInput(
            0, 0, width, height, color
        )
        this.color = color
    }

    def changeSize(res: (Double, Double)): Unit = {
        clipImg.fitWidth = res._1
        fillImg.fitWidth = res._1
        borderImg.fitWidth = res._1
        clipImg.fitHeight = res._2
        fillImg.fitHeight = res._2
        borderImg.fitHeight = res._2
    }
}