package res

/**
  * Important properties defined here.
  */
object Prop {

    final val title: String = "Dragon Creator"
    final val resolution: (Double, Double) = (1280, 720)
    final val imgRes: (Double, Double) = (resolution._1 / 1.25, resolution._2 / 1.25)
    final val padding: Int = 5

    // Widths
    final val pickerWidth: Int = 125
    final val buttonWidth: Int = 85

    // Property names
    final val dragModePropName = "dragModeProp"
    final val baseCBPropName = "baseProp"
    final val baseCPPropName = "baseCPProp"
    final val topCBPropName = "topCBProp"
    final val topCPPropName = "topCPProp"
    final val bottomCBPropName = "bottomCBProp"
    final val bottomCPPropName = "bottomCPProp"

    // Check mark label
    final val dragModeCheckBoxName = "Drag mode"

    // Label names
    final val baseLabel = "Base"
    final val topLabel = "Top"
    final val bottomLabel = "Bottom"

    // Button names
    final val newButton = "New"
    final val openButton = "Open"
    final val saveButton = "Save"
    final val saveAsButton = "Save As..."
    final val saveImageButton = "Save Image..."
    final val quitButton = "Quit"

    // Alerts
    final val alertConfirm = "Confirm New File"
    final val alertSuccess = "Success"
    final val alertError = "Error"

    // File chooser
    final val fileChooserTitle = "Choose File"

    // None option
    final val noneOption = "None"

}
