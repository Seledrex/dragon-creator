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
import scalafx.scene.control._
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

  private val dragToolProp = new BooleanProperty(bean, propName, false)
  private val moveToolProp = new BooleanProperty(bean, propName, true)
  private val resolutionProp = new StringProperty(bean, propName, Properties.ResPropInit)
  private val selectedLayerProp = new ObjectProperty[jfxs.Group](bean, propName, null)
  private val statusProp = new StringProperty(bean, propName, Properties.StatusLabel)

  //====================================================================================================================
  // Application Variables
  //====================================================================================================================

  private val creatorPane = new Pane() {
    styleClass.add("panel-style")
    minWidth = Properties.ResTuple(resolutionProp())._1
    maxWidth = Properties.ResTuple(resolutionProp())._1
    minHeight = Properties.ResTuple(resolutionProp())._2
    maxHeight = Properties.ResTuple(resolutionProp())._2
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
    val oldRes = Properties.ResTuple(oldValue)
    val newRes = Properties.ResTuple(newValue)
    val scaleFactor = newRes._1 / oldRes._1

    creatorPane.children.foreach { node =>
      val elem = node.asInstanceOf[jfxs.Group].children.head.asInstanceOf[ImageLayer]
      elem.changeSize(Properties.ScaleFactor(newValue))
      elem.translateX = elem.translateX() * scaleFactor
      elem.translateY = elem.translateY() * scaleFactor
    }

    creatorPane.minWidth = Properties.ResTuple(newValue)._1
    creatorPane.maxWidth = Properties.ResTuple(newValue)._1
    creatorPane.minHeight = Properties.ResTuple(newValue)._2
    creatorPane.maxHeight = Properties.ResTuple(newValue)._2
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
            //loadRawrFile(true)
          }
        },
        new Button(Properties.Open) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            //loadRawrFile(false)
          }
        },
        new Button(Properties.Save) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            //saveRawrFile(false)
          }
        },
        new Button(Properties.SaveAs) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            //saveRawrFile(true)
          }
        },
        new Button(Properties.SaveImage) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            //saveImage()
          }
        },
        new Button(Properties.Quit) {
          prefWidth = Properties.ButtonWidth
          onAction = (_: ActionEvent) => {
            //quit()
          }
        }
      )
    }
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
                  changeSize(Properties.ScaleFactor(resolutionProp()))
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
  // Mouse Events
  //====================================================================================================================

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