package app

import res.{Prop, Res}
import scalafx.geometry.Pos
import scalafx.scene.image.{Image, ImageView, WritableImage}
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

/**
  * Class definition for an image element that appears on the interface. Consists
  * of two images, one containing the fill and the other containing the border.
  *
  * @param resource Resource to turn into image element.
  */
class ImgElem(val resource: Res.Resource) {

    // Fill image
    var fillImg: ImageView = new ImageView(image = new Image(resource.fill)) {
        fitWidth = Prop.imgRes._1
        fitHeight = Prop.imgRes._2
        visible = false
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

    // Used for updating the image's color
    private val pixelReader = fillImg.getImage.getPixelReader
    private val writableImage = new WritableImage(width, height)
    private val pixelWriter = writableImage.getPixelWriter

    // Name of resource
    val name: String = resource.name

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
        for (x <- 0 until width) {
            for (y <- 0 until height) {
                if (pixelReader.getColor(x, y).isOpaque) {
                    pixelWriter.setColor(x, y, color)
                }
            }
        }

        fillImg.setImage(writableImage)
        this.color = color
    }
}
