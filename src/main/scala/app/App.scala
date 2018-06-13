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

    // Properties
    private val dragModeProperty = new BooleanProperty(this, Properties.dragModePropertyName, false)
    private val topImageProperty = new StringProperty(this, Properties.topImagePropertyName, Resources.topSquare.name)
    private val bottomImageProperty = new StringProperty(this, Properties.bottomImagePropertyName, Resources.bottomSquare.name)

    // Base image
    val base: ImageElement = new ImageElement(Resources.base)

    // Top set of images
    val topSet: Set[ImageElement] =
        Set(new ImageElement(Resources.topSquare),
            new ImageElement(Resources.topCircle))

    // Bottom set of images
    val bottomSet: Set[ImageElement] =
        Set(new ImageElement(Resources.bottomSquare),
            new ImageElement(Resources.bottomCircle))

    // Set initial visibility
    base.visible(true)
    topSet.find(img => img.name == Resources.topSquare.name).get.visible(true)
    bottomSet.find(img => img.name == Resources.bottomSquare.name).get.visible(true)

    // Change which image to show from the top set
    topImageProperty.onChange { (_, oldValue, newValue) =>
        topSet.find(img => img.name == oldValue).get.visible(false)
        topSet.find(img => img.name == newValue).get.visible(true)
    }

    // Change which image to show from the bottom set
    bottomImageProperty.onChange { (_, oldValue, newValue) =>
        bottomSet.find(img => img.name == oldValue).get.visible(false)
        bottomSet.find(img => img.name == newValue).get.visible(true)
    }

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
        val dragModeCheckbox: CheckBox = new CheckBox(Properties.dragModeCheckBoxName) {
            margin = Insets(Properties.padding)
            selected = dragModeProperty()
        }

        // Link the checkbox to the drag mode property
        dragModeProperty <== dragModeCheckbox.selected

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
      * @return Node.
      */
    private def createOptionsPanel(): Node = {
        new VBox(Properties.padding) {
            children = Seq(
                createLabelComboBox(
                    Properties.topImageComboBoxName,
                    Seq(Resources.topSquare.name, Resources.topCircle.name),
                    topImageProperty),
                createLabelComboBox(
                    Properties.bottomImageComboBoxName,
                    Seq(Resources.bottomSquare.name, Resources.bottomCircle.name),
                    bottomImageProperty)
            )
            style = Styles.panelStyle
        }
    }

    /**
      * Used to quickly create Combox Boxes with labels, options,
      * and property listener.
      *
      * @param label Text to show on label.
      * @param options Options to provide in Combo Box.
      * @param property Property listener for Combo Box to attach to.
      * @return Node containing Label and Combo Box.
      */
    private def createLabelComboBox(label: String, options: Seq[String], property: StringProperty): Node = {
        val cb: ComboBox[String] = new ComboBox(options) {
            value = options.head
            prefWidth = Properties.comboBoxWidth
        }

        property <== cb.value

        new VBox(Properties.padding) {
            children = Seq(
                new Label(label),
                cb
            )
        }
    }

    /**
      * Creates the image panel that displays the dragon being made
      * to the user.
      *
      * @return Node.
      */
    private def createImagePanel(): Node = {
        new Pane() {
            children = (bottomSet.toSeq ++ Seq(base) ++ topSet.toSeq).map(img => img.create)
            alignmentInParent = Pos.TopLeft
            style = Styles.panelStyle
        }
    }

    /**
      * Makes node draggable within a pane.
      *
      * @param node Node to make draggable.
      * @return Draggable node.
      */
    private def makeDraggable(node: Node): Node = {

        // Create context
        val dragContext = new DragContext()

        // Put node in group the filter mouse events
        new Group(node) {
            filterEvent(MouseEvent.Any) {
                me: MouseEvent =>
                    if (dragModeProperty()) {
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