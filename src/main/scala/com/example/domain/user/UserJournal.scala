package com.example.domain.user

import com.example.domain.user.UserAggregate.UserEmailUpdated

import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.persistence.query.PersistenceQuery
import akka.stream.ActorMaterializer
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.actor.Actor
import com.example.persistence.UpdateUser

case class InitSchema()

class UserJournal(persistence: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case InitSchema => {
      val journal = PersistenceQuery(context.system)
        .readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
      val source = journal.currentPersistenceIds()
      
      implicit val materializer = ActorMaterializer()
      source
        .runForeach { persistenceId => 
          journal.currentEventsByPersistenceId(persistenceId, 0, Long.MaxValue)
            .runForeach { event => 
              event.event match {
                case UserEmailUpdated(id, email) => persistence ! UpdateUser(id, email)
              }
            }
        }
    }
  }
}