package app

//======================================================================================================================
// Imports
//======================================================================================================================

import javafx.beans.{binding => jfxb}
import javafx.scene.{layout => jfxl, paint => jfxp}
import scalafx.Includes._
import scalafx.beans.binding.Bindings
import scalafx.beans.property.{DoubleProperty, ObjectProperty}
import scalafx.geometry.Insets
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.paint.{Color, CycleMethod, LinearGradient, Stop}

//======================================================================================================================
// ColorChooser
//======================================================================================================================

class ColorChooser extends VBox {

    //==================================================================================================================
    // Variables
    //==================================================================================================================

    private val bean = this
    private val propName = null
    private val defaultColor = Color.White

    private val hue = new DoubleProperty(bean, propName, defaultColor.hue * 100)
    private val sat = new DoubleProperty(bean, propName, defaultColor.saturation * 100)
    private val bright = new DoubleProperty(bean, propName, defaultColor.brightness * 100)
    private val alpha = new DoubleProperty(bean, propName, defaultColor.opacity * 100)

    private val valueProperty = new ObjectProperty[jfxp.Color](bean, propName, defaultColor) {
        onChange { (_, _, newColor) =>
            hue.set(newColor.getHue)
            sat.set(newColor.getSaturation * 100)
            bright.set(newColor.getBrightness * 100)
        }
    }

    this.getStyleClass.add("my-custom-color")

    //==================================================================================================================
    // GUI Elements
    //==================================================================================================================

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
            createHueGradient,
            CornerRadii.Empty, Insets.Empty
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

    private val previewRect = new Pane() {
        styleClass.add("preview-rect")
        background <== Bindings.createObjectBinding(
            () => {
                new jfxl.Background(new BackgroundFill(
                    valueProperty.value,
                    CornerRadii.Empty,
                    Insets.Empty))
            }, valueProperty
        )
    }

    private val box = new VBox() {
        styleClass.add("color-rect-pane")
        children = Seq(
            colorBar, colorRect, previewRect
        )
    }

    this.children = Seq(box)

    //==================================================================================================================
    // Private methods
    //==================================================================================================================

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

    //==================================================================================================================
    // Public methods
    //==================================================================================================================

    def value: ObjectProperty[jfxp.Color] = valueProperty

    def value_=(color: Color): Unit = {
        value() = color
    }

}