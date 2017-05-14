package models

import play.api.libs.json.Json

import scala.beans.BeanProperty

case class Present(id: String, @BeanProperty label: String
                   , @BeanProperty childId: String
                   , @BeanProperty url: String
                   , @BeanProperty santaName: String
                   , @BeanProperty pics: String
                   , @BeanProperty url2: Option[String]
                   , @BeanProperty url3: Option[String]
                   , @BeanProperty order: Option[Int]
                  )

object Present {
  implicit val formatter = Json.format[Present]
  implicit val writer = Json.writes[Present]
}