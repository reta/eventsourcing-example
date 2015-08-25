package com.example.domain.user

import akka.http.scaladsl.Http
import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import scala.concurrent.duration._
import spray.json.DefaultJsonProtocol
import com.example.domain.user.UserAggregate._
import com.example.domain.Acknowledged
import com.example.domain.Error
import akka.actor.ActorNotFound

object UserRoute {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.language.postfixOps
  
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = 5 seconds

  val route = {
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
                    system.actorOf(Props(new UserAggregate(id.toString)), s"user-$id")
                }
                .map {                 
                  _ ? UserEmailUpdate(email) map {
                    case Acknowledged(_) => HttpResponse(status = OK, entity = "Email updated: " + email)
                    case Error(_, message) => HttpResponse(status = Conflict, entity = message)
                  }
                }
            }
          }
        }
      }
    }
  }
}