package app

import java.awt.image.BufferedImage

import res.{Properties, Resources}
import scalafx.geometry.Pos
import scalafx.scene.effect.ColorAdjust
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.Pane

/**
  * Class definition for an image element that appears on the interface. Consists
  * of two images, one containing the fill and the other containing the border.
  *
  * @param resource Resource to turn into image element.
  */
class ImageElement(resource: Resources.Resource) {

    // Fill image
    var fillImg: ImageView = new ImageView(image = new Image(resource.fill)) {
        fitWidth = Properties.imageResolution._1
        fitHeight = Properties.imageResolution._2
        visible = false
    }

    // Border image
    var borderImg: ImageView = new ImageView(image = new Image(resource.border)) {
        fitWidth = Properties.imageResolution._1
        fitHeight = Properties.imageResolution._2
        visible = false
    }

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
    }

    def changeColor(newHue: Double): Unit = {
        println("HERE")
        println(name)
        val adjust = new ColorAdjust() {
            hue = newHue
        }
        fillImg.effect = adjust
    }

    /*private def changeFill(img: BufferedImage, colorCode: Int): BufferedImage = {
        // obtain width and height of image
        val w = img.getWidth
        val h = img.getHeight

        // create new image of the same size
        val out = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR)

        // copy pixels (mirror horizontally)
        for (x <- 0 until w)
            for (y <- 0 until h)
                if ((img.getRGB(x, y) >>> 24) != 0) {
                    out.setRGB(x, y, colorCode)
                } else {
                    out.setRGB(x, y, img.getRGB(x, y))
                }

        out
    }*/

}
