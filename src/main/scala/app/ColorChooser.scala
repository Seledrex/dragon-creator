package app

//======================================================================================================================
// Imports
//======================================================================================================================

import javafx.beans.{binding => jfxb}
import javafx.scene.{control => jfxc, layout => jfxl, paint => jfxp}
import scalafx.Includes._
import scalafx.beans.binding.Bindings
import scalafx.beans.property.{DoubleProperty, ObjectProperty}
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.TextFormatter.Change
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

  private val hue = new DoubleProperty(Bean, PropName, DefaultColor.hue * 100)
  private val sat = new DoubleProperty(Bean, PropName, DefaultColor.saturation * 100)
  private val bright = new DoubleProperty(Bean, PropName, DefaultColor.brightness * 100)
  private val red = new DoubleProperty(Bean, PropName, 255)
  private val green = new DoubleProperty(Bean, PropName, 255)
  private val blue = new DoubleProperty(Bean, PropName, 255)
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

  private val colorCodeControl: HBox = new HBox(Properties.Padding) {
    alignment = Pos.Center
    children = Seq(
      new VBox(Properties.Padding) {
        children = Seq(
          new HBox(Properties.Padding) {
            children = Seq(
              new Label("R:") {
                prefWidth = 15
                alignment = Pos.CenterRight
              },
              new TextField() {
                prefWidth = 40
                font = Font("Courier New")
                alignmentInParent = Pos.Center
                textFormatter = new TextFormatter[String]( {change: Change =>
                  val maxLen: Int = 3
                  if (change.controlNewText.length > maxLen) {
                    change.text = ""
                  }
                  if (change.anchor > maxLen) change.anchor = maxLen
                  if (change.caretPosition > maxLen) change.caretPosition = maxLen
                  change
                })
              },
              new Label("G:") {
                prefWidth = 15
                alignment = Pos.CenterRight
              },
              new TextField() {
                prefWidth = 40
                font = Font("Courier New")
                alignmentInParent = Pos.Center
                textFormatter = new TextFormatter[String]( {change: Change =>
                  val maxLen: Int = 3
                  if (change.controlNewText.length > maxLen) {
                    change.text = ""
                  }
                  if (change.anchor > maxLen) change.anchor = maxLen
                  if (change.caretPosition > maxLen) change.caretPosition = maxLen
                  change
                })
              },
              new Label("B:") {
                prefWidth = 15
                alignment = Pos.CenterRight
              },
              new TextField() {
                prefWidth = 40
                font = Font("Courier New")
                alignmentInParent = Pos.Center
                textFormatter = new TextFormatter[String]( {change: Change =>
                  val maxLen: Int = 3
                  if (change.controlNewText.length > maxLen) {
                    change.text = ""
                  }
                  if (change.anchor > maxLen) change.anchor = maxLen
                  if (change.caretPosition > maxLen) change.caretPosition = maxLen
                  change
                })
              }
            )
          },
          new HBox(Properties.Padding) {
            children = Seq(
              new Label("H:") {
                prefWidth = 15
                alignment = Pos.CenterRight
              },
              new TextField() {
                prefWidth = 40
                font = Font("Courier New")
                alignmentInParent = Pos.Center
                textFormatter = new TextFormatter[String]( {change: Change =>
                  val maxLen: Int = 3
                  if (change.controlNewText.length > maxLen) {
                    change.text = ""
                  }
                  if (change.anchor > maxLen) change.anchor = maxLen
                  if (change.caretPosition > maxLen) change.caretPosition = maxLen
                  change
                })
              },
              new Label("S:") {
                prefWidth = 15
                alignment = Pos.CenterRight
              },
              new TextField() {
                prefWidth = 40
                font = Font("Courier New")
                alignmentInParent = Pos.Center
                textFormatter = new TextFormatter[String]( {change: Change =>
                  val maxLen: Int = 3
                  if (change.controlNewText.length > maxLen) {
                    change.text = ""
                  }
                  if (change.anchor > maxLen) change.anchor = maxLen
                  if (change.caretPosition > maxLen) change.caretPosition = maxLen
                  change
                })
              },
              new Label("V:") {
                prefWidth = 15
                alignment = Pos.CenterRight
              },
              new TextField() {
                prefWidth = 40
                font = Font("Courier New")
                alignmentInParent = Pos.Center
                textFormatter = new TextFormatter[String]( {change: Change =>
                  val maxLen: Int = 3
                  if (change.controlNewText.length > maxLen) {
                    change.text = ""
                  }
                  if (change.anchor > maxLen) change.anchor = maxLen
                  if (change.caretPosition > maxLen) change.caretPosition = maxLen
                  change
                })
              }
            )
          },
          new HBox(Properties.Padding) {
            children = Seq(
              new Label("Hex:") {
                alignment = Pos.CenterRight
              },
              new TextField() {
                prefWidth = 60
                font = Font("Courier New")
                alignmentInParent = Pos.Center
                textFormatter = new TextFormatter[String]( {change: Change =>
                  val maxLen: Int = 6
                  if (change.controlNewText.length > maxLen) {
                    change.text = ""
                  }
                  if (change.anchor > maxLen) change.anchor = maxLen
                  if (change.caretPosition > maxLen) change.caretPosition = maxLen
                  change
                })
              },
              new Button("Enter") {
                alignmentInParent = Pos.Center
              }
            )
          }
        )
      }
    )
  }

  private val colorControl: VBox = new VBox(Properties.Padding) {

    styleClass.add("color-control")
    alignment = Pos.Center

    // Controls
    val labels = new Array[Label](3)
    val sliders = new Array[Slider](3)
    val fields = new Array[TextField](3)

    val settingsPane = new VBox()

    // Update settings
    for (i <- 0 until 3) {
      labels(i) = new Label("Brightness:") {
        prefWidth = 60
      }
      sliders(i) = new Slider() {
        prefWidth = 100
      }
      fields(i) = new TextField() {
        prefWidth = 40
        font = Font("Courier New")
        textFormatter = new TextFormatter[String]( {change: Change =>
          val maxLen: Int = 3
          if (change.controlNewText.length > maxLen || !change.controlNewText.matches("[0-9]+")) {
            change.text = ""
          }
          if (change.anchor > maxLen) change.anchor = maxLen
          if (change.caretPosition > maxLen) change.caretPosition = maxLen
          change
        })
      }
      settingsPane.children.add(
        {
          val box = new HBox() {
            alignment = Pos.CenterLeft
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
        }
      )
    }

    // Create toggles
    val rgbToggle = new ToggleButton("RGB") {
      id = "RGB"
    }
    val hsbToggle = new ToggleButton("HSB") {
      id = "HSB"
    }
    val webToggle = new ToggleButton("Web") {
      id = "WEB"
    }

    // Create toggle group
    val toggleGroup = new ToggleGroup()
    toggleGroup.toggles = Seq(rgbToggle, hsbToggle, webToggle)
    toggleGroup.selectToggle(rgbToggle)
    toggleGroup.selectedToggle.onChange {
      val selected = toggleGroup.selectedToggle.get.asInstanceOf[javafx.scene.control.ToggleButton]
      selected.id() match {
        case a if a == "RGB" =>
          set(0, "Red:", 255, red)
          set(1, "Green:", 255, green)
          set(2, "Blue:", 255, blue)
        case b if b == "HSB" =>
          set(0, "Hue:", 360, hue)
          set(1, "Saturation:", 100, sat)
          set(2, "Brightness:", 100, bright)
        case c if c == "WEB" =>
      }
    }

    def set(i: Int, label: String, max: Int, property: DoubleProperty): Unit = {
      labels(i).setText(label)
      sliders(i).setMax(max)
    }

    children = Seq(
      new HBox() {
        alignment = Pos.Center
        children = Seq(rgbToggle, hsbToggle, webToggle)
      },
      settingsPane
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
    "-fx-background-color: " + Util.colorToRGBCode(Left(color)) + ";" +
    "-fx-background-insets: 0;" +
    "-fx-background-radius: 0;" +
    "-fx-border-color: ladder(" + Util.colorToRGBCode(Left(color)) + ", #bdbdbd 49%, #bdbdbd 50%);" +
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