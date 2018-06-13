package app

import res.{Properties, Resources}
import scalafx.geometry.Pos
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
}
