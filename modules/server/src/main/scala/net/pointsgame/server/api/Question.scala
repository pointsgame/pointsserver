package net.pointsgame.server.api

sealed trait Question

case class RegisterQuestion(name: String, password: String) extends Question
