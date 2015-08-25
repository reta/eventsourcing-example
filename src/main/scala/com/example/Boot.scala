package com.example

import akka.http.scaladsl.Http
import spray.json.DefaultJsonProtocol
import com.example.domain.user.UserRoute._

object Boot extends App with DefaultJsonProtocol {
  Http().bindAndHandle(route, "localhost", 38080)
}
