package com.example.persistence

import slick.driver.H2Driver.api._
import akka.actor.{ActorLogging, Actor}
import akka.pattern.ask
import akka.pattern.pipe
import com.example.persistence.model.Users
import com.example.domain.user.UserAggregate.User

case class CreateSchema()
case class FindUserByEmail(email: String)
case class UpdateUser(id: String, email: String)
case class FindAllUsers()

trait Persistence {
  val users = TableQuery[Users]  
  val db = Database.forConfig("db")
}

class PersistenceService extends Actor with ActorLogging with Persistence {
  import scala.concurrent.ExecutionContext.Implicits.global
   
  def receive = {
    case CreateSchema => db.run(DBIO.seq(users.schema.create))
      
    case UpdateUser(id, email) => {
      val query = for { user <- users if user.id === id } yield user.email
      db.run(users.insertOrUpdate(User(id, email)))
    }
    
    case FindUserByEmail(email) => {
      val replyTo = sender
      db.run(users.filter( _.email === email.toLowerCase).result.headOption) 
        .onComplete { replyTo ! _ }
    }
    
    case FindAllUsers => {
      val replyTo = sender
      db.run(users.result) onComplete { replyTo ! _ }
    }
  }
}