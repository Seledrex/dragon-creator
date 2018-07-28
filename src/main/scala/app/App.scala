package app

//======================================================================================================================
// Imports
//======================================================================================================================

import javafx.{scene => jfxs}
import res.Res
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, ChoiceBox, ComboBox, Label}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.{Group, Node, Scene}

//======================================================================================================================
// App
//======================================================================================================================

object App extends JFXApp {

  private final val bean = this
  private final val propName = null

  //====================================================================================================================
  // Properties
  //====================================================================================================================

  private val dragToolProp = new BooleanProperty(bean, propName, true)
  private val moveToolProp = new BooleanProperty(bean, propName, false)
  private val resolutionProp = new StringProperty(bean, propName, Properties.ResPropInit)
  private val selectedLayerProp = new ObjectProperty[jfxs.Group](bean, propName, null)
  private val statusProp = new StringProperty(bean, propName, Properties.StatusLabel)

  //====================================================================================================================
  // Application Variables
  //====================================================================================================================

  private val creatorPane = new Pane() {
    styleClass.add("panel-style")
    minWidth = Properties.getResTup(resolutionProp())._1
    maxWidth = Properties.getResTup(resolutionProp())._1
    minHeight = Properties.getResTup(resolutionProp())._2
    maxHeight = Properties.getResTup(resolutionProp())._2
    relocate(Properties.CreatorPaneX, Properties.CreatorPaneY)
  }

  private val colorChooser = new ColorChooser() {
    styleClass.add("panel-style")
  }

  private val colorChooserPane = new Pane() {
    children = Seq(colorChooser)
    relocate(Properties.ColorChooserPaneX, Properties.ColorChooserPaneY)
  }

  private val colorPalette = new ColorPalette() {
    styleClass.add("panel-style")
  }

  private val colorPalettePane = new Pane() {
    children = Seq(colorPalette)
    relocate(Properties.ColorPalettePaneX, Properties.ColorPalettePaneY)
  }

  //====================================================================================================================
  // Change Listeners
  //====================================================================================================================

  resolutionProp.onChange { (_, oldValue, newValue) =>
    val oldRes = Properties.getResTup(oldValue)
    val newRes = Properties.getResTup(newValue)
    val scaleFactor = newRes._1 / oldRes._1

    creatorPane.children.foreach { node =>
      val elem = node.asInstanceOf[jfxs.Group].children.head.asInstanceOf[ImageLayer]
      elem.changeSize(Properties.getScaleFactor(newValue))
      elem.translateX = elem.translateX() * scaleFactor
      elem.translateY = elem.translateY() * scaleFactor
    }

    creatorPane.minWidth = Properties.getResTup(newValue)._1
    creatorPane.maxWidth = Properties.getResTup(newValue)._1
    creatorPane.minHeight = Properties.getResTup(newValue)._2
    creatorPane.maxHeight = Properties.getResTup(newValue)._2
  }

  selectedLayerProp.onChange { (_, oldValue, newValue) =>
    if (oldValue != null) {
      val layer = oldValue.children.head.asInstanceOf[ImageLayer]
      layer.style = "-fx-border-width: 0;"
      layer.color.unbind()
    }
    if (newValue != null) {
      val layer = newValue.children.head.asInstanceOf[ImageLayer]
      layer.style = "-fx-border-color: #7a7a7a; -fx-border-width: 1;"
      colorChooser.value = layer.color()
      layer.color <== colorChooser.value
    }
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

    colorChooser.value <==> colorPalette.value

    scene = new Scene(Properties.Resolution._1, Properties.Resolution._2) {
      stylesheets.addAll("css/app.css", "css/colorchooser.css", "css/colorpalette.css")
      root = new BorderPane() {
        styleClass.add("background-style")
        top = new Pane() {
          margin = Insets(Properties.Padding)
          children = new HBox(Properties.Padding) {
            children = Seq(
              createFilePanel(),
              createToolPanel(),
              createStatusPanel()
            )
          }
        }
        center = new Pane() {
          alignmentInParent = Pos.TopLeft
          children = Seq(
            makeCreatorPaneDraggable(creatorPane),
            makeDraggable(colorChooserPane),
            makeDraggable(colorPalettePane)
          )
        }
      }
    }
  }

  private def createFilePanel(): HBox = {
    new HBox(Properties.Padding) {
      styleClass.add("panel-style")
      children = Seq(
        new Button(Properties.NewButton) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            //loadRawrFile(true)
          }
        },
        new Button(Properties.OpenButton) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            //loadRawrFile(false)
          }
        },
        new Button(Properties.SaveButton) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            //saveRawrFile(false)
          }
        },
        new Button(Properties.SaveAsButton) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            //saveRawrFile(true)
          }
        },
        new Button(Properties.SaveImageButton) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            //saveImage()
          }
        },
        new Button(Properties.QuitButton) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            //quit()
          }
        }
      )
    }
  }

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
                }
              )
              selectedLayerProp.value = layer
              layer
            })
          }
        },
        new Button("Move up") {
          prefWidth = Properties.ButtonWidth
          onAction = { _: ActionEvent =>
            if (selectedLayerProp.value != null) {
              val index = creatorPane.children.indexOf(selectedLayerProp.value)
              if (index < creatorPane.children.size - 1) {
                creatorPane.children.remove(selectedLayerProp.value)
                creatorPane.children.add(index + 1, selectedLayerProp.value)
              }
            }
          }
        },
        new Button("Move down") {
          prefWidth = Properties.ButtonWidth
          onAction = { _: ActionEvent =>
            if (selectedLayerProp.value != null) {
              val index = creatorPane.children.indexOf(selectedLayerProp.value)
              if (index > 0) {
                creatorPane.children.remove(selectedLayerProp.value)
                creatorPane.children.add(index - 1, selectedLayerProp.value)
              }
            }
          }
        },
        new Button(Properties.ResetButton) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            creatorPane.relocate(Properties.CreatorPaneX, Properties.CreatorPaneY)
            resolutionProp.value = Properties.ResPropInit
          }
        },
        new ComboBox(Properties.ResolutionOptions) {
          value <==> resolutionProp
          prefWidth = 105
        },
        new ChoiceBox[String]() {
          items.value.addAll(Properties.DragTool, Properties.MoveTool)
          selectionModel().select(0)
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
        }
      )
    }
  }

  private def createStatusPanel(): StackPane = {
    new StackPane() {
      styleClass.add("panel-style")
      children = Seq(
        new Label(Properties.StatusLabel) {
          prefWidth = Properties.pickerWidth
          alignmentInParent = Pos.Center
          alignment = Pos.Center
          text <== statusProp
        }
      )
    }
  }

  private def makeTransformable(layer: ImageLayer): Group = {

    val dragContext = new DragContext()

    new Group(layer) {
      val self: Group = this
      filterEvent(MouseEvent.Any) { me: MouseEvent =>
        me.eventType match {
          case MouseEvent.MousePressed =>
            if (!dragToolProp()) selectedLayerProp.value = self
            if (moveToolProp()) {
              dragContext.mouseAnchorX = me.x
              dragContext.mouseAnchorY = me.y
              dragContext.initialTranslateX = layer.translateX()
              dragContext.initialTranslateY = layer.translateY()
            }
          case MouseEvent.MouseDragged =>
            if (moveToolProp()) {
              layer.translateX = dragContext.initialTranslateX + me.x - dragContext.mouseAnchorX
              layer.translateY = dragContext.initialTranslateY + me.y - dragContext.mouseAnchorY

              val bounds = (0, 0, creatorPane.maxWidth(), creatorPane.maxHeight())
              val nodeBounds = layer.localToScene(layer.getBoundsInLocal)

              if (layer.translateX() < bounds._1)
                layer.translateX = bounds._1 + 2
              if (layer.translateY() < bounds._2)
                layer.translateY = bounds._2 + 2
              if (layer.translateX() + nodeBounds.getWidth > bounds._3)
                layer.translateX = bounds._3 - nodeBounds.getWidth - 2
              if (layer.translateY() + nodeBounds.getHeight > bounds._4)
                layer.translateY = bounds._4 - nodeBounds.getHeight - 2
            }
          case _ =>
        }
        me.consume()
      }
    }
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

}