package app

//======================================================================================================================
// Imports
//======================================================================================================================

import java.io._

import javafx.{scene => jfxs}
import javax.imageio.ImageIO
import org.apache.commons.io.FilenameUtils
import res.Res
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.embed.swing.SwingFXUtils
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.image.WritableImage
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.{Group, Node, Scene, SnapshotParameters}
import scalafx.stage.FileChooser

import scala.util.{Failure, Success, Try}

//======================================================================================================================
// App
//======================================================================================================================

object App extends JFXApp {

  private final val bean = this
  private final val propName = null

  //====================================================================================================================
  // Properties
  //====================================================================================================================

  private val dragToolProp = new BooleanProperty(bean, propName, false)
  private val moveToolProp = new BooleanProperty(bean, propName, true)
  private val resolutionProp = new StringProperty(bean, propName, Properties.ResPropInit)
  private val selectedLayerProp = new ObjectProperty[jfxs.Group](bean, propName, null)
  private val statusProp = new StringProperty(bean, propName, Properties.StatusLabel)
  private val saveFile = new ObjectProperty[File](bean, propName, null)
  private val madeChangesProp = new BooleanProperty(bean, propName, false)

  //====================================================================================================================
  // Application Variables
  //====================================================================================================================

  private val creatorPane = new Pane() {
    styleClass.add("panel-style")
    minWidth = Properties.getResTuple(resolutionProp())._1
    maxWidth = Properties.getResTuple(resolutionProp())._1
    minHeight = Properties.getResTuple(resolutionProp())._2
    maxHeight = Properties.getResTuple(resolutionProp())._2
    relocate(Properties.CreatorPaneX, Properties.CreatorPaneY)
  }

  private val colorChooser = new ColorChooser() {
    styleClass.add("panel-style")
  }

  private val colorChooserPane = new Pane() {
    children = Seq(colorChooser)
    relocate(Properties.ColorChooserPaneX, Properties.ColorChooserPaneY)
  }

  //====================================================================================================================
  // Change Listeners
  //====================================================================================================================

  resolutionProp.onChange { (_, oldValue, newValue) =>
    val oldRes = Properties.getResTuple(oldValue)
    val newRes = Properties.getResTuple(newValue)
    val scaleFactor = newRes._1 / oldRes._1

    val layers = getLayers
    layers.foreach { layer =>
      layer.changeSize(Properties.getScaleFactor(newValue))
      layer.children.foreach { view =>
        view.translateX = view.translateX() * scaleFactor
        view.translateY = view.translateY() * scaleFactor
      }
    }

    creatorPane.minWidth = Properties.getResTuple(newValue)._1
    creatorPane.maxWidth = Properties.getResTuple(newValue)._1
    creatorPane.minHeight = Properties.getResTuple(newValue)._2
    creatorPane.maxHeight = Properties.getResTuple(newValue)._2
  }

  selectedLayerProp.onChange { (_, oldValue, newValue) =>
    if (oldValue != null) {
      val layer = oldValue.asInstanceOf[ImageLayer]
      //layer.style = "-fx-border-width: 0;"
      layer.color.unbind()
    }
    if (newValue != null) {
      val layer = newValue.asInstanceOf[ImageLayer]
      //layer.style = "-fx-border-color: #7a7a7a; -fx-border-width: 1;"
      colorChooser.value = layer.color()
      layer.color <== colorChooser.value
    }
  }

  madeChangesProp.onChange { (_, _, newValue) =>
    if (newValue) updateStatus(true)
  }

  //====================================================================================================================
  // Stage
  //====================================================================================================================

  stage = new JFXApp.PrimaryStage() {

    title = Properties.Title
    resizable = true
    maximized = true
    minWidth = Properties.Resolution._1
    minHeight = Properties.Resolution._2

    scene = new Scene(Properties.Resolution._1, Properties.Resolution._2) {
      stylesheets.addAll("css/app.css", "css/colorchooser.css")
      root = new BorderPane() {
        styleClass.add("background-style")
        top = new Pane() {
          margin = Insets(Properties.Padding)
          children = new HBox(Properties.Padding) {
            children = Seq(
              createStatusPanel(),
              createFilePanel(),
              createToolPanel()
            )
          }
        }
        center = new Pane() {
          alignmentInParent = Pos.TopLeft
          children = Seq(
            makeCreatorPaneDraggable(creatorPane),
            makeDraggable(colorChooserPane)
          )
        }
      }
    }
  }

  //====================================================================================================================
  // File Panel
  //====================================================================================================================

  private def createFilePanel(): HBox = {
    new HBox(Properties.Padding) {
      styleClass.add("panel-style")
      children = Seq(
        new Button(Properties.New) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            loadRawrFile(true)
          }
        },
        new Button(Properties.Open) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            loadRawrFile(false)
          }
        },
        new Button(Properties.Save) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            saveRawrFile(false)
          }
        },
        new Button(Properties.SaveAs) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            saveRawrFile(true)
          }
        },
        new Button(Properties.SaveImage) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            saveImage()
          }
        },
        new Button(Properties.Quit) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            quit()
          }
        }
      )
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
        title = Properties.FileChooserTitle
        extensionFilters.addAll(new FileChooser.ExtensionFilter("RAWR", "*.rawr"))
      }

      // Prompt user to choose file
      saveFile.value = chooser.showOpenDialog(stage)

      // Check if file was chosen
      if (saveFile() != null) {

        // Ensure file extension
        if (FilenameUtils.getExtension(saveFile().getAbsolutePath) != "rawr") {
          createBadFormatDialog(saveFile().getName).showAndWait()
          return
        }

        var in: ObjectInputStream = null
        val attempt = Try {
          in = new ObjectInputStream(new FileInputStream(saveFile()))
          in.readObject.asInstanceOf[Array[ImageLayer.Serialization]]
        }
        in.close()

        attempt match {
          case Success(seq) =>
            creatorPane.children.clear()
            seq.foreach(layer =>
              creatorPane.children.add(
                makeTransformable(
                  {
                    val imgLayer = new ImageLayer(layer.resource)
                    imgLayer.color = layer.color
                    imgLayer.changeSize(Properties.getScaleFactor(resolutionProp()))
                    imgLayer.color.onChange { (_, _, _) => madeChangesProp.value = true }
                    imgLayer.children.forEach { view =>
                      view.translateX = layer.xPos
                      view.translateY = layer.yPos
                    }
                    imgLayer
                  }
                )
              )
            )
          case Failure(e) =>
            createExceptionDialog(e, "Could not load file.", e.getMessage)
        }
      }
    }

    /**
      * Returns either reset or load.
      * @param init True for reinitialization.
      */
    def resetElseLoad(init: Boolean): Unit = {
      if (init) {
        creatorPane.children.clear()
        saveFile.value = null
      } else {
        load()
      }
      updateStatus(false)
    }

    // Check if changes were made to the current file
    if (madeChangesProp()) {
      createSaveChangesDialog().showAndWait() match {
        case Some(ButtonType.Yes) => if (saveRawrFile(false)) resetElseLoad(init)
        case Some(ButtonType.No) => resetElseLoad(init)
        case _ =>
      }
    } else resetElseLoad(init)
  }

  /**
    * Saves the current rawr file to disk.
    * @param saveAs True if doing Save As operation.
    */
  private def saveRawrFile(saveAs: Boolean): Boolean = {

    // Open file chooser if necessary
    if (saveFile() == null || saveAs) {
      val chooser = new FileChooser() {
        title = Properties.FileChooserTitle
        extensionFilters.addAll(new FileChooser.ExtensionFilter("RAWR", "*.rawr"))
      }

      saveFile.value = chooser.showSaveDialog(stage)
    }

    // Check if file is chosen
    if (saveFile() != null) {

      // Ensure file extension
      if (FilenameUtils.getExtension(saveFile().getAbsolutePath) == "") {
        saveFile.value = new File(saveFile().getAbsolutePath + ".rawr")
      }

      val seq = new Array[ImageLayer.Serialization](creatorPane.children.size)
      val layers = getLayers
      for ((layer, i) <- layers.view.zipWithIndex) {
        seq(i) = new ImageLayer.Serialization(layer)
      }

      var out: ObjectOutputStream = null
      val attempt = Try {
        out = new ObjectOutputStream(new FileOutputStream(saveFile()))
        out.writeObject(seq)
      }
      out.close()

      attempt match {
        case Success(_) =>
          new Alert(AlertType.Information) {
            initOwner(stage)
            title = Properties.AlertSuccess
            headerText = "Successfully saved file."
            contentText = "The file was saved successfully to " + saveFile().getAbsolutePath
          }.showAndWait()
          updateStatus(false)
          true
        case Failure(e) =>
          createExceptionDialog(e, "Could not save file.", e.getMessage)
          false
      }
    } else {
      false
    }
  }

  /**
    * Saves what is currently being displayed in the GUI to disk as a
    * PNG image.
    */
  private def saveImage(): Unit = {

    // Create resolution choice dialog
    val resChoiceDialog = new ChoiceDialog(defaultChoice = Properties.ResPropInit, choices = Properties.ResolutionOptions) {
      initOwner(stage)
      title = Properties.DialogResChoice
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
      title = Properties.FileChooserTitle
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

      // Create snapshot of group
      val temp = resolutionProp()
      resolutionProp.value = resChoice
      val wr = new WritableImage(Properties.getResTuple(resChoice)._1.toInt, Properties.getResTuple(resChoice)._2.toInt)
      val out = creatorPane.snapshot(new SnapshotParameters(), wr)
      resolutionProp.value = temp

      // Attempt to save image to disk
      Try {
        ImageIO.write(
          SwingFXUtils.fromFXImage(out, null),
          FilenameUtils.getExtension(file.getAbsolutePath),
          file)
      } match {
        case Success(_) =>
          new Alert(AlertType.Information) {
            initOwner(stage)
            title = Properties.AlertSuccess
            headerText = "Successfully saved image."
            contentText = "The image was saved successfully to " + file.getAbsolutePath
          }.showAndWait()
        case Failure(e) =>
          createExceptionDialog(e, "Error saving image.", e.getMessage).showAndWait()
      }
    }
  }

  private def quit(): Unit = {
    if (madeChangesProp.value) {
      createSaveChangesDialog().showAndWait() match {
        case Some(ButtonType.Yes) => saveRawrFile(false);  System.exit(0)
        case Some(ButtonType.No) =>  System.exit(0)
        case _ =>
      }
    } else System.exit(0)
  }

  //====================================================================================================================
  // Tool Panel
  //====================================================================================================================

  private def createToolPanel(): HBox = {
    new HBox(Properties.Padding) {
      alignment = Pos.CenterLeft
      styleClass.add("panel-style")
      children = Seq(
        new Button("Add") {
          prefWidth = Properties.ButtonWidth
          onAction = { _: ActionEvent =>
            creatorPane.children.add({
              val layer = makeTransformable(
                new ImageLayer(Res.horn1) {
                  changeSize(Properties.getScaleFactor(resolutionProp()))
                  color.onChange { (_, _, _) => madeChangesProp.value = true }
                }
              )
              selectedLayerProp.value = layer
              layer
            })
          }
        },
        new Button(Properties.Delete) {
          prefWidth = Properties.ButtonWidth
          onAction = { _: ActionEvent =>
            if (selectedLayerProp() != null) {
              creatorPane.children.remove(selectedLayerProp())
              selectedLayerProp.value = null
            }
          }
        },
        new SplitMenuButton() {
          text = Properties.BringForward
          prefWidth = Properties.PickerWidth
          def bringForward(): Unit = {
            if (selectedLayerProp() != null) {
              val index = creatorPane.children.indexOf(selectedLayerProp())
              if (index < creatorPane.children.size - 1) {
                creatorPane.children.remove(selectedLayerProp())
                creatorPane.children.add(index + 1, selectedLayerProp())
              }
            }
          }
          onAction = { _: ActionEvent => bringForward() }
          items = Seq(
            new MenuItem(Properties.BringForward) {
              onAction = { _: ActionEvent => bringForward() }
            },
            new MenuItem(Properties.BringToFront) {
              onAction = { _: ActionEvent =>
                if (selectedLayerProp() != null) {
                  val index = creatorPane.children.indexOf(selectedLayerProp())
                  creatorPane.children.get(index).toFront()
                }
              }
            }
          )
        },
        new SplitMenuButton() {
          text = Properties.SendBackward
          prefWidth = Properties.PickerWidth
          def sendBackward(): Unit = {
            if (selectedLayerProp() != null) {
              val index = creatorPane.children.indexOf(selectedLayerProp())
              if (index > 0) {
                creatorPane.children.remove(selectedLayerProp())
                creatorPane.children.add(index - 1, selectedLayerProp())
              }
            }
          }
          onAction = { _: ActionEvent => sendBackward() }
          items = Seq(
            new MenuItem(Properties.SendBackward) {
              onAction = { _: ActionEvent => sendBackward() }
            },
            new MenuItem(Properties.SendToBack) {
              onAction = { _: ActionEvent =>
                if (selectedLayerProp() != null) {
                  val index = creatorPane.children.indexOf(selectedLayerProp())
                  creatorPane.children.get(index).toBack()
                }
              }
            }
          )
        },
        new ChoiceBox[String]() {
          items.value.addAll(Properties.DragTool, Properties.MoveTool)
          selectionModel().select(1)
          selectionModel().selectedItemProperty().onChange { (_, oldValue, newValue) =>
            oldValue match {
              case a if a == Properties.DragTool => dragToolProp.value = false
              case b if b == Properties.MoveTool => moveToolProp.value = false
            }
            newValue match {
              case a if a == Properties.DragTool => dragToolProp.value = true
              case b if b == Properties.MoveTool => moveToolProp.value = true
            }
          }
        },
        new Button(Properties.Reset) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            creatorPane.translateX = 0
            creatorPane.translateY = 0
            colorChooserPane.translateX = 0
            colorChooserPane.translateY = 0
            resolutionProp.value = Properties.ResPropInit
          }
        },
        new ComboBox(Properties.ResolutionOptions) {
          prefWidth = 105
          value <==> resolutionProp
        }
      )
    }
  }

  //====================================================================================================================
  // Status Panel
  //====================================================================================================================

  private def createStatusPanel(): StackPane = {
    new StackPane() {
      styleClass.add("panel-style")
      children = Seq(
        new Label(Properties.StatusLabel) {
          prefWidth = Properties.PickerWidth
          alignmentInParent = Pos.Center
          alignment = Pos.Center
          text <== statusProp
        }
      )
    }
  }

  //====================================================================================================================
  // Dialogs
  //====================================================================================================================

  /**
    * Creates a new save changes dialog.
    * @return Alert.
    */
  private def createSaveChangesDialog(): Alert = {
    new Alert(AlertType.Confirmation) {
      initOwner(stage)
      title = Properties.alertConfirm
      headerText = "Would you like to save changes to the current file?"
      contentText = "You have made changes to " +
        { if (saveFile() == null) "an unsaved file" else FilenameUtils.getBaseName(saveFile().getName) } + "."
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
  private def createExceptionDialog(e: Throwable, header: String, content: String): Alert = {
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
      title = Properties.alertError
      headerText = header
      contentText = content
      dialogPane().expandableContent = expContent
    }
  }

  //====================================================================================================================
  // Mouse Events
  //====================================================================================================================

  private def makeTransformable(layer: ImageLayer): Group = {

    val dragContext = for {
      _ <- 0 until layer.children.size
      ctx = new DragContext()
    } yield ctx

    val x: Group = layer
    x.filterEvent(MouseEvent.Any) { me: MouseEvent =>
      me.eventType match {
        case MouseEvent.MousePressed =>
          if (!dragToolProp()) selectedLayerProp.value = layer
          if (moveToolProp()) {
            for ((view, i) <- layer.children.view.zipWithIndex) {
              dragContext(i).mouseAnchorX = me.x
              dragContext(i).mouseAnchorY = me.y
              dragContext(i).initialTranslateX = view.translateX()
              dragContext(i).initialTranslateY = view.translateY()
            }
          }
        case MouseEvent.MouseDragged =>
          if (moveToolProp()) {
            for ((view, i) <- layer.children.view.zipWithIndex) {
              view.translateX = dragContext(i).initialTranslateX + me.x - dragContext(i).mouseAnchorX
              view.translateY = dragContext(i).initialTranslateY + me.y - dragContext(i).mouseAnchorY

              val bounds = (0, 0, creatorPane.maxWidth(), creatorPane.maxHeight())
              val nodeBounds = view.localToScene(view.getBoundsInLocal)

              if (view.translateX() < bounds._1)
                view.translateX = bounds._1 + 2
              if (view.translateY() < bounds._2)
                view.translateY = bounds._2 + 2
              if (view.translateX() + nodeBounds.getWidth > bounds._3)
                view.translateX = bounds._3 - nodeBounds.getWidth - 2
              if (view.translateY() + nodeBounds.getHeight > bounds._4)
                view.translateY = bounds._4 - nodeBounds.getHeight - 2
            }
            madeChangesProp.value = true
          }
        case _ =>
      }
      me.consume()
    }

    layer
  }

  private def makeCreatorPaneDraggable(pane: Pane): Group = {
    new Group(pane) {
      val dragContext = new DragContext()
      filterEvent(MouseEvent.Any) { me: MouseEvent =>
        if (me.eventType == MouseEvent.MousePressed) selectedLayerProp.value = null
        if (dragToolProp()) {
          me.eventType match {
            case MouseEvent.MousePressed =>
              dragContext.mouseAnchorX = me.x
              dragContext.mouseAnchorY = me.y
              dragContext.initialTranslateX = pane.translateX()
              dragContext.initialTranslateY = pane.translateY()
            case MouseEvent.MouseDragged =>
              pane.translateX = dragContext.initialTranslateX + me.x - dragContext.mouseAnchorX
              pane.translateY = dragContext.initialTranslateY + me.y - dragContext.mouseAnchorY
            case _ =>
          }
        }
      }
    }
  }

  private def makeDraggable(node: Node): Group = {
    new Group(node) {
      val dragContext = new DragContext()
      filterEvent(MouseEvent.Any) { me: MouseEvent =>
        if (dragToolProp()) {
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

  private final class DragContext {
    var mouseAnchorX: Double = 0d
    var mouseAnchorY: Double = 0d
    var initialTranslateX: Double = 0d
    var initialTranslateY: Double = 0d
  }

  //====================================================================================================================
  // Helper Functions
  //====================================================================================================================

  private def getLayers: IndexedSeq[ImageLayer] = {
    for {
      i <- 0 until creatorPane.children.size
      layer = creatorPane.children.get(i).asInstanceOf[jfxs.Group].asInstanceOf[ImageLayer]
    } yield layer
  }

  private def updateStatus(changeMade: Boolean): Unit = {
    if (changeMade) {
      if (saveFile() != null)
        statusProp.value = FilenameUtils.getBaseName(saveFile().getName) + "*"
      else
        statusProp.value = Properties.StatusLabel + "*"
    } else {
      madeChangesProp.value = false
      if (saveFile() != null)
        statusProp.value = FilenameUtils.getBaseName(saveFile().getName)
      else
        statusProp.value = Properties.StatusLabel
    }
  }

}