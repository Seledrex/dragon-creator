package res

/**
  * Image resources that are used in the application.
  */
object Res {

  @SerialVersionUID(101L)
  final class Resource(val name: String, val fill: String, val outline: String) extends Serializable

  final val baseDragon = new Resource(
    "Dragon",
    "bases/western1/western1_fill.png",
    "bases/western1/western1_outline.png")

  final val horn1 = new Resource(
    "Horn 1",
    "horns/horn1/horn1_fill.png",
    "horns/horn1/horn1_outline.png"
  )

}
