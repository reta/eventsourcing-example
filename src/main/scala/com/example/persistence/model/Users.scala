package com.example.persistence.model

import slick.driver.H2Driver.api._
import com.example.domain.user.UserAggregate.User

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def id = column[String]("ID", O.PrimaryKey)
  def email = column[String]("EMAIL", O.Length(512))
  def uniqueEmail = index("USERS_EMAIL_IDX", email, true)
  def * = (id, email) <> (User.tupled, User.unapply)
}