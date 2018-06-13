package app

import res.{Properties, Resources, Styles}
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
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
    private val dragModeActiveProperty = new BooleanProperty(this, "dragModeActive", false)
    private val topImageProperty = new StringProperty(this, "topImage", "top_square")
    private val bottomImageProperty = new StringProperty(this, "bottomImage", "bottom_square")

    val topSquare: ImageElement = new ImageElement("top_square", Resources.topSquareFill, Resources.topSquareBorder)
    val topCircle: ImageElement = new ImageElement("top_circle", Resources.topCircleFill, Resources.topCircleBorder)
    val base: ImageElement = new ImageElement("base", Resources.baseFill, Resources.baseBorder)
    val bottomSquare: ImageElement = new ImageElement("bottom_square", Resources.bottomSquareFill, Resources.bottomSquareBorder)
    val bottomCircle: ImageElement = new ImageElement("bottom_circle", Resources.bottomCircleFill, Resources.bottomCircleBorder)

    topCircle.visible(false)
    bottomCircle.visible(false)

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

    topImageProperty.onChange { (_, _, newValue) =>
        if (newValue == "top_square") {
            topSquare.visible(true)
            topCircle.visible(false)
        } else {
            topSquare.visible(false)
            topCircle.visible(true)
        }
    }

    bottomImageProperty.onChange { (_, _, newValue) =>
        if (newValue == "bottom_square") {
            bottomSquare.visible(true)
            bottomCircle.visible(false)
        } else {
            bottomSquare.visible(false)
            bottomCircle.visible(true)
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
                createLabelComboBox("Top", Seq("top_square", "top_circle"), topImageProperty),
                createLabelComboBox("Bottom", Seq("bottom_square", "bottom_circle"), bottomImageProperty)
            )
            style = Styles.panelStyle
        }
    }

    private def createLabelComboBox(text: String, options: Seq[String], property: StringProperty): Node = {
        val cb: ComboBox[String] = new ComboBox(options) {
            value = options.head
            prefWidth = 125
        }

        property <== cb.value

        new VBox(Properties.padding) {
            children = Seq(
                new Label(text),
                cb
            )
        }
    }

    /**
      * Creates the image panel that displays the dragon being made
      * to the user.
      *
      * @return  Node.
      */
    private def createImagePanel(): Node = {
        new Pane() {
            children = Seq(
                bottomSquare.create,
                bottomCircle.create,
                base.create,
                topSquare.create,
                topCircle.create
            )
            alignmentInParent = Pos.TopLeft
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