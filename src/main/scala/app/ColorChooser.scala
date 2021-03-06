package app

//======================================================================================================================
// Imports
//======================================================================================================================

import javafx.beans.{binding => jfxb}
import javafx.scene.{control => jfxc, layout => jfxl, paint => jfxp}
import scalafx.Includes._
import scalafx.beans.binding.Bindings
import scalafx.beans.property.{DoubleProperty, ObjectProperty, StringProperty}
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.paint.{Color, CycleMethod, LinearGradient, Stop}
import scalafx.scene.text.Font

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

  private val hue = DoubleProperty(DefaultColor.hue * 100)
  private val sat = DoubleProperty(DefaultColor.saturation * 100)
  private val bright = DoubleProperty(DefaultColor.brightness * 100)
  private val red = DoubleProperty(Util.map(DefaultColor.getRed, 0, 1, 0, 255))
  private val green = DoubleProperty(Util.map(DefaultColor.getGreen, 0, 1, 0, 255))
  private val blue = DoubleProperty(Util.map(DefaultColor.getBlue, 0, 1, 0, 255))
  private val alpha = DoubleProperty(DefaultColor.opacity * 100)
  private val web = StringProperty(Util.colorToRGBCode(Right(DefaultColor)))
  private val buttonProp = new ObjectProperty[Button](Bean, PropName,null)
  private val valueProperty = ObjectProperty(DefaultColor)

  private var updateFlag = false

  hue.onInvalidate(sync(updateHSB))
  sat.onInvalidate(sync(updateHSB))
  bright.onInvalidate(sync(updateHSB))
  red.onInvalidate(sync(updateRGB))
  green.onInvalidate(sync(updateRGB))
  blue.onInvalidate(sync(updateRGB))
  web.onInvalidate(sync(updateWeb))
  valueProperty.onChange(sync(updateAll))

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
          valueProperty(),
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

  private val colorControl: VBox = new VBox(Properties.Padding) {

    styleClass.add("color-control")
    alignment = Pos.Center

    // Controls
    val labels = new Array[Label](3)
    val sliders = new Array[Slider](3)
    val fields = new Array[IntegerField](3)
    val boundProperties = new Array[DoubleProperty](3)

    val settingsPane = new VBox()

    // Update settings
    for (i <- 0 until 3) {
      labels(i) = new Label("Brightness:") {
        prefWidth = 60
        alignment = Pos.CenterRight
      }
      sliders(i) = new Slider() {
        prefWidth = 90
      }
      fields(i) = new IntegerField() {
        prefWidth = 40
        alignment = Pos.Center
        font = Font("Courier New")
      }
      settingsPane.children.add({
        val box = new HBox() {
          alignment = Pos.CenterRight
          children = Seq(
            labels(i),
            new Region() {
              hgrow = Priority.Always
            },
            sliders(i),
            new Region() {
              hgrow = Priority.Always
            },
            fields(i)
          )
        }
        box
      })
    }

    val hexPane: HBox = new HBox() {
      margin = Insets(5)
      alignmentInParent = Pos.TopCenter
      alignment = Pos.CenterRight
      children = Seq(
        new Region() {
          hgrow = Priority.Always
        },
        new Label("Hex: ") {
          alignment = Pos.CenterRight
        },
        new HexField(web) {
          prefWidth = 60
          alignment = Pos.Center
          font = Font("Courier New")
          alignmentInParent = Pos.Center
        },
        new Region() {
          hgrow = Priority.Always
        }
      )
    }

    // Create toggles
    val rgbToggle: ToggleButton = new ToggleButton("RGB") {
      id = "RGB"
    }
    val hsbToggle: ToggleButton = new ToggleButton("HSB") {
      id = "HSB"
    }
    val hexToggle: ToggleButton = new ToggleButton("Hex") {
      id = "HEX"
    }

    // Create toggle group
    val toggleGroup = new ToggleGroup()
    toggleGroup.toggles = Seq(rgbToggle, hsbToggle, hexToggle)
    toggleGroup.selectedToggle.onChange { (_, oldValue, newValue) =>
      if (newValue == null) {
        toggleGroup.selectToggle(oldValue.asInstanceOf[javafx.scene.control.ToggleButton])
      } else {
        val newToggle = newValue.asInstanceOf[javafx.scene.control.ToggleButton]
        newToggle.id() match {
          case a if a == "RGB" =>
            settingsPane.visible = true
            hexPane.visible = false
            set(0, "Red:", 255, red, "RGB")
            set(1, "Green:", 255, green, "RGB")
            set(2, "Blue:", 255, blue, "RGB")
          case b if b == "HSB" =>
            settingsPane.visible = true
            hexPane.visible = false
            set(0, "Hue:", 360, hue, "HSB")
            set(1, "Saturation:", 100, sat, "HSB")
            set(2, "Brightness:", 100, bright, "HSB")
          case c if c == "HEX" =>
            settingsPane.visible = false
            hexPane.visible = true
        }
      }
    }
    toggleGroup.selectToggle(rgbToggle)

    def set(i: Int, label: String, max: Int, property: DoubleProperty, group: String): Unit = {
      labels(i).setText(label)
      if (boundProperties(i) != null) {
        sliders(i).value.unbindBidirectional(boundProperties(i))
        fields(i).value.unbindBidirectional(boundProperties(i))
      }
      sliders(i).setMax(max)
      sliders(i).value <==> property
      fields(i).setMax(max)
      fields(i).value <==> property
      boundProperties(i) = property
    }

    children = Seq(
      new HBox() {
        alignment = Pos.Center
        children = Seq(rgbToggle, hsbToggle, hexToggle)
      },
      new StackPane() {
        children = Seq(
          settingsPane, hexPane
        )
      }
    )
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
    buttonProp.value = children.head.asInstanceOf[jfxc.Button]
  }

  private val box = new VBox() {
    styleClass.add("color-rect-pane")
    children = Seq(colorBar, colorRect, previewRect, colorControl, colorPalette)
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
    updateHSB()
  }

  private def handleBarMouseEvent(me: MouseEvent): Unit = {
    val x = me.getX
    hue.set(clamp(x / colorRect.getWidth) * 360)
    updateHSB()
  }

  private def createHueGradient: LinearGradient = {
    val stops = for {
      x <- 0 until 255
      s = Stop((1.0 / 255) * x, Color.hsb(((x / 255.0) * 360).toInt, 1.0, 1.0))
    } yield s
    LinearGradient(0f, 0f, 1f, 0f, proportional = true, CycleMethod.NoCycle, stops.toList)
  }

  private def updateAll(): Unit = {
    hue.set(value().getHue)
    sat.set(value().getSaturation * 100)
    bright.set(value().getBrightness * 100)
    red.set(Util.map(value().getRed, 0, 1, 0, 255))
    green.set(Util.map(value().getGreen, 0, 1, 0, 255))
    blue.set(Util.map(value().getBlue, 0, 1, 0, 255))
    web.set(Util.colorToRGBCode(Right(value())))
  }

  private def updateHSB(): Unit = {
    val newColor = Color.hsb(hue.get, clamp(sat.get / 100), clamp(bright.get / 100), clamp(alpha.get / 100))
    red.set(Util.map(newColor.getRed, 0, 1, 0, 255))
    green.set(Util.map(newColor.getGreen, 0, 1, 0, 255))
    blue.set(Util.map(newColor.getBlue, 0, 1, 0, 255))
    web.set(Util.colorToRGBCode(Right(newColor)))
    value = newColor
  }

  private def updateRGB(): Unit = {
    val newColor = Color.rgb(red.get.toInt, green.get.toInt, blue.get.toInt)
    hue.set(newColor.getHue)
    sat.set(newColor.getSaturation * 100)
    bright.set(newColor.getBrightness * 100)
    web.set(Util.colorToRGBCode(Right(newColor)))
    value = newColor
  }

  private def updateWeb(): Unit = {
    if (web().length == 6) {
      val newColor = Color.web("#" + web())
      hue.set(newColor.getHue)
      sat.set(newColor.getSaturation * 100)
      bright.set(newColor.getBrightness * 100)
      red.set(Util.map(newColor.getRed, 0, 1, 0, 255))
      green.set(Util.map(newColor.getGreen, 0, 1, 0, 255))
      blue.set(Util.map(newColor.getBlue, 0, 1, 0, 255))
      value = newColor
    }
  }

  private def sync(func: () => Unit): Unit = {
    if (!updateFlag) {
      updateFlag = true
      func()
      updateFlag = false
    }
  }

  private def clamp(value: Double): Double = {
    if (value < 0) 0
    else if (value > 1) 1
    else value
  }

  private def getBackgroundStyle(color: Color): String = { "" +
    "-fx-background-color: #" + Util.colorToRGBCode(Left(color)) + ";" +
    "-fx-background-insets: 0;" +
    "-fx-background-radius: 0;" +
    "-fx-border-color: ladder(#" + Util.colorToRGBCode(Left(color)) + ", #bdbdbd 49%, #bdbdbd 50%);" +
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