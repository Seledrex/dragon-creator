package res

import javafx.scene.{text => jfxt}
import scalafx.scene.text.Font

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

    final val getMultiplier = Map(
        "854×480" -> 1.00,
        "1024×576" -> 1.20,
        "1280×720" -> 1.50,
        "1600×900" -> 1.87,
        "1920×1080" -> 2.25
    )

    final val resOptions: Seq[String] = Seq("854×480", "1024×576", "1280×720", "1600×900", "1920×1080")

    // Default font
    def getDefaultTitleFont: jfxt.Font = Font.font("Helvetica", 25d)
    def getDefaultBodyFont: jfxt.Font = Font.font("Helvetica", 12d)

    // Text box limits
    final val titleMaxLen: Int = 35
    final val bodyMaxLen: Int = 100
    final val bodyMaxLines: Int = 15

    // Prompt text
    final val titlePrompt = "Title"
    final val bodyPrompt = "Body"

}
