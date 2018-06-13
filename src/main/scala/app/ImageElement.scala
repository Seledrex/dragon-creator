package app

import res.Properties
import scalafx.geometry.Pos
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.Pane

class ImageElement(val name: String, val fill: String, val outline: String) {
    var fillImg: ImageView = new ImageView(image = new Image(fill)) {
        fitWidth = Properties.imageResolution._1
        fitHeight = Properties.imageResolution._2
    }

    var borderImg: ImageView = new ImageView(image = new Image(outline)) {
        fitWidth = Properties.imageResolution._1
        fitHeight = Properties.imageResolution._2
    }

    def create: Pane = new Pane() {
        children = Seq(fillImg, borderImg)
        alignmentInParent = Pos.TopLeft
    }

    def visible(v: Boolean): Unit = {
        fillImg.visible = v
        borderImg.visible = v
    }
}
