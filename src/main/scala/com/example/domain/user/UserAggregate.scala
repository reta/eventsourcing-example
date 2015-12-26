package com.example.domain.user

import akka.actor._
import akka.pattern.ask
import akka.pattern.pipe
import akka.persistence._
import scala.Some
import scala.util.{Try, Success, Failure}
import com.example.domain._
import com.example.persistence.Persistence
import com.example.persistence.{FindUserByEmail, UpdateUser}
import akka.util.Timeout
import scala.concurrent.Await
import scala.util.control.NonFatal

object UserAggregate {
  case class User(id: String, email: String = "") extends State[User] {
    override def updateState(event: Event): State[User] = event match {
      case UserEmailUpdated(id, email) => copy(email = email)
    }
  }

  case class UserEmailUpdate(email: String) extends Command
  case class UserEmailUpdated(id: String, email: String) extends Event
}

class UserAggregate(id: String, persistence: ActorRef) extends PersistentActor with ActorLogging {
  import UserAggregate._  
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout: Timeout = 3 seconds 
  override def persistenceId = id
  var state: State[User] = User(id)

  def updateState(event: Event): Unit = {
    state = state.updateState(event)
  }
  
  val receiveCommand: Receive = {
    case UserEmailUpdate(email) => 
      try {
        val future = (persistence ? FindUserByEmail(email)).mapTo[Try[Option[User]]]
        val result = Await.result(future, timeout.duration) match {
          case Failure(ex) => Error(id, ex.getMessage)
          case Success(Some(user)) if user.id != id => Error(id, s"Email '$email' already registered")
          case _ => persist(UserEmailUpdated(id, email)) { event =>
            updateState(event)
            persistence ! UpdateUser(id, email)
          }
          Acknowledged(id)
        }
        
        sender ! result
    } catch {
      case ex: Exception if NonFatal(ex) => sender ! Error(id, ex.getMessage) 
    }
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: User) => state = snapshot
  }
}
