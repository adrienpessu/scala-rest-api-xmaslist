package controllers

import io.jsonwebtoken.Jwts
import models.User
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.mvc.Http.HeaderNames._

import scala.concurrent.Future

class AuthenticatedRequest[A](val user: User, val request: Request[A]) extends WrappedRequest[A](request)
class AuthenticatedAsyncRequest[A](user: User, request: Request[A]) extends WrappedRequest[A](request)

trait Secured {
  def SecuredAction = AuthenticatedAction
}

object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {

  def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) ={
    val auth = request.headers.get(AUTHORIZATION)
    if(auth.isDefined) {
      val token = auth.get.substring(6).trim

      val jwtSecret = Option(sys.env("JWT_SECRET")).getOrElse("password")

      try {
       val claims = Jwts.parser()
          .setSigningKey(jwtSecret)
          .parseClaimsJws(token.replace("Bearer ", ""))
          .getBody.getSubject

       val jsonMap: JsObject = Json.parse(claims).asInstanceOf[JsObject]

       jsonMap.\("username").get.toString()

        val user = User(jsonMap.\("username").get.toString(),
          jsonMap.\("username").get.toString(),
          jsonMap.\("username").get.toString(),
          jsonMap.\("email").get.toString(),
          jsonMap.\("role").get.toString(),
          true
        )

        Logger.info("user" + user)
        block(new AuthenticatedRequest[A](user, request))
      }
      catch {
        case e: Exception => block(new AuthenticatedRequest[A](User("Anonymous", "", "", "", "", false), request))
      }

    }
    else{
      block(new AuthenticatedRequest[A](User("Anonymous", "","", "", "", false), request))
    }
  }
}


