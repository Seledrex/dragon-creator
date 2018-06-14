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
    final val basePropName = "baseProp"
    final val topPropName = "topProp"
    final val bottomPropName = "bottomProp"

    // Check mark label
    final val dragModeCheckBoxName = "Drag mode"

    // Label names
    final val baseLabel = "Base"
    final val topLabel = "Top"
    final val bottomLabel = "Bottom"

}
