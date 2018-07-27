package test

//======================================================================================================================
// Imports
//======================================================================================================================

import app.{ColorChooser, ColorPalette}
import javafx.scene.{effect => jfxe, paint => jfxp}
import res.Properties
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.beans.property._
import scalafx.event.ActionEvent
import scalafx.scene.control.Button
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
object TestColorChooser extends JFXApp {

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

        title = Properties.Title
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

        val picker: ColorChooser = new ColorChooser() {
            pickerProp <== value
            styleClass.add("panel-style")
        }

        picker.requestLayout()

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

        scene = new Scene(Properties.resolution._1, Properties.resolution._2) {
            stylesheets.add("styles.css")
            root = new BorderPane() {
                center = new Pane() {
                    children = Seq(
                        imageView,
                        new ColorPalette() {
                            styleClass.add("panel-style")
                        }
                    )
                }
                top = new HBox() {
                    children = Seq(
                        picker,
                        new Button("Blep") {
                            onAction = { _: ActionEvent =>
                                picker.value = Color.DarkBlue
                            }
                        },
                    )
                }
                styleClass.add("background-style")
            }
        }
    }
}