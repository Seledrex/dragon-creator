package app

//======================================================================================================================
// Imports
//======================================================================================================================

import javafx.scene.{effect => jfxe, paint => jfxp}
import res.{Prop, Styles}
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.beans.property._
import scalafx.scene.control._
import scalafx.scene.effect.ColorInput
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.{CacheHint, Scene}

//======================================================================================================================
// Test
//======================================================================================================================

/**
  * Dragon Creator ScalaFX application.
  * @author Seledrex, Sanuthem
  */
object Test extends JFXApp {

    //==================================================================================================================
    // Application Variables
    //==================================================================================================================

    private val app = this
    private val propName = null

    //==================================================================================================================
    // Stage
    //==================================================================================================================

    /**
      * Application stage. All user interface elements are contained
      * within this object.
      */
    stage = new JFXApp.PrimaryStage() {

        title = Prop.title
        resizable = true
        maximized = true

        val image: Image = new Image("base_square_fill.png")
        val imageView: ImageView = new ImageView(image) {
            fitWidth = 854
            fitHeight = 480
        }
        val clip: ImageView = new ImageView(image)
        clip.setFitHeight(480)
        clip.setFitWidth(854)

        imageView.setClip(clip)
        imageView.cache = true
        imageView.setCacheHint(CacheHint.Speed)

        val defaultEffect = new ColorInput(
            0,
            0,
            imageView.getImage.getWidth,
            imageView.getImage.getWidth,
            Color.White
        )

        val pickerProp = new ObjectProperty[jfxp.Color](app, propName, Color.White)
        val effectProp = new ObjectProperty[jfxe.Effect](app, propName, defaultEffect)

        val picker: ColorPicker = new ColorPicker(Color.White) {
            pickerProp <== value
        }

        imageView.effect <== effectProp

        pickerProp.onChange { (_, _, newValue) =>
            effectProp.value = new ColorInput(
                0,
                0,
                imageView.getImage.getWidth,
                imageView.getImage.getWidth,
                newValue
            )
        }

        scene = new Scene(Prop.resolution._1, Prop.resolution._2) {
            root = new BorderPane() {
                center = imageView
                top = picker
                style = Styles.backgroundStyle
            }
        }
    }
}