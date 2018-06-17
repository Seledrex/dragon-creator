package app

import java.io.{File, PrintWriter, StringWriter}
import javax.imageio.ImageIO
import org.apache.commons.io.FilenameUtils
import res.{Prop, Res, Styles}
import scalafx.Includes._
import scalafx.scene.paint.Color
import scalafx.application.JFXApp
import scalafx.beans.property._
import scalafx.embed.swing.SwingFXUtils
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.effect.BlendMode
import scalafx.scene.image.WritableImage
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.{Group, Node, Scene, SnapshotParameters}
import scalafx.stage.FileChooser

/**
  * Dragon Creator ScalaFX application.
  *
  * @author Eric Auster
  */
object App extends JFXApp {

    // Properties
    private val dragModeProp = new BooleanProperty(this, Prop.dragModePropName, false)

    // Base
    private val baseCBProp = new StringProperty(this, Prop.baseCBPropName, Res.baseSquare.name)
    private val baseCPProp = new ObjectProperty[javafx.scene.paint.Color](this, Prop.baseCPPropName, Color.White)
    val baseSet: Set[ImgElem] =
        Set(new ImgElem(Res.baseSquare),
            new ImgElem(Res.baseCircle))

    // Top
    private val topCBProp = new StringProperty(this, Prop.topCBPropName, Res.topSquare.name)
    private val topCPProp = new ObjectProperty[javafx.scene.paint.Color](this, Prop.topCPPropName, Color.White)
    val topSet: Set[ImgElem] =
        Set(new ImgElem(Res.topSquare),
            new ImgElem(Res.topCircle))

    // Bottom
    private val bottomCBProp = new StringProperty(this, Prop.bottomCBPropName, Res.bottomSquare.name)
    private val bottomCPProp = new ObjectProperty[javafx.scene.paint.Color](this, Prop.bottomCPPropName, Color.White)
    val bottomSet: Set[ImgElem] =
        Set(new ImgElem(Res.bottomSquare),
            new ImgElem(Res.bottomCircle))

    // Put all sets into a list
    val imgList: List[(String, Seq[String], (Set[ImgElem], StringProperty, ObjectProperty[javafx.scene.paint.Color]))] = List(
        (Prop.bottomLabel, Seq(Res.bottomSquare.name, Res.bottomCircle.name), (bottomSet, bottomCBProp, bottomCPProp)),
        (Prop.baseLabel, Seq(Res.baseSquare.name, Res.baseCircle.name), (baseSet, baseCBProp, baseCPProp)),
        (Prop.topLabel, Seq(Res.topSquare.name, Res.topCircle.name), (topSet, topCBProp, topCPProp)))

    // Set initial visibility
    baseSet.find(img => img.name == Res.baseSquare.name).get.visible(true)
    topSet.find(img => img.name == Res.topSquare.name).get.visible(true)
    bottomSet.find(img => img.name == Res.bottomSquare.name).get.visible(true)

    // Set combo box change listeners
    imgList.foreach(x => x._3._2.onChange { (_, oldValue, newValue) =>
        x._3._1.find(img => img.name == oldValue).get.visible(false)
        x._3._1.find(img => img.name == newValue).get.visible(true)
    })

    // Set color picker change listeners
    imgList.foreach(x => x._3._3.onChange { (_, _, newValue) =>
        x._3._1.foreach(img => img.changeColor(newValue))
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
    private def createElementControl(label: String, options: Seq[String], x: (Set[ImgElem], StringProperty, ObjectProperty[javafx.scene.paint.Color])): Node = {

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
        x._2 <==> cb.value

        // Bind color picker property to value
        x._3 <==> cp.value

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
            children = imgList.map(x => x._3._1).flatMap(set => set.toSeq).map(img => img.create)
            alignmentInParent = Pos.TopLeft
            style = Styles.panelStyle
        }
    }

    /**
      * Creates the file panel which gives users basic controls.
      *
      * @return Node.
      */
    private def createFilePanel(): Node = {
        new HBox(Prop.padding) {
            children = Seq(
                new Button(Prop.newButton) {
                    prefWidth = Prop.buttonWidth
                },
                new Button(Prop.openButton) {
                    prefWidth = Prop.buttonWidth
                },
                new Button(Prop.saveButton) {
                    prefWidth = Prop.buttonWidth
                },
                new Button(Prop.saveAsButton) {
                    prefWidth = Prop.buttonWidth
                },
                new Button(Prop.saveImageButton) {
                    prefWidth = Prop.buttonWidth
                    onAction = (_: ActionEvent) => {
                        // Create file chooser
                        val chooser = new FileChooser() {
                            title = Prop.fileChooserTitle
                            extensionFilters.addAll(
                                new FileChooser.ExtensionFilter("PNG", "*.png"))
                        }

                        // Prompt user to choose file
                        var file = chooser.showSaveDialog(stage)

                        // Check if a file is chosen
                        if (file != null) {

                            // Ensure file extension
                            if (FilenameUtils.getExtension(file.getAbsolutePath) == "") {
                                file = new File(file.getAbsolutePath + ".png")
                            }

                            // Group together visible elements and make copies
                            val group: Group = new Group() {
                                children = imgList
                                    .map(x => x._3._1)
                                    .flatMap(set => set.toSeq)
                                    .filter(img => img.isVisible)
                                    .map(img => (img.resource, img.color))
                                    .map(x => {
                                        val copy = new ImgElem(x._1)
                                        copy.visible(true)
                                        copy.changeColor(x._2)
                                        copy })
                                    .flatMap(x => Seq(x.fillImg, x.borderImg))
                                blendMode = BlendMode.SrcAtop
                            }

                            // Create snapshot of group
                            val wr = new WritableImage(Prop.imgRes._1.toInt, Prop.imgRes._2.toInt)
                            val out = group.snapshot(new SnapshotParameters(), wr)

                            // Attempt to save image to disk
                            try {
                                ImageIO.write(
                                    SwingFXUtils.fromFXImage(out, null),
                                    FilenameUtils.getExtension(file.getAbsolutePath),
                                    file)
                                new Alert(AlertType.Information) {
                                    initOwner(stage)
                                    title = Prop.alertSuccess
                                    headerText = "Successfully saved image."
                                    contentText = "The image was saved successfully to " + file.getAbsolutePath
                                }.showAndWait()
                            } catch {
                                case e: Exception => createExceptionDialog(e,
                                    "Error saving image.", e.getMessage).showAndWait()
                            }
                        }
                    }
                },
                new Button(Prop.quitButton) {
                    prefWidth = Prop.buttonWidth
                    onAction = (_: ActionEvent) => {
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
      * Creates a new exception dialog.
      *
      * @param e Exception to create dialog for.
      * @param header Header of dialog.
      * @param content Content of dialog.
      * @return Alert.
      */
    private def createExceptionDialog(e: Exception, header: String, content: String): Alert = {
        val exceptionText = {
            val sw = new StringWriter()
            val pw = new PrintWriter(sw)
            e.printStackTrace(pw)
            sw.toString
        }

        val label = new Label("The exception stacktrace was:")

        val textArea = new TextArea {
            text = exceptionText
            editable = false
            wrapText = true
            maxWidth = Double.MaxValue
            maxHeight = Double.MaxValue
            vgrow = Priority.Always
            hgrow = Priority.Always
        }

        val expContent = new GridPane {
            maxWidth = Double.MaxValue
            add(label, 0, 0)
            add(textArea, 0, 1)
        }

        new Alert(AlertType.Error) {
            initOwner(stage)
            title = Prop.alertError
            headerText = header
            contentText = content
            dialogPane().expandableContent = expContent
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