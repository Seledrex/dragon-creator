package app

import scalafx.beans.property.StringProperty
import scalafx.scene.control.TextFormatter.Change
import scalafx.scene.control.{TextField, TextFormatter}

class HexField(val binding: StringProperty) extends TextField {

  private val regex = "[0-9A-F]*"
  private var unbound = false

  private val valueProperty = StringProperty({
    if (!binding().matches(regex) || binding().length != 6) throw new IllegalArgumentException
    binding()
  })

  value <==> binding

  value.onChange { (_, _, newValue) =>
    if (newValue == null) {
      text = ""
    } else {
      text = newValue
    }
  }

  textFormatter = new TextFormatter[String]({ change: Change =>

    val maxLen = 6
    val newText = change.controlNewText.toUpperCase

    if (newText.matches(regex) && newText.length < maxLen) {
      if (!unbound) value.unbindBidirectional(binding)
      unbound = true
      change.text = change.text.toUpperCase
    } else if (newText.matches(regex) && newText.length == maxLen) {
      if (unbound) value <==> binding
      change.text = change.text.toUpperCase
    } else {
      change.text = ""
    }

    if (change.anchor > maxLen) change.anchor = maxLen
    if (change.caretPosition > maxLen) change.caretPosition = maxLen

    change
  })

  text.onChange { (_, _, newValue) =>
    if (newValue == null || newValue == "") {
      value() = ""
    } else {
      value() = text()
    }
  }

  def value: StringProperty = valueProperty

  def value_=(newValue: String): Unit = {
    if (!newValue.matches(regex) || newValue.length != 6) throw new IllegalArgumentException
    value() = newValue
  }

}
