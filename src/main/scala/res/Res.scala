package res

/**
  * Image resources that are used in the application.
  */
object Res {

    final class Resource(val name: String, val fill: String, val border: String)

    final val baseCircle = new Resource(
        "Circle",
        "base_circle_fill.png",
        "base_circle_border.png")

    final val baseSquare = new Resource(
        "Square",
        "base_square_fill.png",
        "base_square_border.png")

    final val topCircle = new Resource(
        "Circle",
        "top_circle_fill.png",
        "top_circle_border.png")

    final val topSquare = new Resource(
        "Square",
        "top_square_fill.png",
        "top_square_border.png")

    final val bottomCircle = new Resource(
        "Circle",
        "bottom_circle_fill.png",
        "bottom_circle_border.png")

    final val bottomSquare = new Resource(
        "Square",
        "bottom_square_fill.png",
        "bottom_square_border.png")

}
