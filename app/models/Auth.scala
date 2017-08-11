package models

import play.api.libs.json.Json

/**
  * Created by jyothi on 11/8/17.
  */
case class Auth()

case class LoginModel(username: String, password: String)

object LoginModel {
  val loginModelFormat = Json.format[LoginModel]
}