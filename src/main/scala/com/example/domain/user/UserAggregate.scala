package com.example.domain.user

import akka.actor._
import akka.pattern.ask
import akka.persistence._
import com.example.domain._

object UserAggregate {
  case class User(id: String, email: String = "") extends State[User] {
    override def updateState(event: Event): State[User] = event match {
      case UserEmailUpdated(id, email) => copy(email = email)
    }
  }

  case class UserEmailUpdate(email: String) extends Command
  case class UserEmailUpdated(id: String, email: String) extends Event
}

class UserAggregate(id: String) extends PersistentActor with ActorLogging {
  import UserAggregate._

  override def persistenceId = id
  var state: State[User] = User(id)

  def updateState(event: Event): Unit = {
    state = state.updateState(event)
  }
  
  val receiveCommand: Receive = {
    case UserEmailUpdate(email) => {
      persist(UserEmailUpdated(id, email)) { event =>
        updateState(event)
        sender ! Acknowledged(id)
      }
    }
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: User) => state = snapshot
  }
}
