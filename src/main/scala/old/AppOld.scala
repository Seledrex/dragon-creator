package old

//======================================================================================================================
// Imports
//======================================================================================================================

import java.io.{File, PrintWriter, StringWriter}

import app.{ColorChooser, Util}
import javafx.scene.{layout => jfxl, paint => jfxp, text => jfxt}
import javax.imageio.ImageIO
import org.apache.commons.io.FilenameUtils
import org.controlsfx.dialog.FontSelectorDialog
import res.Res
import scalafx.Includes._
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
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontPosture, FontWeight, Text}
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
object AppOld extends JFXApp {

    //==================================================================================================================
    // Application Variables
    //==================================================================================================================

    private val bean = this
    private val propName = null

    // Save file
    private var saveFile: File = _

    // Properties
    private val madeChangesProp = new BooleanProperty(bean, propName, false)
    private val dragModeProp = new BooleanProperty(bean, propName, false)
    private val statusProp = new StringProperty(bean, propName, PropertiesOld.statusLabel)
    private val resProp = new StringProperty(bean, propName, PropertiesOld.imgResStr)

    // Base
    private val baseCBProp = new StringProperty(bean, propName, "")
    private val baseCPProp = new ObjectProperty[jfxp.Color](bean, propName, Color.White)
    private val baseSet: Set[ImgElem] =
        Set(new ImgElem(Res.baseDragon))

    // Top
    private val topCBProp = new StringProperty(bean, propName, "")
    private val topCPProp = new ObjectProperty[jfxp.Color](bean, propName, Color.White)
    private val topSet: Set[ImgElem] = Set.empty

    // Bottom
    private val bottomCBProp = new StringProperty(bean, propName, "")
    private val bottomCPProp = new ObjectProperty[jfxp.Color](bean, propName, Color.White)
    private val bottomSet: Set[ImgElem] = Set.empty

    // Background
    private val backgroundCPProp = new ObjectProperty[jfxp.Color](bean, propName, Color.web(PropertiesOld.defaultColor))
    private val backgroundFillProp = new ObjectProperty[jfxl.Background](bean, propName,
        new jfxl.Background(new BackgroundFill(Color.web(PropertiesOld.defaultColor), CornerRadii.Empty, Insets.Empty)))
    private val backgroundImg = new ImgElem(Res.baseDragon)

    // Text
    private val titleFontScaleProp = new ObjectProperty[jfxt.Font](bean, propName,
        adjustFontSize(PropertiesOld.getDefaultTitleFont, PropertiesOld.getScaleFactor(PropertiesOld.imgResStr)))
    private val titleFPProp = new ObjectProperty[jfxt.Font](bean, propName, PropertiesOld.getDefaultTitleFont)
    private val bodyFontScaleProp = new ObjectProperty[jfxt.Font](bean, propName,
        adjustFontSize(PropertiesOld.getDefaultBodyFont, PropertiesOld.getScaleFactor(PropertiesOld.imgResStr)))
    private val bodyFPProp = new ObjectProperty[jfxt.Font](bean, propName, PropertiesOld.getDefaultBodyFont)
    private val titleTextProp = new StringProperty(bean, propName, "")
    private val bodyTextProp = new StringProperty(bean, propName, "")
    private val textCPProp = new ObjectProperty[jfxp.Color](bean, propName, Color.Black)
    private val textFillProp = new ObjectProperty[jfxp.Paint](bean, propName, Color.Black)

    // Put all sets into a list
    private val imgList: List[(String, Seq[String], (Set[ImgElem], StringProperty, ObjectProperty[jfxp.Color]))] =
        List(
            (PropertiesOld.bottomLabel,
                Seq("", "", PropertiesOld.noneOption),
                (bottomSet, bottomCBProp, bottomCPProp)),
            (PropertiesOld.baseLabel,
                Seq("", "", Res.baseDragon.name),
                (baseSet, baseCBProp, baseCPProp)),
            (PropertiesOld.topLabel,
                Seq("", "", PropertiesOld.noneOption),
                (topSet, topCBProp, topCPProp)))

    // Set initial visibility
    imgList.foreach(x => x._3._1.find(img => img.name == x._3._2.get).get.visible(true))

    // Color Chooser and Color Palette
    private val colorChooser: ColorChooser = new ColorChooser() {
        styleClass.add("panel-style")
    }

    //==================================================================================================================
    // Property Listeners
    //==================================================================================================================

    // Combo box change listeners
    imgList.foreach(x => x._3._2.onChange { (_, oldValue, newValue) =>
        if (newValue == PropertiesOld.noneOption) {
            x._3._1.find(img => img.name == oldValue).get.visible(false)
        } else if (oldValue == PropertiesOld.noneOption) {
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
                statusProp.value = PropertiesOld.statusLabel + "*"
            }
        } else if (oldValue == true && !madeChange) {
            if (saveFile != null) {
                statusProp.value = FilenameUtils.getBaseName(saveFile.getName)
            } else {
                statusProp.value = PropertiesOld.statusLabel
            }
        }
    }

    // Resolution change listener
    resProp.onChange { (_, _, newValue) => {
        imgList.foreach(x => x._3._1.foreach(img => img.changeSize(convertRes(newValue))))
        backgroundImg.changeSize(convertRes(newValue))
        titleFontScaleProp.value = adjustFontSize(titleFPProp.value, PropertiesOld.getScaleFactor(resProp.value))
        bodyFontScaleProp.value = adjustFontSize(bodyFPProp.value, PropertiesOld.getScaleFactor(resProp.value))
    }}

    // Text change listeners
    titleTextProp.onChange { (_, _, _) => madeChangesProp.value = true }
    bodyTextProp.onChange { (_, _, _) => madeChangesProp.value = true }

    // Text color change listener
    textCPProp.onChange { (_, _, newValue) => {
        textFillProp.value = newValue
        madeChangesProp.value = true
    }}

    // Text font change listeners
    titleFPProp.onChange { (_, _, newValue) =>
        titleFontScaleProp.value = adjustFontSize(newValue, PropertiesOld.getScaleFactor(resProp.value))
        madeChangesProp.value = true
    }

    bodyFPProp.onChange { (_, _, newValue) =>
        bodyFontScaleProp.value = adjustFontSize(newValue, PropertiesOld.getScaleFactor(resProp.value))
        madeChangesProp.value = true
    }

    //==================================================================================================================
    // Stage
    //==================================================================================================================

    /**
      * Application stage. All user interface elements are contained
      * within this object.
      */
    stage = new JFXApp.PrimaryStage() {

        // Set parameters
        title = PropertiesOld.Title
        resizable = true
        maximized = true

        // Create panels
        val filePanel: Node = createFilePanel()
        val optionPanel: Node = makeDraggable(createOptionsPanel())
        val imagePanel: Node = makeDraggable(createImagePanel())
        val textPanel: Node = makeDraggable(createTextPanel())

        val colorChooserPanel: Node = makeDraggable(colorChooser)

        // Make window panel
        val windowPanel: Node = new Pane() {
            margin = Insets(PropertiesOld.padding)
            children = new HBox(PropertiesOld.padding) {
                children = Seq(
                    filePanel,
                    new HBox(PropertiesOld.padding) {
                        // Reset button
                        val panelResetButton: Button = new Button(PropertiesOld.resetButton) {
                            prefWidth = PropertiesOld.buttonWidth
                            onAction = (_: ActionEvent) => {
                                optionPanel.relocate(PropertiesOld.optionPanelX, PropertiesOld.optionPanelY)
                                textPanel.relocate(PropertiesOld.textPanelX, PropertiesOld.textPanelY)
                                imagePanel.relocate(PropertiesOld.imagePanelX, PropertiesOld.imagePanelY)
                                colorChooserPanel.relocate(PropertiesOld.colorChooserPanelX, PropertiesOld.colorChooserPanelY)
                                resProp.value = PropertiesOld.imgResStr
                            }
                        }

                        // Resolution combo box
                        val imgResComboBox: ComboBox[String] = new ComboBox(PropertiesOld.resOptions) {
                            value = PropertiesOld.imgResStr
                            prefWidth = 105
                        }

                        // Create a checkbox to toggle drag mode
                        val dragModeCheckbox: CheckBox = new CheckBox(PropertiesOld.dragModeCheckBoxName) {
                            selected = dragModeProp()
                            prefWidth = PropertiesOld.buttonWidth
                        }

                        // Bind properties
                        dragModeProp <== dragModeCheckbox.selected
                        resProp <==> imgResComboBox.value

                        children = Seq(panelResetButton, imgResComboBox, dragModeCheckbox)
                        alignment = Pos.CenterLeft
                        styleClass.add("panel-style")
                    },
                    new StackPane() {
                        // Status label
                        val status: Label = new Label(PropertiesOld.statusLabel) {
                            prefWidth = PropertiesOld.pickerWidth
                            alignmentInParent = Pos.Center
                            alignment = Pos.Center
                        }

                        // Bind property
                        status.text <== statusProp

                        children = Seq(status)
                        styleClass.add("panel-style")
                    })
            }
        }

        // Create a pane that holds multiple panels
        val panelsPane: Pane = new Pane() {
            optionPanel.relocate(PropertiesOld.optionPanelX, PropertiesOld.optionPanelY)
            textPanel.relocate(PropertiesOld.textPanelX, PropertiesOld.textPanelY)
            imagePanel.relocate(PropertiesOld.imagePanelX, PropertiesOld.imagePanelY)
            colorChooserPanel.relocate(PropertiesOld.colorChooserPanelX, PropertiesOld.colorChooserPanelY)
            children = Seq(imagePanel, textPanel, colorChooserPanel, optionPanel)
            alignmentInParent = Pos.TopLeft
        }

        // Create scene containing all elements and proper resolution
        scene = new Scene(PropertiesOld.Resolution._1, PropertiesOld.Resolution._2) {
            stylesheets.add("css/styles.css")
            root = new BorderPane() {
                center = panelsPane
                top = windowPanel
                styleClass.add("background-style")
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
                prefWidth = PropertiesOld.pickerWidth
            }

            // Create color picker
            val button: Button = new Button(PropertiesOld.applyColorButton) {
                prefWidth = PropertiesOld.pickerWidth
                onAction = { _: ActionEvent =>
                    props._2.value = colorChooser.value.value
                }
            }

            // Bind combo box property to value
            props._1 <==> cb.value

            // Organize vertically
            new VBox(PropertiesOld.padding) {
                children = Seq(
                    new Label(label),
                    cb,
                    button
                )
            }
        }

        /**
          * Creates a control for just the background.
          * @return Node.
          */
        def createBackgroundControl: Node = {
            val button: Button = new Button(PropertiesOld.applyColorButton) {
                prefWidth = PropertiesOld.pickerWidth
                onAction = { _: ActionEvent =>
                    backgroundCPProp.value = colorChooser.value.value
                }
            }

            new VBox(PropertiesOld.padding) {
                children = Seq(
                    new Label(backgroundImg.name),
                    button
                )
            }
        }

        // Add all layer controls to panel
        new VBox(PropertiesOld.padding) {
            children = imgList.reverse.map(x => createLayerControl(x._1, x._2, (x._3._2, x._3._3))) ++
                Seq(createBackgroundControl)
            styleClass.add("panel-style")
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
                            font = titleFontScaleProp.value
                            titleTextProp <==> text
                            font <== titleFontScaleProp
                            fill <== textFillProp
                        },
                        new Text("") {
                            font = bodyFontScaleProp.value
                            bodyTextProp <==> text
                            font <== bodyFontScaleProp
                            fill <== textFillProp
                        }
                    )
                    relocate(10, 0)
                })
            alignmentInParent = Pos.TopLeft
            styleClass.add("panel-style")
            backgroundFillProp <==> background
        }
    }

    /**
      * Creates the file panel which gives users basic controls.
      * @return Node.
      */
    private def createFilePanel(): HBox = {
        new HBox(PropertiesOld.padding) {
            children = Seq(
                new Button(PropertiesOld.newButton) {
                    prefWidth = PropertiesOld.buttonWidth
                    onAction = (_: ActionEvent) => {
                        loadRawrFile(true)
                    }
                },
                new Button(PropertiesOld.openButton) {
                    prefWidth = PropertiesOld.buttonWidth
                    onAction = (_: ActionEvent) => {
                        loadRawrFile(false)
                    }
                },
                new Button(PropertiesOld.saveButton) {
                    prefWidth = PropertiesOld.buttonWidth
                    onAction = (_: ActionEvent) => {
                        saveRawrFile(false)
                    }
                },
                new Button(PropertiesOld.saveAsButton) {
                    prefWidth = PropertiesOld.buttonWidth
                    onAction = (_: ActionEvent) => {
                        saveRawrFile(true)
                    }
                },
                new Button(PropertiesOld.saveImageButton) {
                    prefWidth = PropertiesOld.buttonWidth
                    onAction = (_: ActionEvent) => {
                        saveImage()
                    }
                },
                new Button(PropertiesOld.quitButton) {
                    prefWidth = PropertiesOld.buttonWidth
                    onAction = (_: ActionEvent) => {
                        quit()
                    }
                }
            )
            styleClass.add("panel-style")
        }
    }

    /**
      * Creates the text panel that lets users edit text on the image panel.
      * @return Node.
      */
    private def createTextPanel(): Node = {
        new VBox(PropertiesOld.padding) {
            children = Seq(
                new HBox(PropertiesOld.padding) {
                    children = Seq(
                        new TextField() {
                            promptText = PropertiesOld.titlePrompt
                            prefWidth = PropertiesOld.textAreaWidth - PropertiesOld.pickerWidth - 5
                            textFormatter = new TextFormatter[String]( {change: Change =>
                                val maxLen: Int = PropertiesOld.titleMaxLen
                                if (change.controlNewText.length > maxLen) {
                                    change.text = ""
                                }
                                if (change.anchor > maxLen) change.anchor = maxLen
                                if (change.caretPosition > maxLen) change.caretPosition = maxLen
                                change
                            })
                            titleTextProp <==> text
                        },
                        new Button(PropertiesOld.titleFontButton) {
                            prefWidth = PropertiesOld.pickerWidth
                            onAction = (_: ActionEvent) => {
                                val fs = new FontSelectorDialog(titleFPProp.value) {
                                    initOwner(stage)
                                    setTitle(PropertiesOld.dialogFontChooser)
                                }
                                val f = fs.showAndWait()
                                if (f.isPresent) {
                                    titleFPProp.value = f.get()
                                }
                            }
                        }
                    )
                },
                new TextArea() {
                    promptText = PropertiesOld.bodyPrompt
                    prefWidth = PropertiesOld.textAreaWidth
                    textFormatter = new TextFormatter[String]( {change: Change =>
                        val maxLen: Int = PropertiesOld.bodyMaxLen; val maxLines: Int = PropertiesOld.bodyMaxLines
                        def makeChange(change: Change): Unit = {
                            change.text = ""
                            change.anchor = if (change.anchor > 0) change.anchor - 1 else 0
                            change.caretPosition = if (change.caretPosition > 0) change.caretPosition - 1 else 0
                        }
                        val split = change.controlNewText.split("[\n]")
                        if ((for {c <- change.controlNewText.filter(x => x == '\n')} yield c).length > maxLines)
                            makeChange(change)
                        split.foreach(str => if (str.length > maxLen) makeChange(change))
                        change
                    })
                    bodyTextProp <==> text
                },
                new HBox(PropertiesOld.padding) {
                    children = Seq(
                        new Button(PropertiesOld.applyColorButton) {
                            prefWidth = PropertiesOld.textAreaWidth - PropertiesOld.pickerWidth - 5
                            onAction = { _: ActionEvent =>
                                textCPProp.value = colorChooser.value.value
                            }
                        },
                        new Button(PropertiesOld.bodyFontButton) {
                            prefWidth = PropertiesOld.pickerWidth
                            onAction = (_: ActionEvent) => {
                                val fs = new FontSelectorDialog(bodyFPProp.value) {
                                    initOwner(stage)
                                    setTitle(PropertiesOld.dialogFontChooser)
                                }
                                val f = fs.showAndWait()
                                if (f.isPresent) {
                                    bodyFPProp.value = f.get()
                                }
                            }
                        }
                    )
                }
            )
            styleClass.add("panel-style")
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
            // Reset save file
            saveFile = null

            // Reset image panel
            imgList.foreach(x => {
                x._3._2.value = x._2.head
                x._3._3.value = Color.White
            })

            // Reset background
            backgroundCPProp.value = Color.web(PropertiesOld.defaultColor)

            // Reset text
            titleTextProp.value = ""
            bodyTextProp.value = ""
            titleFPProp.value = PropertiesOld.getDefaultTitleFont
            bodyFPProp.value = PropertiesOld.getDefaultBodyFont
            textCPProp.value = Color.Black

            // Reset made changes
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
            def createBadFormatDialog(fileName: String): Alert = createExceptionDialog(
                new Exception("Incorrect file format."),
                "Could not load " + fileName + ".",
                "Incorrect file format.")

            // Create file chooser that only accepts rawr files
            val chooser = new FileChooser() {
                title = PropertiesOld.fileChooserTitle
                extensionFilters.addAll(
                    new FileChooser.ExtensionFilter("RAWR", "*.rawr"))
            }

            // Prompt user to choose file
            saveFile = chooser.showOpenDialog(stage)

            // Check if file was chosen
            if (saveFile != null) {

                // Ensure file extension
                if (FilenameUtils.getExtension(saveFile.getAbsolutePath) != "rawr") {
                    createBadFormatDialog(saveFile.getName).showAndWait()
                    return
                }

                // File format regex
                val elemRx = "(^[A-Za-z]+)=([A-Za-z]+);(#[0-9a-f]{6})"
                val bgRx = "Background=(#[0-9a-f]{6})"
                val ttRx = "TitleText=(.*)"
                val tfRx = "TitleFont=([^;\n]+);([^;\n]+);(\\d*\\.?\\d*)"
                val btRx = "BodyText=(.*)"
                val bfRx = "BodyFont=([^;\n]+);([^;\n]+);(\\d*\\.?\\d*)"
                val tcRx = "TextColor=(#[0-9a-f]{6})"

                // Parse each line of the file
                for (line <- Source.fromFile(saveFile).getLines()) {
                    line match {
                        // Found image element
                        case ln if ln matches elemRx =>
                            elemRx.r.findFirstMatchIn(line) match {
                                case Some(m) =>
                                    imgList.find(x => x._1 == m.subgroups.head) match {
                                        case Some(layer) =>
                                            layer._3._2.value = m.subgroups(1)
                                            layer._3._3.value = Color.web(m.subgroups(2))
                                        case None =>
                                            createBadFormatDialog(saveFile.getName).showAndWait(); return
                                    }
                                case None =>
                            }
                        // Found background
                        case ln if ln matches bgRx =>
                            bgRx.r.findFirstMatchIn(line) match {
                                case Some(bg) => backgroundCPProp.value = Color.web(bg.subgroups.head)
                                case None =>
                            }
                        // Found title text
                        case ln if ln matches ttRx =>
                            ttRx.r.findFirstMatchIn(line) match {
                                case Some(tt) => titleTextProp.value = tt.subgroups.head
                                case None =>
                            }
                        // Found title font
                        case ln if ln matches tfRx =>
                            tfRx.r.findFirstMatchIn(line) match {
                                case Some(tf) => titleFPProp.value = loadFont(tf.subgroups.head,
                                    tf.subgroups(1), tf.subgroups(2).toDouble, 1)
                                case None =>
                            }
                        // Found body text
                        case ln if ln matches btRx =>
                            btRx.r.findFirstMatchIn(line) match {
                                case Some(bt) =>
                                    bodyTextProp.value = bt.subgroups.head.replaceAll("NEWLINE", "\n")
                                case None =>
                            }
                        // Found body font
                        case ln if ln matches bfRx =>
                            bfRx.r.findFirstMatchIn(line) match {
                                case Some(bf) => bodyFPProp.value = loadFont(bf.subgroups.head,
                                    bf.subgroups(1), bf.subgroups(2).toDouble, 1)
                                case None =>
                            }
                        // Found text fill color
                        case ln if ln matches tcRx =>
                            tcRx.r.findFirstMatchIn(line) match {
                                case Some(tf) => textCPProp.value = Color.web(tf.subgroups.head)
                                case None =>
                            }
                        case _ =>
                            println(line)
                            val fileName = saveFile.getName
                            reset(); createBadFormatDialog(fileName).showAndWait(); return
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

        // Open file chooser if necessary
        if (saveFile == null || saveAs) {
            val chooser = new FileChooser() {
                title = PropertiesOld.fileChooserTitle
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
                    printWriter.write(x._1 + "=" + x._3._2.value + ";" + Util.colorToRGBCode(x._3._3.value) + "\n")
                })
                printWriter.write("Background=" + Util.colorToRGBCode(backgroundImg.color) + "\n")
                printWriter.write("TitleText=" + titleTextProp.value + "\n")
                printWriter.write("TitleFont=" + titleFPProp.value.getFamily + ";" + titleFPProp.value.getStyle + ";" + titleFPProp.value.getSize + "\n")
                printWriter.write("BodyText=" + bodyTextProp.value.replaceAll("\n", "NEWLINE") + "\n")
                printWriter.write("BodyFont=" + bodyFPProp.value.getFamily + ";" + bodyFPProp.value.getStyle + ";" + bodyFPProp.value.getSize + "\n")
                printWriter.write("TextColor=" + Util.colorToRGBCode(textCPProp.value) + "\n")
                new Alert(AlertType.Information) {
                    initOwner(stage)
                    title = PropertiesOld.alertSuccess
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
        val resChoiceDialog = new ChoiceDialog(defaultChoice = PropertiesOld.imgResStr, choices = PropertiesOld.resOptions) {
            initOwner(stage)
            title = PropertiesOld.dialogResChoice
            headerText = "Select the resolution to output to."
            contentText = "Resolution:"
        }

        // Get choice
        val resChoice = resChoiceDialog.showAndWait() match {
            case Some(choice) => choice
            case None => return
        }

        // Create file chooser
        val chooser = new FileChooser() {
            title = PropertiesOld.fileChooserTitle
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

            def copyBackground(): Seq[Node] = {
                Seq(new ImgElem(Res.baseDragon) {
                        visible(true)
                        changeColor(backgroundImg.color)
                        changeSize(convertRes(resChoice))
                    }
                ).flatMap(x => Seq(x.fillImg, x.borderImg))
            }

            def copyImage(): Seq[Node] = {
                imgList.map(x => x._3._1)
                    .flatMap(set => set.toSeq)
                    .filter(img => img.isVisible)
                    .map(img => (img.resource, img.color))
                    .map(x =>
                        new ImgElem(x._1) {
                            visible(true)
                            changeColor(x._2)
                            changeSize(convertRes(resChoice))
                        })
                    .flatMap(x => Seq(x.fillImg, x.borderImg))
            }

            def copyText(): Seq[Node] = {
                Seq(new VBox() {
                    children = Seq(
                        new Text(titleTextProp.value) {
                            font = adjustFontSize(titleFPProp.value,  PropertiesOld.getScaleFactor(resChoice))
                            fill = textFillProp.value
                        },
                        new Text(bodyTextProp.value) {
                            font = adjustFontSize(bodyFPProp.value,  PropertiesOld.getScaleFactor(resChoice))
                            fill = textFillProp.value
                        }
                    )
                    relocate(10, 0)
                })
            }

            // Group together visible elements and make copies
            val group: Group = new Group() {
                children = copyBackground() ++ copyImage() ++ copyText()
                blendMode = BlendMode.SrcAtop
            }

            // Create snapshot of group
            val wr = new WritableImage(convertRes(resChoice)._1.toInt, convertRes(resChoice)._2.toInt)
            val out = group.snapshot(new SnapshotParameters(), wr)

            // Attempt to save image to disk
            try {
                ImageIO.write(
                    SwingFXUtils.fromFXImage(out, null),
                    FilenameUtils.getExtension(file.getAbsolutePath),
                    file)
                new Alert(AlertType.Information) {
                    initOwner(stage)
                    title = PropertiesOld.alertSuccess
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
            title = PropertiesOld.alertConfirm
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
            title = PropertiesOld.alertError
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
                                val bounds = node.localToScene(node.getBoundsInLocal)
                                //println(s"bounds.getMinX: ${bounds.getMinX}, bounds.getMinY: ${bounds.getMinY - 51}")
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
                                val snap = true
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
            case None => PropertiesOld.imgRes
        }
    }

    /**
      * Adjusts the font size of an existing font.
      * @param font Font to adjust.
      * @param multiplier Multiplier to adjust size by.
      * @return New font with correct style and size.
      */
    private def adjustFontSize(font: jfxt.Font, multiplier: Double): jfxt.Font = {
        loadFont(font.getFamily, font.getStyle, font.getSize, multiplier)
    }

    /**
      * Loads a font from a give font family, style, size, and multiplier.
      * @param family Font family.
      * @param style Font style.
      * @param size Font size.
      * @param multiplier Multiplier to adjust size by.
      * @return New font with correct style and size.
      */
    private def loadFont(family: String, style: String, size: Double, multiplier: Double): jfxt.Font = {
        val weight: FontWeight = style match {
            case s if s.contains("Bold") => FontWeight.Bold
            case _ => FontWeight.Normal
        }
        val posture: FontPosture = style match {
            case s if s.contains("Italic") => FontPosture.Italic
            case _ => FontPosture.Regular
        }
        Font.font(family, weight, posture, size * multiplier)
    }
}