package controllers

import java.util.UUID
import javax.inject._

import models.{Present, User}
import play.Logger
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo._
import reactivemongo.api.ReadPreference
import reactivemongo.play.json.collection._
import reactivemongo.play.json._
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import reactivemongo.api.Cursor

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PresentController @Inject()(val reactiveMongoApi: ReactiveMongoApi, configuration: play.api.Configuration)(implicit exec: ExecutionContext) extends Controller with MongoController with ReactiveMongoComponents with Secured {

  def presentFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("presents"))

  def logging[A](action: Action[A]) = Action.async(action.parser) { request =>
    Logger.debug("Calling action")
    action(request)
  }

  def generateRandomUuidInPresent( newPresent : Present ) : Present= {
    def present: Present = new Present(UUID.randomUUID().toString, newPresent.getLabel, newPresent.getChildId
      , newPresent.getUrl, newPresent.getSantaName, newPresent.getPics
      , newPresent.getUrl2,newPresent.getUrl3, newPresent.getOrder);
    return present;
  }

  def create = AuthenticatedAction.async(parse.json) {
    authenticatedRequest => {
      if(!authenticatedRequest.user.success){
        Future.successful(Forbidden)
      }
      else {
        Json.fromJson[Present](authenticatedRequest.body) match {
          case JsSuccess(present, _) =>
            presentFuture.map(repositories => {
              val presentToInsert = generateRandomUuidInPresent(present)
              repositories.insert(presentToInsert)
              Created(Json.toJson(presentToInsert))
            });
          case JsError(errors) =>
            Future.successful(BadRequest("Could not build a present from the json provided. "))
        }
      }

    }
  }

  def update = AuthenticatedAction.async(parse.json) {
    authenticatedRequest => {
      if(!authenticatedRequest.user.success){
        Future.successful(Forbidden)
      }
      else {
        Json.fromJson[Present](authenticatedRequest.body) match {
          case JsSuccess(present, _) =>
            for {
              repositories <- presentFuture
              lastError <- repositories.findAndUpdate(Json.obj("id" -> present.id), present)
            } yield {
              Logger.debug("Updated 1 present from json");
              Ok(authenticatedRequest.body.toString())
            }
          case JsError(errors) =>
            Future.successful(BadRequest("Could not build a present from the json provided. "))
        }
      }
    }
  }


  def delete(uuid: String) = AuthenticatedAction.async {
    authenticatedRequest => {
      if(!authenticatedRequest.user.success){
        Future.successful(Forbidden)
      }
      else {
        for {
          repositories <- presentFuture
          lastError <- repositories.findAndRemove(Json.obj("id" -> uuid))
        } yield {
          Logger.debug("Created 1 present from json");
          Ok(authenticatedRequest.body.toString())
        }
      }
    }
  }


  def list() = AuthenticatedAction.async {
    authenticatedRequest => {

      if(!authenticatedRequest.user.success){
        Future.successful(Forbidden)
      }
      else {

        // let's do our query
        val futurePresentsList: Future[List[Present]] = presentFuture.flatMap {
          // find all cities with name `name`
          _.find(Json.obj()).
            // perform the query and get a cursor of JsObject
            cursor[Present](ReadPreference.primary).
            // Coollect the results as a list
            collect[List](-1, Cursor.FailOnError[List[Present]]())
        }

        // everything's ok! Let's reply with a JsValue
        futurePresentsList.map { presents =>
          Ok(Json.toJson(presents))
        }
      }
    }
  }

  def presentByName(child: String) = AuthenticatedAction.async {
    authenticatedRequest => {
      if(!authenticatedRequest.user.success){
        Future.successful(Forbidden)
      }
      else{
        // let's do our query
        val futurePresentsList: Future[List[Present]] = presentFuture.flatMap {
          // find all cities with name `name`
          _.find(Json.obj("childId" -> child)).
            // perform the query and get a cursor of JsObject
            cursor[Present](ReadPreference.primary).
            // Coollect the results as a list
            collect[List](-1, Cursor.FailOnError[List[Present]]())
        }

        // everything's ok! Let's reply with a JsValue
        futurePresentsList.map { presents =>
          Ok(Json.toJson(presents))
        }
      }
    }
  }


  def hello = AuthenticatedAction {
    Ok(Json.toJson("{Hello: 'World'}"))
  }

  def helloDude = AuthenticatedAction { authenticatedRequest =>
    Ok(Json.toJson("{Hello: '" + authenticatedRequest.user.name + "'}"))
  }

}


