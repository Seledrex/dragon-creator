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
    final val textAreaWidth: Int = 300

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
    final val titleFontButton = "Title Font"
    final val bodyFontButton = "Body Font"

    // Alerts and dialogs
    final val alertConfirm = "Confirm New File"
    final val alertSuccess = "Success"
    final val alertError = "Error"
    final val dialogResChoice = "Select Resolution"
    final val dialogFontChooser = "Select Font"

    // File chooser
    final val fileChooserTitle = "Choose File"

    // None option
    final val noneOption = "None"

    // Default color
    final val defaultColor = "#f2f2f2"

    var titleFontSize = 30d
    var bodyFontSize = 14d

    final val resMap = Map(
        "854×480" -> (titleFontSize / 1.2, bodyFontSize / 1.2),
        "1024×576" -> (titleFontSize, bodyFontSize),
        "1280×720" -> (titleFontSize * 1.25, bodyFontSize * 1.25),
        "1600×900" -> (titleFontSize * 1.56, bodyFontSize * 1.56),
        "1920×1080" -> (titleFontSize * 1.875, bodyFontSize * 1.875))

    final val resOptions: Seq[String] = Seq("854×480", "1024×576", "1280×720", "1600×900", "1920×1080")

    // Default font
    final val defaultFont: String = "Helvetica"

    // Text box limits
    final val titleMaxLen: Int = 35
    final val bodyMaxLen: Int = 100
    final val bodyMaxLines: Int = 15

    // Prompt text
    final val titlePrompt = "Title"
    final val bodyPrompt = "Body"

}
