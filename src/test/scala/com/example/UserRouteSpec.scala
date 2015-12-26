package com.example

import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import scala.concurrent.duration._
import scala.language.postfixOps
import com.example.persistence.Persistence
import akka.stream.ActorMaterializer
import com.example.persistence.PersistenceService
import com.example.persistence.CreateSchema


class UserRouteSpec extends FlatSpec with ScalatestRouteTest with Matchers {
  import com.example.domain.user.UserRoute
  import spray.json._
  import com.example.domain.user.UserAggregate.User
  import com.example.domain.user.UserJsonProtocol._
  
  implicit def executionContext = scala.concurrent.ExecutionContext.Implicits.global
  
  implicit val persistence = system.actorOf(Props[PersistenceService], "persistence")
  persistence ! CreateSchema
    
  it should "return success on email update" in {
    Put("http://localhost:38080/api/v1/users/123", FormData("email" ->  "a@b.com")) ~> UserRoute.create ~> check {
      response.status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "Email updated: a@b.com"
    }
  }
  
  it should "return conflict on non-unique update" in {
    Put("http://localhost:38080/api/v1/users/123", FormData("email" ->  "a@b.com")) ~> UserRoute.create ~> check {
      response.status shouldBe StatusCodes.OK
    }
    
    Put("http://localhost:38080/api/v1/users/124", FormData("email" ->  "a@b.com")) ~> UserRoute.create ~> check {
      response.status shouldBe StatusCodes.Conflict
      responseAs[String] shouldBe "Email 'a@b.com' already registered"
    }
  }
  
  it should "return all users" in {
    Put("http://localhost:38080/api/v1/users/123", FormData("email" ->  "a@b.com")) ~> UserRoute.create ~> check {
      response.status shouldBe StatusCodes.OK
    }
    
    Get("http://localhost:38080/api/v1/users") ~> UserRoute.create ~> check {
      response.status shouldBe StatusCodes.OK
      responseAs[String] shouldBe List(User("123", "a@b.com")).toJson.compactPrint
    }
  }
}
