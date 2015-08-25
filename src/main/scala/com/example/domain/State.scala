package com.example.domain

trait Event
trait Command

trait State[T] {
  def updateState(event: Event): State[T]
}

case class Acknowledged(id: String)
case class Error(id: String, message: String)
