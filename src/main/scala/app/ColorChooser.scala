package app

//======================================================================================================================
// Imports
//======================================================================================================================

import javafx.beans.{binding => jfxb}
import javafx.scene.{layout => jfxl, paint => jfxp, control => jfxc}
import scalafx.Includes._
import scalafx.beans.binding.Bindings
import scalafx.beans.property.{DoubleProperty, ObjectProperty}
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Tooltip}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.paint.{Color, CycleMethod, LinearGradient, Stop}

//======================================================================================================================
// ColorChooser
//======================================================================================================================

class ColorChooser extends VBox {

  //====================================================================================================================
  // Variables
  //====================================================================================================================

  private final val Bean = this
  private final val PropName = null
  private final val DefaultColor = jfxp.Color.WHITE

  private final val PaletteColumns = 12
  private final val PaletteRows = 2

  private val hue = new DoubleProperty(Bean, PropName, DefaultColor.hue * 100)
  private val sat = new DoubleProperty(Bean, PropName, DefaultColor.saturation * 100)
  private val bright = new DoubleProperty(Bean, PropName, DefaultColor.brightness * 100)
  private val alpha = new DoubleProperty(Bean, PropName, DefaultColor.opacity * 100)

  private val buttonProp = new ObjectProperty[Button](Bean, PropName, null) {
    onChange { (_, oldButton, _) =>
      if (oldButton != null) {
        oldButton.style = getBackgroundStyle(oldButton.userData.asInstanceOf[jfxp.Color])
      }
    }
  }

  private val valueProperty = new ObjectProperty[jfxp.Color](Bean, PropName, DefaultColor) {
    onChange { (_, _, newColor) =>
      hue.set(newColor.getHue)
      sat.set(newColor.getSaturation * 100)
      bright.set(newColor.getBrightness * 100)
    }
  }

  this.getStyleClass.add("my-custom-color")

  //====================================================================================================================
  // GUI Elements
  //====================================================================================================================

  private val colorRect: StackPane = new StackPane() {
    val ref: StackPane = this
    styleClass.add("color-rect")
    vgrow = Priority.Sometimes
    children = Seq(
      new StackPane() {
        opacity <== alpha.divide(100)
        children = Seq(
          new Pane() {
            background <== Bindings.createObjectBinding(
              () => {
                new jfxl.Background(new BackgroundFill(
                  Color.hsb(hue.value, 1.0, 1.0),
                  CornerRadii.Empty,
                  Insets.Empty))
              }, hue
            )
          },
          new Pane() {
            styleClass.add("color-rect")
            background = new jfxl.Background(new BackgroundFill(
              LinearGradient(0, 0, 1, 0, true, CycleMethod.NoCycle,
                Stop(0, Color.rgb(255, 255, 255, 1.0d)),
                Stop(1, Color.rgb(255, 255, 255, 0.0d))),
              CornerRadii.Empty, Insets.Empty
            ))
          },
          new Pane() {
            styleClass.add("color-rect")
            background = new jfxl.Background(new BackgroundFill(
              LinearGradient(0, 0, 0, 1, true, CycleMethod.NoCycle,
                Stop(0, Color.rgb(0, 0, 0, 0)),
                Stop(1, Color.rgb(0, 0, 0, 1))),
              CornerRadii.Empty, Insets.Empty
            ))
            onMouseDragged = { me: MouseEvent => handleRectMouseEvent(me) }
            onMousePressed = { me: MouseEvent => handleRectMouseEvent(me) }
          }
        )
      },
      new Pane() {
        styleClass.addAll("color-rect", "color-rect-border")
        mouseTransparent = true
      },
      new Pane() {
        managed = false
        mouseTransparent = true
        children = Seq(
          new Pane() {
            id = "color-rect-indicator"
            mouseTransparent = true
            cache = true
            layoutX <== sat.divide(100).multiply(ref.width)
            layoutY <== jfxb.Bindings.subtract(1, bright.divide(100)).multiply(ref.height)
          }
        )
      }
    )
  }

  private val colorBar: Pane = new Pane() {
    val ref: Pane = this
    styleClass.add("color-bar")
    background = new jfxl.Background(new BackgroundFill(
      createHueGradient, CornerRadii.Empty, Insets.Empty
    ))
    onMouseDragged = { me: MouseEvent => handleBarMouseEvent(me) }
    onMousePressed = { me: MouseEvent => handleBarMouseEvent(me) }
    children = Seq(
      new Region() {
        id = "color-bar-indicator"
        mouseTransparent = true
        cache = true
        layoutX <== hue.divide(360).multiply(ref.width)
      }
    )
  }

  private val previewRect: Pane = new Pane() {
    styleClass.add("preview-rect")
    background <== Bindings.createObjectBinding(
      () => {
        new jfxl.Background(new BackgroundFill(
          valueProperty.value,
          CornerRadii.Empty,
          Insets.Empty))
      }, valueProperty
    )
    onMouseClicked = { _: MouseEvent =>
      buttonProp.value.userData = valueProperty()
      buttonProp.value.style = getBackgroundStyle(buttonProp.value.userData.asInstanceOf[jfxp.Color]) +
        getBorderStyle(buttonProp.value.userData.asInstanceOf[jfxp.Color])
    }
    onMouseEntered = { _: MouseEvent =>
      style = "-fx-border-color: #70beff; -fx-border-width: 2;"
    }
    onMouseExited = { _: MouseEvent =>
      style = "-fx-border-color: derive(#ececec, -20%); -fx-border-width: 1;"
    }
    Tooltip.install(this, new Tooltip("Click to save color"))
  }

  private val colorPalette = new GridPane() {
    vgrow = Priority.Never
    hgap = 1
    vgap = 1
    children = {
      val swatches = for {
        i <- 0 until PaletteRows
        j <- 0 until PaletteColumns
        swatch = {
          val button = new Button() {
            userData = DefaultColor
            styleClass.add("color-swatch")
            style = getBackgroundStyle(DefaultColor)
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
    buttonProp.value = { val button = children.head.asInstanceOf[jfxc.Button]; button }
  }

  private val box = new VBox() {
    styleClass.add("color-rect-pane")
    children = Seq(colorBar, colorRect, previewRect, colorPalette)
  }

  this.children = Seq(box)

  //====================================================================================================================
  // Private methods
  //====================================================================================================================

  private def handleRectMouseEvent(me: MouseEvent): Unit = {
    val x = if (me.getX <= 0) 0.01 else me.getX
    val y = if (me.getY >= 200) 199.99 else me.getY
    sat.value = clamp(x / colorRect.getWidth) * 100
    bright.value = 100 - (clamp(y / colorRect.getHeight) * 100)
    updateHSBColor()
  }

  private def handleBarMouseEvent(me: MouseEvent): Unit = {
    val x = me.getX
    hue.set(clamp(x / colorRect.getWidth) * 360)
    updateHSBColor()
  }

  private def createHueGradient: LinearGradient = {
    val stops = for {
      x <- 0 until 255
      s = Stop((1.0 / 255) * x, Color.hsb(((x / 255.0) * 360).toInt, 1.0, 1.0))
    } yield s
    LinearGradient(0f, 0f, 1f, 0f, proportional = true, CycleMethod.NoCycle, stops.toList)
  }

  private def updateHSBColor(): Unit = {
    val newColor = Color.hsb(hue.get, clamp(sat.get / 100), clamp(bright.get / 100), clamp(alpha.get / 100))
    value = newColor
  }

  private def clamp(value: Double): Double = {
    if (value < 0) 0
    else if (value > 1) 1
    else value
  }

  private def getBackgroundStyle(color: Color): String = { "" +
    "-fx-background-color: " + Util.colorToRGBCode(color) + ";" +
    "-fx-background-insets: 0;" +
    "-fx-background-radius: 0;" +
    "-fx-border-color: ladder(" + Util.colorToRGBCode(color) + ", #bdbdbd 49%, #bdbdbd 50%);" +
    "-fx-border-radius: 0;"
  }

  private def getBorderStyle(color: Color): String = { "" +
    "-fx-border-width: 2;"
  }

  //====================================================================================================================
  // Public methods
  //====================================================================================================================

  def value: ObjectProperty[jfxp.Color] = valueProperty

  def value_=(color: Color): Unit = {
    value() = color
  }

}