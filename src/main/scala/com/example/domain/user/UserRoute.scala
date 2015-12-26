package com.example.domain.user

import akka.actor.{ActorSystem, Props, ActorNotFound, ActorRef}
import akka.stream.{Materializer, ActorMaterializer}
import akka.stream.scaladsl.Flow
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import scala.util.{Try, Success, Failure}
import spray.json.DefaultJsonProtocol
import spray.json._
import com.example.domain.user.UserAggregate._
import com.example.domain.Acknowledged
import com.example.domain.Error
import com.example.persistence.{CreateSchema, FindAllUsers}

object UserRoute {
  def create(implicit system: ActorSystem, materializer: ActorMaterializer, persistence: ActorRef) = 
    new UserRoute route
}

object UserJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat2(User.apply)
}

class UserRoute(implicit system: ActorSystem, persistence: ActorRef) extends DefaultJsonProtocol {
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.language.postfixOps 
  import UserJsonProtocol._
  
  implicit val timeout: Timeout = 5 seconds  
  
  def route(implicit materializer: Materializer) = {
    logRequestResult("eventsourcing-example") {
      pathPrefix("api" / "v1" / "users") {
        path(LongNumber) { id =>
          (put & formFields('email.as[String])) { email =>
            complete {
              system
                .actorSelection(s"user/user-$id")
                .resolveOne
                .recover {
                  case _: ActorNotFound => 
                    system.actorOf(Props(new UserAggregate(id.toString, persistence)), s"user-$id")
                }
                .map {                 
                  _ ? UserEmailUpdate(email) map {
                    case Acknowledged(_) => HttpResponse(status = OK, entity = "Email updated: " + email)
                    case Error(_, message) => HttpResponse(status = Conflict, entity = message)
                  }
                }
            }
          }
        } ~
        pathEnd {
          get {
             complete {
               (persistence ? FindAllUsers).mapTo[Try[Vector[User]]] map { 
                 case Success(users) => HttpResponse(status = OK, entity = users.toJson.compactPrint)
                 case Failure(ex) => HttpResponse(status = InternalServerError, entity = ex.getMessage)
               }
             }
          }
        }
      }
    }
  }
}