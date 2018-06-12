package app

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.beans.property.BooleanProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.{Group, Node, Scene}

object App extends JFXApp {

    private val dragModeActiveProperty = new BooleanProperty(this, "dragModeActive", true)
    private val borderStyle = "" +
        "-fx-background-color: white;" +
        "-fx-border-color: black;" +
        "-fx-border-width: 2;" +
        "-fx-border-radius: 6;" +
        "-fx-background-radius: 6;" +
        "-fx-padding: 6;"

    stage = new JFXApp.PrimaryStage() {

        title = "Dragon Creator Test Interface"
        resizable = false

        val panelsPane: Pane = new Pane() {
            val optionPanel: Node = makeDraggable(createOptionsPanel())
            val imagePanel: Node = makeDraggable(createImagePanel())

            optionPanel.relocate(5, 5)
            imagePanel.relocate(130, 70)

            children = Seq(optionPanel, imagePanel)
            alignmentInParent = Pos.TopLeft
        }

        val dragModeCheckbox: CheckBox = new CheckBox("Drag mode") {
            margin = Insets(6)
            selected = dragModeActiveProperty()
        }

        dragModeActiveProperty <== dragModeCheckbox.selected

        scene = new Scene(1280, 720) {
            root = new BorderPane() {
                center = panelsPane
                bottom = dragModeCheckbox
                style = "-fx-background-color: #99baef;"
            }
        }
    }

    private def makeDraggable(node: Node): Node = {

        val dragContext = new DragContext()

        new Group(node) {
            filterEvent(MouseEvent.Any) {
                me: MouseEvent =>
                    if (dragModeActiveProperty()) {
                        me.eventType match {
                            case MouseEvent.MousePressed =>
                                dragContext.mouseAnchorX = me.x
                                dragContext.mouseAnchorY = me.y
                                dragContext.initialTranslateX = node.translateX()
                                dragContext.initialTranslateY = node.translateY()
                            case MouseEvent.MouseDragged =>
                                node.translateX = dragContext.initialTranslateX + me.x - dragContext.mouseAnchorX
                                node.translateY = dragContext.initialTranslateY + me.y - dragContext.mouseAnchorY
                            case _ =>
                        }
                        me.consume()
                    }
            }
        }
    }

    private def createOptionsPanel(): Node = {
        new VBox(4) {
            children = Seq(
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")),
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")),
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")),
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")),
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")),
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")),
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")),
                new ComboBox(Seq("Option 1", "Option 2", "Option 3"))
            )
            style = borderStyle
        }
    }

    private def createImagePanel(): Node = {
        new HBox(4) {
            children = Seq(
                new ImageView(image = new Image("draggily.png")) {
                    fitHeight = 576
                    fitWidth = 1024
                }
            )
            style = borderStyle
        }
    }

    private final class DragContext {
        var mouseAnchorX: Double = 0d
        var mouseAnchorY: Double = 0d
        var initialTranslateX: Double = 0d
        var initialTranslateY: Double = 0d
    }


}