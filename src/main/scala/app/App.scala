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

/**
  * Dragon Creator ScalaFX application.
  *
  * @author Eric Auster
  */
object App extends JFXApp {

    private val dragModeActiveProperty = new BooleanProperty(this, "dragModeActive", true)

    stage = new JFXApp.PrimaryStage() {

        title = Properties.title
        resizable = false

        val panelsPane: Pane = new Pane() {
            val optionPanel: Node = makeDraggable(createOptionsPanel())
            val imagePanel: Node = makeDraggable(createImagePanel())

            optionPanel.relocate(Properties.padding, Properties.padding)
            imagePanel.relocate(130, 70)

            children = Seq(imagePanel, optionPanel)
            alignmentInParent = Pos.TopLeft
        }

        val dragModeCheckbox: CheckBox = new CheckBox("Drag mode") {
            margin = Insets(Properties.padding)
            selected = dragModeActiveProperty()
        }

        dragModeActiveProperty <== dragModeCheckbox.selected

        scene = new Scene(Properties.resolution._1, Properties.resolution._2) {
            root = new BorderPane() {
                center = panelsPane
                bottom = dragModeCheckbox
                style = Styles.backgroundStyle
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
        new VBox(Properties.padding) {
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
            style = Styles.panelStyle
        }
    }

    private def createImagePanel(): Node = {
        new HBox(Properties.padding) {
            children = Seq(
                new ImageView(image = new Image(Resources.base)) {
                    fitWidth = Properties.imageResolution._1
                    fitHeight = Properties.imageResolution._2
                }
            )
            style = Styles.panelStyle
        }
    }

    private final class DragContext {
        var mouseAnchorX: Double = 0d
        var mouseAnchorY: Double = 0d
        var initialTranslateX: Double = 0d
        var initialTranslateY: Double = 0d
    }


}