package com.example

import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import scala.concurrent.duration._
import scala.language.postfixOps


class UserRouteSpec extends FlatSpec with ScalatestRouteTest with Matchers with BeforeAndAfterAll {
  import com.example.domain.user.UserRoute
  
  implicit def executionContext = scala.concurrent.ExecutionContext.Implicits.global

  "UserRoute" should "return success on email update" in {
    Put("http://localhost:38080/api/v1/users/123", FormData("email" ->  "a@b.com")) ~> UserRoute.route ~> check {
      response.status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "Email updated: a@b.com"
    }
  }
}
