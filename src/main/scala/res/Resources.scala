package res

/**
  * Image resources that are used in the application.
  */
object Resources {

    final class Resource(val name: String, val fill: String, val border: String)

    final val base = new Resource(
        "base",
        "base_fill.png",
        "base_border.png")

    final val topCircle = new Resource(
        "top_circle",
        "top_circle_fill.png",
        "top_circle_border.png")

    final val topSquare = new Resource(
        "top_square",
        "top_square_fill.png",
        "top_square_border.png")

    final val bottomCircle = new Resource(
        "bottom_circle",
        "bottom_circle_fill.png",
        "bottom_circle_border.png")

    final val bottomSquare = new Resource(
        "bottom_square",
        "bottom_square_fill.png",
        "bottom_square_border.png")

}
