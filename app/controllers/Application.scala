package controllers

import play.api._
import play.api.mvc._

class Application extends Controller with {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def getTaggedTexts(tag: String) = Action.async {
    Ok(views.html.index("Your new application is ready."))
  }

  def searchTexts(q: String, tag: Option[String]) = Action.async {
    Ok(views.html.index("Your new application is ready."))
  }

}