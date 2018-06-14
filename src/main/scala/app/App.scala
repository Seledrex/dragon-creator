package app

import res.{Prop, Res, Styles}
import scalafx.Includes._
import scalafx.scene.paint.Color
import scalafx.application.JFXApp
import scalafx.beans.property._
import scalafx.event.ActionEvent
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
    private val dragModeProp = new BooleanProperty(this, Prop.dragModePropName, false)
    private val baseProp = new StringProperty(this, Prop.basePropName, Res.baseSquare.name)
    private val topProp = new StringProperty(this, Prop.topPropName, Res.topSquare.name)
    private val bottomProp = new StringProperty(this, Prop.bottomPropName, Res.bottomSquare.name)

    val baseSet: Set[ImgElem] =
        Set(new ImgElem(Res.baseSquare),
            new ImgElem(Res.baseCircle))

    // Top set of images
    val topSet: Set[ImgElem] =
        Set(new ImgElem(Res.topSquare),
            new ImgElem(Res.topCircle))

    // Bottom set of images
    val bottomSet: Set[ImgElem] =
        Set(new ImgElem(Res.bottomSquare),
            new ImgElem(Res.bottomCircle))

    // Set initial visibility
    baseSet.find(img => img.name == Res.baseSquare.name).get.visible(true)
    topSet.find(img => img.name == Res.topSquare.name).get.visible(true)
    bottomSet.find(img => img.name == Res.bottomSquare.name).get.visible(true)

    // Change which image to show from the base set
    baseProp.onChange { (_, oldValue, newValue) =>
        baseSet.find(img => img.name == oldValue).get.visible(false)
        baseSet.find(img => img.name == newValue).get.visible(true)
    }

    // Change which image to show from the top set
    topProp.onChange { (_, oldValue, newValue) =>
        topSet.find(img => img.name == oldValue).get.visible(false)
        topSet.find(img => img.name == newValue).get.visible(true)
    }

    // Change which image to show from the bottom set
    bottomProp.onChange { (_, oldValue, newValue) =>
        bottomSet.find(img => img.name == oldValue).get.visible(false)
        bottomSet.find(img => img.name == newValue).get.visible(true)
    }

    /**
      * Application stage. All user interface elements are contained
      * within this object.
      */
    stage = new JFXApp.PrimaryStage() {

        // Set parameters
        title = Prop.title
        resizable = false

        // Create a pane that holds multiple panels
        val panelsPane: Pane = new Pane() {
            val optionPanel: Node = makeDraggable(createOptionsPanel())
            val imagePanel: Node = makeDraggable(createImagePanel())

            optionPanel.relocate(Prop.padding, Prop.padding)
            imagePanel.relocate(170, 70)

            children = Seq(imagePanel, optionPanel)
            alignmentInParent = Pos.TopLeft
        }

        // Create a checkbox to toggle drag mode
        val dragModeCheckbox: CheckBox = new CheckBox(Prop.dragModeCheckBoxName) {
            margin = Insets(Prop.padding)
            selected = dragModeProp()
        }

        // Link the checkbox to the drag mode property
        dragModeProp <== dragModeCheckbox.selected

        // Create scene containing all elements and proper resolution
        scene = new Scene(Prop.resolution._1, Prop.resolution._2) {
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
        new VBox(Prop.padding) {
            children = Seq(
                createLabelComboBox(
                    Prop.topComboBoxName,
                    Seq(Res.topSquare.name, Res.topCircle.name),
                    topProp),
                createLabelComboBox(
                    Prop.baseComboBoxName,
                    Seq(Res.baseSquare.name, Res.baseCircle.name),
                    baseProp),
                createLabelComboBox(
                    Prop.bottomComboBoxName,
                    Seq(Res.bottomSquare.name, Res.bottomCircle.name),
                    bottomProp))
            style = Styles.panelStyle
        }
    }

    /**
      * Used to quickly create Combox Boxes with labels, options,
      * and property listener.
      *
      * @param label Text to show on label.
      * @param options Options to provide in Combo Box.
      * @param cbProperty Property listener for Combo Box to attach to.
      * @return Node containing Label and Combo Box.
      */
    private def createLabelComboBox(label: String, options: Seq[String],
                                    cbProperty: StringProperty): Node = {

        // Create combo box
        val cb: ComboBox[String] = new ComboBox(options) {
            value = options.head
            prefWidth = Prop.pickerWidth
        }

        // Create color picker
        val cp: ColorPicker = new ColorPicker(Color.White) {
            prefWidth = Prop.pickerWidth
        }

        // Bind combo box property to value
        cbProperty <== cb.value

        // Set event handler for changing colors
        cp.onAction = (e: ActionEvent) => {
            label match {
                case Prop.baseComboBoxName =>
                    baseSet.foreach(img => img.changeColor(cp.getValue))
                case Prop.`topComboBoxName` =>
                    topSet.foreach(img => img.changeColor(cp.getValue))
                case Prop.`bottomComboBoxName` =>
                    bottomSet.foreach(img => img.changeColor(cp.getValue))
            }
            e.consume()
        }

        // Organize vertically
        new VBox(Prop.padding) {
            children = Seq(
                new Label(label),
                cb,
                cp
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
            children = (bottomSet.toSeq ++ baseSet.toSeq ++ topSet.toSeq).map(img => img.create)
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
                    if (dragModeProp()) {
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