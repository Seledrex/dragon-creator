package app

import javafx.scene.{text => jfxt}
import scalafx.scene.text.Font

/**
  * Important properties defined here.
  */
object Properties {

  final val Title: String = "Dragon Creator"
  final val Resolution: (Double, Double) = (1280, 720)
  final val Padding: Int = 5

  // Initial property values
  final val ResPropInit: String = "1024×576"

  // Default panel locations
  final val optionPanelX = 250
  final val optionPanelY = 0
  final val textPanelX = 5
  final val textPanelY = 349
  final val CreatorPaneX = 395
  final val CreatorPaneY = 0
  final val ColorChooserPaneX = 5
  final val ColorChooserPaneY = 0
  final val ColorPalettePaneX = 27
  final val ColorPalettePaneY = 296

  // Widths
  final val PickerWidth: Int = 125
  final val ButtonWidth: Int = 85
  final val textAreaWidth: Int = 300

  // Check mark label
  final val dragModeCheckBoxName = "Drag mode"

  // Label names
  final val baseLabel = "Base"
  final val topLabel = "Top"
  final val bottomLabel = "Bottom"
  final val StatusLabel = "Untitled"

  // Button names
  final val NewButton = "New"
  final val OpenButton = "Open"
  final val SaveButton = "Save"
  final val SaveAsButton = "Save As..."
  final val SaveImageButton = "Save Image..."
  final val QuitButton = "Quit"
  final val ResetButton = "Reset"
  final val titleFontButton = "Title Font"
  final val bodyFontButton = "Body Font"
  final val applyColorButton = "Apply Color"

  // Tool names
  final val DragTool = "Drag Tool"
  final val MoveTool = "Move Tool"

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

  final val ScaleFactor = Map(
    "854×480"   -> 2.24824355971897,
    "1024×576"  -> 1.875,
    "1280×720"  -> 1.500,
    "1600×900"  -> 1.200,
    "1920×1080" -> 1.000
  )

  final val ResTuple = Map(
    "854×480"   -> (854d, 480d),
    "1024×576"  -> (1024d, 576d),
    "1280×720"  -> (1280d, 720d),
    "1600×900"  -> (1600d, 900d),
    "1920×1080" -> (1920d, 1080d)
  )

  final val ResolutionOptions: Seq[String] = Seq("854×480", "1024×576", "1280×720", "1600×900", "1920×1080")

  // Default font
  val DefaultTitleFont: jfxt.Font = Font.font("Helvetica", 25d)
  val DefaultBodyFont: jfxt.Font = Font.font("Helvetica", 12d)

  // Text box limits
  final val titleMaxLen: Int = 35
  final val bodyMaxLen: Int = 100
  final val bodyMaxLines: Int = 15

  // Prompt text
  final val titlePrompt = "Title"
  final val bodyPrompt = "Body"

}
