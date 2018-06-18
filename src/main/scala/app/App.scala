package app

//======================================================================================================================
// Imports
//======================================================================================================================

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
import scala.io.Source

//======================================================================================================================
// App
//======================================================================================================================

/**
  * Dragon Creator ScalaFX application.
  * @author Seledrex, Sanuthem
  */
object App extends JFXApp {

    //==================================================================================================================
    // Application Variables
    //==================================================================================================================

    // Save file
    private var saveFile: File = _

    // Keeps track if user made changes
    private var madeChanges = false

    // Drag mode
    private val dragModeProp = new BooleanProperty(this, Prop.dragModePropName, false)

    // Base
    private val baseCBProp = new StringProperty(this, Prop.baseCBPropName, Res.baseDragon.name)
    private val baseCPProp = new ObjectProperty[javafx.scene.paint.Color](this, Prop.baseCPPropName, Color.White)
    val baseSet: Set[ImgElem] =
        Set(new ImgElem(Res.baseDragon),
            new ImgElem(Res.baseSquare),
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
    val imgList: List[(String, Seq[String], (Set[ImgElem], StringProperty, ObjectProperty[javafx.scene.paint.Color]))] =
        List(
            (Prop.bottomLabel,
                Seq(Prop.noneOption, Res.bottomSquare.name, Res.bottomCircle.name),
                (bottomSet, bottomCBProp, bottomCPProp)),
            (Prop.baseLabel,
                Seq(Res.baseDragon.name, Res.baseSquare.name, Res.baseCircle.name),
                (baseSet, baseCBProp, baseCPProp)),
            (Prop.topLabel,
                Seq(Prop.noneOption, Res.topSquare.name, Res.topCircle.name),
                (topSet, topCBProp, topCPProp)))


    // Set initial visibility
    imgList.foreach(x => x._3._1.find(img => img.name == x._3._2.get).get.visible(true))

    //==================================================================================================================
    // Property Listeners
    //==================================================================================================================

    // Set combo box change listeners
    imgList.foreach(x => x._3._2.onChange { (_, oldValue, newValue) =>
        if (newValue == Prop.noneOption) {
            x._3._1.find(img => img.name == oldValue).get.visible(false)
        } else if (oldValue == Prop.noneOption) {
            x._3._1.find(img => img.name == newValue).get.visible(true)
        } else {
            x._3._1.find(img => img.name == oldValue).get.visible(false)
            x._3._1.find(img => img.name == newValue).get.visible(true)
        }

        madeChanges = true
    })

    // Set color picker change listeners
    imgList.foreach(x => x._3._3.onChange { (_, _, newValue) =>
        x._3._1.foreach(img => img.changeColor(newValue))
        madeChanges = true
    })

    //==================================================================================================================
    // Stage
    //==================================================================================================================

    /**
      * Application stage. All user interface elements are contained
      * within this object.
      */
    stage = new JFXApp.PrimaryStage() {

        // Set parameters
        title = Prop.title
        resizable = true

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

    //==================================================================================================================
    // Panels
    //==================================================================================================================

    /**
      * Creates the options panel that houses the controls for
      * designing the dragon.
      * @return Node.
      */
    private def createOptionsPanel(): Node = {

        /**
          * Creates a control for a single layer.
          * @param label Layer name.
          * @param options Layer options.
          * @param props Property listeners.
          * @return
          */
        def createLayerControl(label: String,
                                 options: Seq[String],
                                 props: (StringProperty, ObjectProperty[javafx.scene.paint.Color])): Node = {

            // Create combo box
            val cb: ComboBox[String] = new ComboBox(options) {
                value = props._1.get
                prefWidth = Prop.pickerWidth
            }

            // Create color picker
            val cp: ColorPicker = new ColorPicker(props._2.value) {
                prefWidth = Prop.pickerWidth
            }

            // Bind combo box property to value
            props._1 <==> cb.value

            // Bind color picker property to value
            props._2 <==> cp.value

            // Organize vertically
            new VBox(Prop.padding) {
                children = Seq(
                    new Label(label),
                    cb,
                    cp
                )
            }
        }

        // Add all layer controls to panel
        new VBox(Prop.padding) {
            children = imgList.reverse.map(x => createLayerControl(x._1, x._2, (x._3._2, x._3._3)))
            style = Styles.panelStyle
        }
    }

    /**
      * Creates the image panel that displays the dragon being made
      * to the user.
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
      * @return Node.
      */
    private def createFilePanel(): Node = {
        new HBox(Prop.padding) {
            children = Seq(
                new Button(Prop.newButton) {
                    prefWidth = Prop.buttonWidth
                    onAction = (_: ActionEvent) => {
                        loadRawrFile(true)
                    }
                },
                new Button(Prop.openButton) {
                    prefWidth = Prop.buttonWidth
                    onAction = (_: ActionEvent) => {
                        loadRawrFile(false)
                    }
                },
                new Button(Prop.saveButton) {
                    prefWidth = Prop.buttonWidth
                    onAction = (_: ActionEvent) => {
                        saveRawrFile(false)
                    }
                },
                new Button(Prop.saveAsButton) {
                    prefWidth = Prop.buttonWidth
                    onAction = (_: ActionEvent) => {
                        saveRawrFile(true)
                    }
                },
                new Button(Prop.saveImageButton) {
                    prefWidth = Prop.buttonWidth
                    onAction = (_: ActionEvent) => {
                        saveImage()
                    }
                },
                new Button(Prop.quitButton) {
                    prefWidth = Prop.buttonWidth
                    onAction = (_: ActionEvent) => {
                        quit()
                    }
                }
            )
            style = Styles.panelStyle
        }
    }

    //==================================================================================================================
    // File Operations
    //==================================================================================================================

    /**
      * Loads a new rawr file from disk or reinitialized the program.
      * @param init True for reinitialization.
      */
    private def loadRawrFile(init: Boolean): Unit = {

        /**
          * Resets the images to default.
          */
        def reset(): Unit = {
            saveFile = null
            imgList.foreach(x => {
                x._3._2.value = x._2.head
                x._3._3.value = Color.White
            })
            madeChanges = false
        }

        /**
          * Loads a rawr file from disk.
          */
        def load(): Unit = {

            /**
              * Creates a bad format dialog.
              * @return Alert.
              */
            def createBadFormatDialog(): Alert = createExceptionDialog(
                new Exception("Incorrect file format."),
                "Could not load " + saveFile.getName + ".",
                "Incorrect file format.")

            // Create file chooser that only accepts rawr files
            val chooser = new FileChooser() {
                title = Prop.fileChooserTitle
                extensionFilters.addAll(
                    new FileChooser.ExtensionFilter("RAWR", "*.rawr"))
            }

            // Prompt user to choose file
            saveFile = chooser.showOpenDialog(stage)

            // Check if file was chosen
            if (saveFile != null) {

                // Ensure file extension
                if (FilenameUtils.getExtension(saveFile.getAbsolutePath) != "rawr") {
                    createBadFormatDialog().showAndWait()
                    return
                }

                // File format regex
                val regex = "(^[A-Za-z]+)=([A-Za-z]+);(#[0-9a-f]{6})".r

                // Parse each line of the file
                for (line <- Source.fromFile(saveFile).getLines()) {

                    // Check for matches
                    regex.findFirstMatchIn(line) match {
                        case Some(m) =>
                            // Set the corresponding layer
                            imgList.find(x => x._1 == m.subgroups.head) match {
                                case Some(layer) =>
                                    layer._3._2.value = m.subgroups(1)
                                    layer._3._3.value = Color.web(m.subgroups(2))
                                case None =>
                                    createBadFormatDialog().showAndWait(); return
                            }
                        case None =>
                            createBadFormatDialog().showAndWait(); return
                    }
                }

                madeChanges = false
            }
        }

        /**
          * Returns either reset or load.
          * @param init True for reinitialization.
          */
        def resetElseLoad(init: Boolean): Unit = if (init) reset() else load()

        // Check if changes were made to the current file
        if (madeChanges) {
            createSaveChangesDialog().showAndWait() match {
                case Some(ButtonType.Yes) => saveRawrFile(false); resetElseLoad(init)
                case Some(ButtonType.No) => resetElseLoad(init)
                case _ =>
            }
        } else resetElseLoad(init)
    }

    /**
      * Saves the current rawr file to disk.
      * @param saveAs True if doing Save As operation.
      */
    private def saveRawrFile(saveAs: Boolean): Unit = {

        /**
          * Converts a ScalaFX Color to its hex code.
          * @param color Color to convert.
          * @return Hex code string.
          */
        def colorToRGBCode(color: Color): String = {
            "#%02x%02x%02x" format ((color.red * 255).toInt, (color.green * 255).toInt, (color.blue * 255).toInt)
        }

        // Open file chooser if necessary
        if (saveFile == null || saveAs) {
            val chooser = new FileChooser() {
                title = Prop.fileChooserTitle
                extensionFilters.addAll(
                    new FileChooser.ExtensionFilter("RAWR", "*.rawr"))
            }

            saveFile = chooser.showSaveDialog(stage)
        }

        // Check if file is chosen
        if (saveFile != null) {

            // Ensure file extension
            if (FilenameUtils.getExtension(saveFile.getAbsolutePath) == "") {
                saveFile = new File(saveFile.getAbsolutePath + ".rawr")
            }

            var printWriter: PrintWriter = null

            // Attempt to write file to disk
            try {
                printWriter = new PrintWriter(saveFile)
                imgList.foreach(x => {
                    printWriter.write(x._1 + "=" + x._3._2.value + ";" + colorToRGBCode(x._3._3.value) + "\n")
                })
                new Alert(AlertType.Information) {
                    initOwner(stage)
                    title = Prop.alertSuccess
                    headerText = "Successfully saved file."
                    contentText = "The file was saved successfully to " + saveFile.getAbsolutePath
                }.showAndWait()
                madeChanges = false
            } catch {
                case e: Exception => createExceptionDialog(e, "Could not save file.", e.getMessage)
            } finally {
                printWriter.close()
            }
        }
    }

    /**
      * Saves what is currently being displayed in the GUI to disk as a
      * PNG image.
      */
    private def saveImage(): Unit = {

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

    /**
      * Quits the program and asks the user to save changes.
      */
    private def quit(): Unit = {
        if (madeChanges) {
            createSaveChangesDialog().showAndWait() match {
                case Some(ButtonType.Yes) => saveRawrFile(false);  System.exit(0)
                case Some(ButtonType.No) =>  System.exit(0)
                case _ =>
            }
        } else System.exit(0)
    }

    //==================================================================================================================
    // Dialogs
    //==================================================================================================================

    /**
      * Creates a new save changes dialog.
      * @return Alert.
      */
    private def createSaveChangesDialog(): Alert = {
        new Alert(AlertType.Confirmation) {
            initOwner(stage)
            title = Prop.alertConfirm
            headerText = "Would you like to save changes to the current file?"
            contentText = "You have made changes to " +
                { if (saveFile == null) "an unsaved file" else saveFile.getName } + "."
            buttonTypes = Seq(ButtonType.Yes, ButtonType.No, ButtonType.Cancel)
        }
    }

    /**
      * Creates a new exception dialog.
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

    //==================================================================================================================
    // Drag Mode
    //==================================================================================================================

    /**
      * Makes node draggable within a pane.
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