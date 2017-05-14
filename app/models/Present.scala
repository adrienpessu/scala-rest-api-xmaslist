package models

import play.api.libs.json.Json

import scala.beans.BeanProperty

case class Present(id: String, @BeanProperty label: String
                   , @BeanProperty childId: String
                   , @BeanProperty url: String
                   , @BeanProperty url2: String
                   , @BeanProperty url3: String
                   , @BeanProperty santaName: String
                   , @BeanProperty pics: String)

object Present {
  implicit val formatter = Json.format[Present]
  implicit val writer = Json.writes[Present]
}