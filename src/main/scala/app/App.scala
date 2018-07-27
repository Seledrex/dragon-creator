package app

//======================================================================================================================
// Imports
//======================================================================================================================

import javafx.{scene => jfxs}
import res.{Properties, Res}
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, ChoiceBox, ComboBox}
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
  private val resolutionProp = new StringProperty(bean, propName, Properties.imgResStr)
  private val layerProp = new ObjectProperty[jfxs.Group](bean, propName, null)

  //====================================================================================================================
  // Application Variables
  //====================================================================================================================

  private val creatorPane = new Pane() {
    styleClass.add("panel-style")
    minWidth = Properties.getResTup(Properties.imgResStr)._1
    maxWidth = Properties.getResTup(Properties.imgResStr)._1
    minHeight = Properties.getResTup(Properties.imgResStr)._2
    maxHeight = Properties.getResTup(Properties.imgResStr)._2
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

  layerProp.onChange { (_, oldValue, newValue) =>
    if (oldValue != null) oldValue.children.head.style = "-fx-border-width: 0;"
    if (newValue != null) newValue.children.head.style = "-fx-border-color: #7a7a7a; -fx-border-width: 1;"
  }

  //====================================================================================================================
  // Stage
  //====================================================================================================================

  stage = new JFXApp.PrimaryStage() {

    title = Properties.Title
    resizable = true
    maximized = true
    minWidth = 1280
    minHeight = 720

    scene = new Scene(Properties.resolution._1, Properties.resolution._2) {
      stylesheets.add("styles.css")
      root = new BorderPane() {
        center = new Pane() {
          alignmentInParent = Pos.TopLeft
          children = Seq(
            makeDraggable(
              creatorPane
            )
          )
        }
        top = new Pane() {
          alignmentInParent = Pos.TopLeft
          children = Seq(
            new HBox(Properties.padding) {
              styleClass.add("panel-style")
              children = Seq(
                new Button("Add") {
                  onAction = { _: ActionEvent =>
                    creatorPane.children.add(
                      makeTransformable(
                        new ImageLayer(Res.horn1) {
                          changeSize(Properties.getScaleFactor(resolutionProp()))
                        }
                      )
                    )
                  }
                },
                new Button("Move up") {
                  onAction = { _: ActionEvent =>
                    if (layerProp.value != null) {
                      val index = creatorPane.children.indexOf(layerProp.value)
                      if (index < creatorPane.children.size - 1) {
                        creatorPane.children.remove(layerProp.value)
                        creatorPane.children.add(index + 1, layerProp.value)
                      }
                    }
                  }
                },
                new Button("Move down") {
                  onAction = { _: ActionEvent =>
                    if (layerProp.value != null) {
                      val index = creatorPane.children.indexOf(layerProp.value)
                      if (index > 0) {
                        val temp = creatorPane.children.get(index - 1)
                        creatorPane.children.remove(layerProp.value)
                        creatorPane.children.remove(temp)
                        creatorPane.children.add(index - 1, layerProp.value)
                        creatorPane.children.add(index, temp)
                      }
                    }
                  }
                },
                new ComboBox(Properties.resOptions) {
                  value <==> resolutionProp
                  prefWidth = 105
                },
                new ChoiceBox[String]() {
                  items.value.addAll("Drag tool", "Move tool")
                  selectionModel().select(0)
                  selectionModel().selectedItemProperty().onChange { (_, oldValue, newValue) =>
                    oldValue match {
                      case a if a == "Drag tool" => dragToolProp.value = false
                      case b if b == "Move tool" => moveToolProp.value = false
                    }
                    newValue match {
                      case a if a == "Drag tool" => dragToolProp.value = true
                      case b if b == "Move tool" => moveToolProp.value = true
                    }
                  }
                }
              )
            }
          )
        }
        styleClass.add("background-style")
      }
    }
  }

  private def makeTransformable(node: ImageLayer): Group = {

    val dragContext = new DragContext()

    new Group(node) {
      val self: Group = this
      filterEvent(MouseEvent.Any) { me: MouseEvent =>
        me.eventType match {
          case MouseEvent.MousePressed =>
            if (!dragToolProp()) layerProp.value = self
            if (moveToolProp()) {
              dragContext.mouseAnchorX = me.x
              dragContext.mouseAnchorY = me.y
              dragContext.initialTranslateX = node.translateX()
              dragContext.initialTranslateY = node.translateY()
            }
          case MouseEvent.MouseDragged =>
            if (moveToolProp()) {
              node.translateX = dragContext.initialTranslateX + me.x - dragContext.mouseAnchorX
              node.translateY = dragContext.initialTranslateY + me.y - dragContext.mouseAnchorY

              val bounds = (0, 0, creatorPane.maxWidth(), creatorPane.maxHeight())
              val nodeBounds = node.localToScene(node.getBoundsInLocal)

              if (node.translateX() < bounds._1)
                node.translateX = bounds._1 + 2
              if (node.translateY() < bounds._2)
                node.translateY = bounds._2 + 2
              if (node.translateX() + nodeBounds.getWidth > bounds._3)
                node.translateX = bounds._3 - nodeBounds.getWidth - 2
              if (node.translateY() + nodeBounds.getHeight > bounds._4)
                node.translateY = bounds._4 - nodeBounds.getHeight - 2
            }
          case _ =>
        }
        me.consume()
      }
    }
  }

  private def makeDraggable(node: Node): Node = {

    val dragContext = new DragContext()

    new Group(node) {
      filterEvent(MouseEvent.Any) { me: MouseEvent =>
        me.eventType match {
          case MouseEvent.MousePressed =>
            layerProp.value = null
            if (dragToolProp()) {
              dragContext.mouseAnchorX = me.x
              dragContext.mouseAnchorY = me.y
              dragContext.initialTranslateX = node.translateX()
              dragContext.initialTranslateY = node.translateY()
            }
          case MouseEvent.MouseDragged =>
            if (dragToolProp()) {
              node.translateX = dragContext.initialTranslateX + me.x - dragContext.mouseAnchorX
              node.translateY = dragContext.initialTranslateY + me.y - dragContext.mouseAnchorY
            }
          case _ =>
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