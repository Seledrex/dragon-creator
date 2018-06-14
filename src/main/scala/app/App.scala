package app

import java.io.File
import javax.imageio.ImageIO
import res.{Prop, Res, Styles}
import scalafx.Includes._
import scalafx.scene.paint.Color
import scalafx.application.JFXApp
import scalafx.beans.property._
import scalafx.embed.swing.SwingFXUtils
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.effect.BlendMode
import scalafx.scene.image.WritableImage
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.{Group, Node, Scene, SnapshotParameters}

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

    // Put all sets into a list
    val imgList: List[(String, Seq[String], (StringProperty, Set[ImgElem]))] = List(
        (Prop.bottomLabel, Seq(Res.bottomSquare.name, Res.bottomCircle.name), (bottomProp, bottomSet)),
        (Prop.baseLabel, Seq(Res.baseSquare.name, Res.baseCircle.name), (baseProp, baseSet)),
        (Prop.topLabel, Seq(Res.topSquare.name, Res.topCircle.name), (topProp, topSet)))

    // Set initial visibility
    baseSet.find(img => img.name == Res.baseSquare.name).get.visible(true)
    topSet.find(img => img.name == Res.topSquare.name).get.visible(true)
    bottomSet.find(img => img.name == Res.bottomSquare.name).get.visible(true)

    // Set the change listener for each set
    imgList.foreach(x => x._3._1.onChange { (_, oldValue, newValue) =>
        x._3._2.find(img => img.name == oldValue).get.visible(false)
        x._3._2.find(img => img.name == newValue).get.visible(true)
    })

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
            val filePanel: Node = makeDraggable(createFilePanel())
            val optionPanel: Node = makeDraggable(createOptionsPanel())
            val imagePanel: Node = makeDraggable(createImagePanel())

            filePanel.relocate(Prop.padding, Prop.padding)
            optionPanel.relocate(Prop.padding, 50)
            imagePanel.relocate(150, 50)

            children = Seq(imagePanel, optionPanel, filePanel)
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
            children = imgList.reverse.map(x => createElementControl(x._1, x._2, x._3))
            style = Styles.panelStyle
        }
    }

    /**
      * Used to quickly create Combox Boxes with labels, options,
      * property listener, and color picker.
      *
      * @param label Text to show on label.
      * @param options Options to provide in Combo Box.
      * @param x Property listener and set tuple.
      * @return Node containing Label and Combo Box.
      */
    private def createElementControl(label: String, options: Seq[String], x: (StringProperty, Set[ImgElem])): Node = {

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
        x._1 <== cb.value

        // Set event handler for changing colors
        cp.onAction = (e: ActionEvent) => {
            x._2.foreach(img => img.changeColor(cp.getValue))
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
            children = imgList.map(x => x._3._2).flatMap(set => set.toSeq).map(img => img.create)
            alignmentInParent = Pos.TopLeft
            style = Styles.panelStyle
        }
    }

    private def createFilePanel(): Node = {
        new HBox(Prop.padding) {
            children = Seq(
                new Button("New") {
                    prefWidth = Prop.buttonWidth
                },
                new Button("Open...") {
                    prefWidth = Prop.buttonWidth
                },
                new Button("Save") {
                    prefWidth = Prop.buttonWidth
                },
                new Button("Save As...") {
                    prefWidth = Prop.buttonWidth
                },
                new Button("Save Image...") {
                    prefWidth = Prop.buttonWidth
                    onAction = (e: ActionEvent) => {
                        val group: Group = new Group() {
                            children = imgList
                                .map(x => x._3._2)
                                .flatMap(set => set.toSeq)
                                .filter(img => { img.isVisible })
                                .flatMap(img => Seq(img.fillImg, img.borderImg))
                            blendMode = BlendMode.SrcAtop
                        }
                        val wr = new WritableImage(Prop.resolution._1.toInt, Prop.resolution._2.toInt)
                        val out = group.snapshot(new SnapshotParameters(), wr)
                        val file: File = new File("test.png")
                        ImageIO.write(SwingFXUtils.fromFXImage(out, null), "png", file)
                        println("HERE1")
                        Thread.sleep(5000)
                        println("HERE2")
                    }
                },
                new Button("Quit") {
                    prefWidth = Prop.buttonWidth
                    onAction = (e: ActionEvent) => {
                        System.exit(0)
                    }
                }
            )
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