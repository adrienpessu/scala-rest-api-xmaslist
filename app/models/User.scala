package models

import play.api.libs.json.Json

case class User(name: String
                , nickname: String
                , userId: String
                , email: String
                , success: Boolean
               )

object User {
  implicit val jsonFormatter = Json.format[User]
}
