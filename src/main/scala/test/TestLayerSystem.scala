package test

//======================================================================================================================
// Imports
//======================================================================================================================

import res.{Prop, Res}
import javafx.{scene => jfxs}
import scalafx.application.JFXApp
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.scene.input.MouseEvent
import scalafx.scene.{Group, Node, Scene}
import scalafx.scene.layout._
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, CheckBox}

//======================================================================================================================
// Test
//======================================================================================================================

/**
  * Dragon Creator ScalaFX application.
  * @author Seledrex, Sanuthem
  */
object TestLayerSystem extends JFXApp {

  //==================================================================================================================
  // Application Variables
  //==================================================================================================================

  private val bean = this
  private val propName = null

  private val dragModeProp = new BooleanProperty(bean, propName, false)
  private val moveToolProp = new BooleanProperty(bean, propName, true)
  private val selectionProp = new ObjectProperty[jfxs.Group](bean, propName, null)

  private val creatorPane = new Pane() {
    styleClass.add("panel-style")
    minWidth = 1280
    maxWidth = 1280
    minHeight = 720
    maxHeight = 720
  }

  //==================================================================================================================
  // Stage
  //==================================================================================================================

  /**
    * Application stage. All user interface elements are contained
    * within this object.
    */
  stage = new JFXApp.PrimaryStage() {

    title = Prop.title
    resizable = true
    maximized = true


    scene = new Scene(Prop.resolution._1, Prop.resolution._2) {
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
            new HBox(Prop.padding) {
              styleClass.add("panel-style")
              children = Seq(
                new Button("Add square") {
                  onAction = { _: ActionEvent =>
                    creatorPane.children.add(
                      makeTransformable(
                        new TestImgElem(Res.horn1)
                      )
                    )
                  }
                },
                new Button("Move up") {
                  onAction = { _: ActionEvent =>
                    if (selectionProp.value != null) {
                      println("Move up")
                      val index = creatorPane.children.indexOf(selectionProp.value)
                      if (index < creatorPane.children.size - 1) {
                        creatorPane.children.remove(selectionProp.value)
                        creatorPane.children.add(index + 1, selectionProp.value)
                      }
                    }
                  }
                },
                new Button("Move down") {
                  onAction = { _: ActionEvent =>
                    if (selectionProp.value != null) {
                      println("Move down")
                      val index = creatorPane.children.indexOf(selectionProp.value)
                      if (index > 0) {
                        val temp = creatorPane.children.get(index - 1)
                        creatorPane.children.remove(selectionProp.value)
                        creatorPane.children.remove(temp)
                        creatorPane.children.add(index - 1, selectionProp.value)
                        creatorPane.children.add(index, temp)
                      }
                    }
                  }
                },
                new CheckBox(Prop.dragModeCheckBoxName) {
                  dragModeProp <== selected
                  prefWidth = Prop.buttonWidth
                }
              )
            }
          )
        }
        styleClass.add("background-style")
      }
    }
  }

  private def makeTransformable(node: TestImgElem): Group = {

    // Create context
    val dragContext = new DragContext()

    // Put node in group the filter mouse events
    new Group(node) {
      val self: Group = this
      filterEvent(MouseEvent.Any) { me: MouseEvent =>
        me.eventType match {
          case MouseEvent.MousePressed =>
            selectionProp.value = self
            if (moveToolProp()) {
              dragContext.mouseAnchorX = me.x
              dragContext.mouseAnchorY = me.y
              dragContext.initialTranslateX = node.translateX()
              dragContext.initialTranslateY = node.translateY()
            }
          case MouseEvent.MouseDragged =>
            if (moveToolProp()) {
              val tempX = node.translateX.value
              val tempY = node.translateY.value
              node.translateX = dragContext.initialTranslateX + me.x - dragContext.mouseAnchorX
              node.translateY = dragContext.initialTranslateY + me.y - dragContext.mouseAnchorY
              val bounds = creatorPane.localToScene(creatorPane.getBoundsInLocal)
              val nodeBounds = node.localToScene(node.getBoundsInLocal)
              if (nodeBounds.getMinX <= bounds.getMinX || nodeBounds.getMinY <= bounds.getMinY ||
                nodeBounds.getMaxX >= bounds.getMaxX || nodeBounds.getMaxY >= bounds.getMaxY) {
                node.translateX = tempX
                node.translateY = tempY
              }
            }
          case _ =>
        }
        me.consume()
      }
    }
  }

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

}