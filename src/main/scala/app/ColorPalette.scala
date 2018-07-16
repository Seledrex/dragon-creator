package app

import javafx.scene.{control => jfxc, paint => jfxp}
import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.event.ActionEvent
import scalafx.scene.control.Button
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.paint.Color

class ColorPalette extends GridPane {

    private val bean = this
    private val propName = null
    private val defaultColor = jfxp.Color.WHITE

    private final val paletteColumns = 10
    private final val paletteRows = 2

    private val buttonProp = new ObjectProperty[Button](bean, propName, null) {
        onChange { (_, oldButton, _) =>
            if (oldButton != null) {
                oldButton.style = getBackgroundStyle(oldButton.userData.asInstanceOf[jfxp.Color])
            }
        }
    }

    private val valueProperty = new ObjectProperty[jfxp.Color](bean, propName, defaultColor) {
        onChange { (_, _, newColor) =>
            buttonProp.value.userData = newColor
            buttonProp.value.style = getBackgroundStyle(buttonProp.value.userData.asInstanceOf[jfxp.Color]) +
                getBorderStyle(buttonProp.value.userData.asInstanceOf[jfxp.Color])
        }
    }

    vgrow = Priority.Never
    hgap = 1
    vgap = 1

    children = {
        val swatches = for {
            i <- 0 until paletteRows
            j <- 0 until paletteColumns
            swatch = {
                val button = new Button() {
                    userData = defaultColor
                    styleClass.add("color-swatch")
                    style = getBackgroundStyle(defaultColor)
                }
                button.onMouseEntered = { _: MouseEvent =>
                    button.style = getBackgroundStyle(button.userData.asInstanceOf[jfxp.Color]) +
                        getBorderStyle(button.userData.asInstanceOf[jfxp.Color])
                }
                button.onMouseExited = { _: MouseEvent =>
                    if (buttonProp.value != button)
                        button.style = getBackgroundStyle(button.userData.asInstanceOf[jfxp.Color])
                }
                button.onAction = { _: ActionEvent =>
                    buttonProp.value = button
                    valueProperty.value = button.userData.asInstanceOf[jfxp.Color]
                }
                GridPane.setRowIndex(button, i)
                GridPane.setColumnIndex(button, j)
                button
            }
        } yield swatch
        swatches
    }

    private def getBackgroundStyle(color: Color): String = { "" +
        "-fx-background-color: " + Util.colorToRGBCode(color) + ";" +
        "-fx-background-insets: 0;" +
        "-fx-background-radius: 0;" +
        "-fx-border-color: ladder(" + Util.colorToRGBCode(color) + ", #7a7a7a 49%, #7a7a7a 50%);" +
        "-fx-border-radius: 0;"
    }

    private def getBorderStyle(color: Color): String = { "" +
        "-fx-border-width: 2;"
    }

    buttonProp.value = children.get(0).asInstanceOf[jfxc.Button]

    //==================================================================================================================
    // Public methods
    //==================================================================================================================

    def value: ObjectProperty[jfxp.Color] = valueProperty

    def value_=(color: Color): Unit = {
        value() = color
    }

}
