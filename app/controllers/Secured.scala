package controllers

import com.google.common.io.BaseEncoding

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._
import models.User
import play.api.Logger
import play.mvc.Http.HeaderNames._
import org.jose4j.keys.HmacKey
import org.jose4j.jwt.consumer.JwtConsumer
import org.jose4j.jwt.consumer.JwtConsumerBuilder

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
        val key = new HmacKey(jwtSecret.getBytes("UTF-8"))
        val jwtConsumer = new JwtConsumerBuilder().setRequireExpirationTime.setAllowedClockSkewInSeconds(30).setRequireSubject.setExpectedIssuer("the issuer").setExpectedAudience("the audience").setVerificationKey(key).setRelaxVerificationKeyValidation.build
        // relaxes key length requirement
        val claims = jwtConsumer.processToClaims(token)

        val user = User(claims.getClaimValue("payload").toString,
          claims.getClaimValue("payload").toString,
          claims.getClaimValue("payload").toString,
          "",
          true
        )

        Logger.info("user" + user)
        block(new AuthenticatedRequest[A](user, request))
      }
      catch {
        case e: Exception => block(new AuthenticatedRequest[A](User("Anonymous", "", "", "", false), request))
      }

    }
    else{
      block(new AuthenticatedRequest[A](User("Anonymous","", "", "", false), request))
    }
  }
}


