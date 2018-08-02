package app

import scalafx.beans.property.IntegerProperty
import scalafx.scene.control.TextFormatter.Change
import scalafx.scene.control.{TextField, TextFormatter}

class IntegerField(minValue: Int, maxValue: Int, initValue: Int) extends TextField {

  def this() {
    this(0, 100, 0)
  }

  private var min: Int = {
    if (minValue >= maxValue) throw new IllegalArgumentException
    minValue
  }

  private var max: Int = {
    if (maxValue <= minValue) throw new IllegalArgumentException
    maxValue
  }

  private val valueProperty = IntegerProperty({
    if (initValue < min || initValue > max) throw new IllegalArgumentException
    initValue
  })

  text = s"$initValue"

  value.onChange { (_, _, newValue) =>
    if (newValue == null) {
      text = ""
    } else {
      if (newValue.intValue() < min) {
        value() = min
      } else if (newValue.intValue() > max) {
        value() = max
      } else {
        text = newValue.toString
      }
    }
  }

  textFormatter = new TextFormatter[String]( { change: Change =>
    if (!change.controlNewText.matches("[0-9]+") && change.controlNewText != "") {
      change.text = ""
    } else if (change.controlNewText == "") {
      change.text = "0"
      change.anchor += 1
      change.caretPosition += 1
    }
    change
  })

  text.onChange { (_, oldValue, newValue) =>
    if (newValue == null || newValue == "") {
      value() = 0
    } else {
      val intValue = newValue.toInt
      if (intValue < min || intValue > max) text = oldValue
      value() = text().toInt
    }
  }

  def value: IntegerProperty = valueProperty

  def value_=(newValue: Int): Unit = {
    if (newValue > max) value() = max
    else if (newValue < min) value() = min
    else value() = newValue
  }

  def getMax: Int = max
  def getMin: Int = min

  def setMax(newMax: Int): Unit = {
    if (newMax > min) {
      max = newMax
      if (value() > max) value() = max
    } else throw new IllegalArgumentException
  }

  def setMin(newMin: Int): Unit = {
    if (newMin < max) {
      min = newMin
      if (value() < min) value() = min
    } else throw new IllegalArgumentException
  }

}
