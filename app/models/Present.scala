package models

import play.api.libs.json.Json

case class Present(id: String, label: String, childId: String,url: String ,santaName: String, pics: String)

object Present {
  implicit val formatter = Json.format[Present]
  implicit val writer = Json.writes[Present]
}