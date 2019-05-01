package object models {
  sealed trait Extension
  final case object PostGIS         extends Extension
  final case object Earthdistance   extends Extension

  val defaultExtension: Extension = Earthdistance
}
