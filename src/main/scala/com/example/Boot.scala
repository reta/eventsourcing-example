package com.example

import akka.http.scaladsl.Http
import akka.actor.{ActorSystem, Props}
import spray.json.DefaultJsonProtocol
import com.example.domain.user.UserRoute
import com.example.persistence.PersistenceService
import akka.stream.ActorMaterializer
import com.example.domain.user.UserJournal
import com.example.persistence.CreateSchema
import com.example.domain.user.InitSchema

object Boot extends App with DefaultJsonProtocol {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  implicit val persistence = system.actorOf(Props[PersistenceService], "persistence")  
  val journal = system.actorOf(Props(new UserJournal(persistence)), "user-journal")

  persistence ! CreateSchema
  journal ! InitSchema

  Http().bindAndHandle(UserRoute.create, "localhost", 38080)  
}
