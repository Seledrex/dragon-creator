package test

//======================================================================================================================
// Imports
//======================================================================================================================

import res.Prop
import scalafx.application.JFXApp
import scalafx.beans.property.BooleanProperty
import scalafx.scene.input.MouseEvent
import scalafx.scene.{Group, Node, Scene}
import scalafx.scene.layout._
import scalafx.Includes._

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
        }
        top = new HBox() {
        }
        styleClass.add("background-style")
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