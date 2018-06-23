package app

//======================================================================================================================
// Imports
//======================================================================================================================

import java.io.{File, PrintWriter, StringWriter}
import javax.imageio.ImageIO
import javafx.scene.{paint => jfxp}
import javafx.scene.{layout => jfxl}
import javafx.scene.{text => jfxt}
import org.apache.commons.io.FilenameUtils
import org.controlsfx.dialog.FontSelectorDialog
import res.{Prop, Res, Styles}
import scalafx.Includes._
import scalafx.scene.paint.Color
import scalafx.application.JFXApp
import scalafx.beans.property._
import scalafx.embed.swing.SwingFXUtils
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.TextFormatter.Change
import scalafx.scene.control._
import scalafx.scene.effect.BlendMode
import scalafx.scene.image.WritableImage
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.text.{Font, Text}
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

    private val app = this
    private val propName = null

    // Save file
    private var saveFile: File = _

    // Properties
    private val madeChangesProp = new BooleanProperty(app, propName, false)
    private val dragModeProp = new BooleanProperty(app, propName, false)
    private val statusProp = new StringProperty(app, propName, Prop.statusLabel)
    private val resProp = new StringProperty(app, propName, Prop.imgResStr)

    // Base
    private val baseCBProp = new StringProperty(app, propName, Res.baseSquare.name)
    private val baseCPProp = new ObjectProperty[jfxp.Color](app, propName, Color.White)
    private val baseSet: Set[ImgElem] =
        Set(new ImgElem(Res.baseSquare),
            new ImgElem(Res.baseCircle),
            new ImgElem(Res.baseDragon))

    // Top
    private val topCBProp = new StringProperty(app, propName, Res.topSquare.name)
    private val topCPProp = new ObjectProperty[jfxp.Color](app, propName, Color.White)
    private val topSet: Set[ImgElem] =
        Set(new ImgElem(Res.topSquare),
            new ImgElem(Res.topCircle))

    // Bottom
    private val bottomCBProp = new StringProperty(app, propName, Res.bottomSquare.name)
    private val bottomCPProp = new ObjectProperty[jfxp.Color](app, propName, Color.White)
    private val bottomSet: Set[ImgElem] =
        Set(new ImgElem(Res.bottomSquare),
            new ImgElem(Res.bottomCircle))

    // Background
    private val backgroundCPProp = new ObjectProperty[jfxp.Color](app, propName, Color.web(Prop.defaultColor))
    private val backgroundFillProp = new ObjectProperty[jfxl.Background](app, propName,
        new jfxl.Background(new BackgroundFill(Color.web(Prop.defaultColor), CornerRadii.Empty, Insets.Empty)))
    private val backgroundImg = new ImgElem(Res.background)

    // Text
    private val titleTextFontProp = new ObjectProperty[jfxt.Font](app, propName, Font.font(Prop.defaultFont, Prop.titleFontSize))
    private val bodyTextFontProp = new ObjectProperty[jfxt.Font](app, propName, Font.font(Prop.defaultFont, Prop.bodyFontSize))
    private val titleTextProp = new StringProperty(app, propName, "")
    private val bodyTextProp = new StringProperty(app, propName, "")
    private val textCPProp = new ObjectProperty[jfxp.Color](app, propName, Color.Black)
    private val textFillProp = new ObjectProperty[jfxp.Paint](app, propName, Color.Black)

    // Put all sets into a list
    private val imgList: List[(String, Seq[String], (Set[ImgElem], StringProperty, ObjectProperty[jfxp.Color]))] =
        List(
            (Prop.bottomLabel,
                Seq(Res.bottomSquare.name, Res.bottomCircle.name, Prop.noneOption),
                (bottomSet, bottomCBProp, bottomCPProp)),
            (Prop.baseLabel,
                Seq(Res.baseSquare.name, Res.baseCircle.name, Res.baseDragon.name),
                (baseSet, baseCBProp, baseCPProp)),
            (Prop.topLabel,
                Seq(Res.topSquare.name, Res.topCircle.name, Prop.noneOption),
                (topSet, topCBProp, topCPProp)))

    // Set initial visibility
    imgList.foreach(x => x._3._1.find(img => img.name == x._3._2.get).get.visible(true))

    //==================================================================================================================
    // Property Listeners
    //==================================================================================================================

    // Combo box change listeners
    imgList.foreach(x => x._3._2.onChange { (_, oldValue, newValue) =>
        if (newValue == Prop.noneOption) {
            x._3._1.find(img => img.name == oldValue).get.visible(false)
        } else if (oldValue == Prop.noneOption) {
            x._3._1.find(img => img.name == newValue).get.visible(true)
        } else {
            x._3._1.find(img => img.name == oldValue).get.visible(false)
            x._3._1.find(img => img.name == newValue).get.visible(true)
        }

        madeChangesProp.value = true
    })

    // Color picker change listeners
    imgList.foreach(x => x._3._3.onChange { (_, _, newValue) =>
        x._3._1.foreach(img => img.changeColor(newValue))
        madeChangesProp.value = true
    })

    // Background change listener
    backgroundCPProp.onChange { (_, _, newValue) =>
        backgroundImg.changeColor(newValue)
        backgroundFillProp.value = new jfxl.Background(
            new BackgroundFill(newValue, new CornerRadii(7), Insets.Empty))
        madeChangesProp.value = true
    }

    // Status change listener
    madeChangesProp.onChange { (_, oldValue, madeChange) =>
        if (oldValue == false && madeChange) {
            if (saveFile != null) {
                statusProp.value = FilenameUtils.getBaseName(saveFile.getName) + "*"
            } else {
                statusProp.value = Prop.statusLabel + "*"
            }
        } else if (oldValue == true && !madeChange) {
            if (saveFile != null) {
                statusProp.value = FilenameUtils.getBaseName(saveFile.getName)
            } else {
                statusProp.value = Prop.statusLabel
            }
        }
    }

    // Resolution change listener
    resProp.onChange { (_, _, newValue) => {
        imgList.foreach(x => x._3._1.foreach(img => img.changeSize(convertRes(newValue))))
        backgroundImg.changeSize(convertRes(newValue))
        titleTextFontProp.value = Font.font(titleTextFontProp.value.getName, Prop.resMap(newValue)._1)
        bodyTextFontProp.value = Font.font(bodyTextFontProp.value.getName, Prop.resMap(newValue)._2)
    }}

    // Text change listeners
    titleTextProp.onChange { (_, _, _) => madeChangesProp.value = true }
    bodyTextProp.onChange { (_, _, _) => madeChangesProp.value = true }
    textCPProp.onChange { (_, _, newValue) => {
        textFillProp.value = newValue
        madeChangesProp.value = true
    }}
    titleTextFontProp.onChange { (_, _, newValue) => {
        Prop.titleFontSize = newValue.getSize
        madeChangesProp.value = true
    }}
    bodyTextFontProp.onChange { (_, _, newValue) => {
        Prop.bodyFontSize = newValue.getSize
        madeChangesProp.value = true
    }}

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
        maximized = true

        // Create panels
        val filePanel: Node = createFilePanel()
        val optionPanel: Node = makeDraggable(createOptionsPanel())
        val imagePanel: Node = makeDraggable(createImagePanel())
        val textPanel: Node = makeDraggable(createTextPanel())

        // Make window panel
        val windowPanel: Node = new Pane() {
            margin = Insets(Prop.padding)
            children = new HBox(Prop.padding) {
                children = Seq(
                    filePanel,
                    new HBox(Prop.padding) {
                        // Reset button
                        val panelResetButton: Button = new Button(Prop.resetButton) {
                            prefWidth = Prop.buttonWidth
                            onAction = (_: ActionEvent) => {
                                optionPanel.relocate(Prop.padding, 0)
                                imagePanel.relocate(150, 0)
                                resProp.value = Prop.imgResStr
                            }
                        }

                        // Resolution combo box
                        val imgResComboBox: ComboBox[String] = new ComboBox(Prop.resOptions) {
                            value = Prop.imgResStr
                            prefWidth = 105
                        }

                        // Create a checkbox to toggle drag mode
                        val dragModeCheckbox: CheckBox = new CheckBox(Prop.dragModeCheckBoxName) {
                            selected = dragModeProp()
                            prefWidth = Prop.buttonWidth
                        }

                        // Bind properties
                        dragModeProp <== dragModeCheckbox.selected
                        resProp <==> imgResComboBox.value

                        children = Seq(panelResetButton, imgResComboBox, dragModeCheckbox)
                        alignment = Pos.CenterLeft
                        style = Styles.panelStyle
                    },
                    new StackPane() {
                        // Status label
                        val status: Label = new Label(Prop.statusLabel) {
                            prefWidth = Prop.pickerWidth
                            alignmentInParent = Pos.Center
                            alignment = Pos.Center
                        }

                        // Bind property
                        status.text <== statusProp

                        children = Seq(status)
                        style = Styles.panelStyle
                    })
            }
        }

        // Create a pane that holds multiple panels
        val panelsPane: Pane = new Pane() {
            optionPanel.relocate(Prop.padding, 0)
            textPanel.relocate(150, 0)
            imagePanel.relocate(470, 0)
            children = Seq(imagePanel, textPanel, optionPanel)
            alignmentInParent = Pos.TopLeft
        }

        // Create scene containing all elements and proper resolution
        scene = new Scene(Prop.resolution._1, Prop.resolution._2) {
            root = new BorderPane() {
                center = panelsPane
                top = windowPanel
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
                               props: (StringProperty, ObjectProperty[jfxp.Color])): Node = {

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

        /**
          * Creates a control for just the background.
          * @return Node.
          */
        def createBackgroundControl: Node = {
            val cp: ColorPicker = new ColorPicker(Prop.defaultColor) {
                prefWidth = Prop.pickerWidth
            }

            backgroundCPProp <==> cp.value

            new VBox(Prop.padding) {
                children = Seq(
                    new Label(backgroundImg.name),
                    cp
                )
            }
        }

        // Add all layer controls to panel
        new VBox(Prop.padding) {
            children = imgList.reverse.map(x => createLayerControl(x._1, x._2, (x._3._2, x._3._3))) ++
                Seq(createBackgroundControl)
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
            children = Seq(backgroundImg.create) ++
                imgList.map(x => x._3._1).flatMap(set => set.toSeq).map(img => img.create) ++
                Seq(new VBox() {
                    children = Seq(
                        new Text("") {
                            font = Font.font(Prop.defaultFont, Prop.titleFontSize)
                            titleTextFontProp <==> font
                            titleTextProp <==> text
                            fill <== textFillProp
                        },
                        new Text("") {
                            font = Font.font(Prop.defaultFont, Prop.bodyFontSize)
                            bodyTextFontProp <==> font
                            bodyTextProp <==> text
                            fill <== textFillProp
                        }
                    )
                    relocate(10, 0)
                })
            alignmentInParent = Pos.TopLeft
            style = Styles.panelStyle
            backgroundFillProp <==> background
        }
    }

    /**
      * Creates the file panel which gives users basic controls.
      * @return Node.
      */
    private def createFilePanel(): HBox = {
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

    private def createTextPanel(): Node = {
        new VBox(Prop.padding) {
            children = Seq(
                new HBox(Prop.padding) {
                    children = Seq(
                        new TextField() {
                            promptText = Prop.titlePrompt
                            prefWidth = Prop.textAreaWidth - Prop.pickerWidth - 5
                            textFormatter = new TextFormatter[String]( {change: Change =>
                                val maxLen: Int = Prop.titleMaxLen
                                if (change.controlNewText.length > maxLen) {
                                    change.text = ""
                                }
                                if (change.anchor > maxLen) change.anchor = maxLen
                                if (change.caretPosition > maxLen) change.caretPosition = maxLen
                                change
                            })
                            titleTextProp <==> text
                        },
                        new Button(Prop.titleFontButton) {
                            prefWidth = Prop.pickerWidth
                            onAction = (_: ActionEvent) => {
                                val fs = new FontSelectorDialog(titleTextFontProp.value) {
                                    initOwner(stage)
                                    setTitle(Prop.dialogFontChooser)
                                }
                                val f = fs.showAndWait()
                                if (f.isPresent) {
                                    titleTextFontProp.value = f.get()
                                }
                            }
                        }
                    )
                },
                new TextArea() {
                    promptText = Prop.bodyPrompt
                    prefWidth = Prop.textAreaWidth
                    textFormatter = new TextFormatter[String]( {change: Change =>
                        val maxLen: Int = Prop.bodyMaxLen; val maxLines: Int = Prop.bodyMaxLines
                        def makeChange(change: Change): Unit = {
                            change.text = ""
                            change.anchor = change.anchor - 1
                            change.caretPosition = change.caretPosition - 1
                        }
                        val split = change.controlNewText.split("[\n]")
                        if ((for {c <- change.controlNewText.filter(x => x == '\n')} yield c).length > maxLines)
                            makeChange(change)
                        split.foreach (str => if (str.length > maxLen) makeChange(change))
                        change
                    })
                    bodyTextProp <==> text
                },
                new HBox(Prop.padding) {
                    children = Seq(
                        new ColorPicker(Color.Black) {
                            prefWidth = Prop.textAreaWidth - Prop.pickerWidth - 5
                            textCPProp <==> value
                        },
                        new Button(Prop.bodyFontButton) {
                            prefWidth = Prop.pickerWidth
                            onAction = (_: ActionEvent) => {
                                val fs = new FontSelectorDialog(bodyTextFontProp.value) {
                                    initOwner(stage)
                                    setTitle(Prop.dialogFontChooser)
                                }
                                val f = fs.showAndWait()
                                if (f.isPresent) {
                                    bodyTextFontProp.value = f.get()
                                }
                            }
                        }
                    )
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
            backgroundCPProp.value = Color.web(Prop.defaultColor)
            madeChangesProp.value = false
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
                val elemRx = "(^[A-Za-z]+)=([A-Za-z]+);(#[0-9a-f]{6})"
                val bgRx = "(^[A-Za-z]+)=(#[0-9a-f]{6})"

                // Parse each line of the file
                for (line <- Source.fromFile(saveFile).getLines()) {
                    if (line matches elemRx) {
                        elemRx.r.findFirstMatchIn(line) match {
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
                        }
                    }
                    else if (line matches bgRx) {
                        bgRx.r.findFirstMatchIn(line) match {
                            case Some(bg) =>
                                backgroundCPProp.value = Color.web(bg.subgroups(1))
                            case None =>
                        }
                    }
                    else {
                        createBadFormatDialog().showAndWait(); return
                    }
                }

                madeChangesProp.value = false
            }
        }

        /**
          * Returns either reset or load.
          * @param init True for reinitialization.
          */
        def resetElseLoad(init: Boolean): Unit = if (init) reset() else load()

        // Check if changes were made to the current file
        if (madeChangesProp.value) {
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
                printWriter.write(backgroundImg.name + "=" + colorToRGBCode(backgroundImg.color) + "\n")
                new Alert(AlertType.Information) {
                    initOwner(stage)
                    title = Prop.alertSuccess
                    headerText = "Successfully saved file."
                    contentText = "The file was saved successfully to " + saveFile.getAbsolutePath
                }.showAndWait()
                madeChangesProp.value = false
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

        // Create resolution choice dialog
        val resChoiceDialog = new ChoiceDialog(defaultChoice = Prop.imgResStr, choices = Prop.resOptions) {
            initOwner(stage)
            title = Prop.dialogResChoice
            headerText = "Select the resolution to output to."
            contentText = "Resolution:"
        }

        // Get choice
        val resChoice = resChoiceDialog.showAndWait() match {
            case Some(choice) => convertRes(choice)
            case None => return
        }

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
                children = Seq({
                    val copy = new ImgElem(Res.background)
                    copy.visible(true)
                    copy.changeColor(backgroundImg.color)
                    copy.changeSize(resChoice)
                    copy
                }).flatMap(x => Seq(x.fillImg, x.borderImg)) ++
                    imgList.map(x => x._3._1)
                        .flatMap(set => set.toSeq)
                        .filter(img => img.isVisible)
                        .map(img => (img.resource, img.color))
                        .map(x => {
                            val copy = new ImgElem(x._1)
                            copy.visible(true)
                            copy.changeColor(x._2)
                            copy.changeSize(resChoice)
                            copy })
                        .flatMap(x => Seq(x.fillImg, x.borderImg))
                blendMode = BlendMode.SrcAtop
            }

            // Create snapshot of group
            val wr = new WritableImage(resChoice._1.toInt, resChoice._2.toInt)
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
        if (madeChangesProp.value) {
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
                { if (saveFile == null) "an unsaved file" else FilenameUtils.getBaseName(saveFile.getName) } + "."
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
                            case MouseEvent.MouseReleased =>
                                // @TODO Does not auto-snap from start of program or res-change (window or image pane)
                                // @TODO Make this a def for window/pane resize too?
                                // @TODO Possibly prevent panes from being dragged outside of window borders instead
                                var snap = true
                                if (snap) {
                                    val bounds = node.localToScene(node.getBoundsInLocal)
                                    if (bounds.getMinX < 0) {
                                        node.layoutX.value -= bounds.getMinX
                                    }
                                    if (bounds.getMinY < 0) {
                                        node.layoutY.value -= bounds.getMinY
                                    }
                                    if (bounds.getMaxX > stage.width()) {
                                        node.layoutX.value -= bounds.getMaxX - stage.width() + 16
                                        // Stage width is 16 pixels larger than the window width
                                    }
                                    if (bounds.getMaxY > stage.height()) {
                                        node.layoutY.value -= bounds.getMaxY - stage.height() + 39
                                        // Stage height is 39 pixels larger than the window height
                                    }
                                }
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

    //==================================================================================================================
    // Miscellaneous
    //==================================================================================================================

    /**
      * Converts an image resolution string into a tuple of doubles. If the passed
      * in string does not have the correct format, the function will return the
      * default image resolution tuple.
      * @param resStr String to convert from.
      * @return Tuple of doubles.
      */
    private def convertRes(resStr: String): (Double, Double) = {
        "([0-9]+)×([0-9]+)".r.findFirstMatchIn(resStr) match {
            case Some(res) => (res.subgroups.head.toDouble, res.subgroups(1).toDouble)
            case None => Prop.imgRes
        }
    }
}