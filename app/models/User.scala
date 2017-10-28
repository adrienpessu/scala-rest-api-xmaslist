package models

import play.api.libs.json.Json

case class User(username: String
                , name: String
                , userId: String
                , email: String
                , role: String
                , success: Boolean
               )

object User {
  implicit val jsonFormatter = Json.format[User]
}
