package res

/**
  * Important properties defined here.
  */
object Prop {

    final val title: String = "Dragon Creator"
    final val resolution: (Double, Double) = (1280, 720)
    final val imgRes: (Double, Double) = (resolution._1 / 1.25, resolution._2 / 1.25)
    final val imgResStr = "1024×576"
    final val padding: Int = 5

    // Widths
    final val pickerWidth: Int = 125
    final val buttonWidth: Int = 85

    // Check mark label
    final val dragModeCheckBoxName = "Drag mode"

    // Label names
    final val baseLabel = "Base"
    final val topLabel = "Top"
    final val bottomLabel = "Bottom"
    final val statusLabel = "Untitled"

    // Button names
    final val newButton = "New"
    final val openButton = "Open"
    final val saveButton = "Save"
    final val saveAsButton = "Save As..."
    final val saveImageButton = "Save Image..."
    final val quitButton = "Quit"
    final val resetButton = "Reset"

    // Alerts
    final val alertConfirm = "Confirm New File"
    final val alertSuccess = "Success"
    final val alertError = "Error"

    // File chooser
    final val fileChooserTitle = "Choose File"

    // None option
    final val noneOption = "None"

    // Default color
    final val defaultColor = "#f2f2f2"

}
