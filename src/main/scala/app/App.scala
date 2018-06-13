package app

import res.{Properties, Resources, Styles}
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

    /**
      * Property that toggles drag mode on and off.
      */
    private val dragModeActiveProperty = new BooleanProperty(this, "dragModeActive", true)

    /**
      * Application stage. All user interface elements are contained
      * within this object.
      */
    stage = new JFXApp.PrimaryStage() {

        // Set parameters
        title = Properties.title
        resizable = false

        // Create a pane that holds multiple panels
        val panelsPane: Pane = new Pane() {
            val optionPanel: Node = makeDraggable(createOptionsPanel())
            val imagePanel: Node = makeDraggable(createImagePanel())

            optionPanel.relocate(Properties.padding, Properties.padding)
            imagePanel.relocate(130, 70)

            children = Seq(imagePanel, optionPanel)
            alignmentInParent = Pos.TopLeft
        }

        // Create a checkbox to toggle drag mode
        val dragModeCheckbox: CheckBox = new CheckBox("Drag mode") {
            margin = Insets(Properties.padding)
            selected = dragModeActiveProperty()
        }

        // Link the checkbox to the drag mode property
        dragModeActiveProperty <== dragModeCheckbox.selected

        // Create scene containing all elements and proper resolution
        scene = new Scene(Properties.resolution._1, Properties.resolution._2) {
            root = new BorderPane() {
                center = panelsPane
                bottom = dragModeCheckbox
                style = Styles.backgroundStyle
            }
        }
    }

    /**
      * Creates the options panel that houses the controls for
      * designing the dragon.
      *
      * @return  Node.
      */
    private def createOptionsPanel(): Node = {
        new VBox(Properties.padding) {
            children = Seq(
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")) {
                    value = "Option 1"
                },
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")) {
                    value = "Option 1"
                },
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")) {
                    value = "Option 1"
                },
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")) {
                    value = "Option 1"
                },
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")) {
                    value = "Option 1"
                },
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")) {
                    value = "Option 1"
                },
                new ComboBox(Seq("Option 1", "Option 2", "Option 3")) {
                    value = "Option 1"
                }
            )
            style = Styles.panelStyle
        }
    }

    /**
      * Creates the image panel that displays the dragon being made
      * to the user.
      *
      * @return  Node.
      */
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

    /**
      * Makes node draggable within a pane.
      *
      * @param node  Node to make draggable.
      * @return      Draggable node.
      */
    private def makeDraggable(node: Node): Node = {

        // Create context
        val dragContext = new DragContext()

        // Put node in group the filter mouse events
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

    /**
      * Defines the dragging context using mouse anchor and
      * initial translation.
      */
    private final class DragContext {
        var mouseAnchorX: Double = 0d
        var mouseAnchorY: Double = 0d
        var initialTranslateX: Double = 0d
        var initialTranslateY: Double = 0d
    }


}