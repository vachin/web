package controllers

import models.TextRequestModel
import play.api._
import org.slf4j.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._
import services.DataService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import utils.Utils._

class Application(dataService: DataService, logger: Logger) extends Controller {

  def index = Action.async { implicit request =>
    Future(Ok(views.html.index(title)))
  }

  def getTexts(version: Option[Int], limit: Option[Int]) = Action.async { implicit request =>
    val futureTagsWithCount = dataService.getTagsWithCount(None, None)
    val futureTexts = dataService.getTexts(None, version, limit)
    futureTagsWithCount.flatMap{ tagsWithCount =>
      futureTexts.map{ texts =>
        Ok(views.html.tagged(tagsWithCount, texts, None))
      }
    }
  }

  def getTaggedTexts(tag: String, version: Option[Int], limit: Option[Int]) = Action.async { implicit request =>
    val futureTagsWithCount = dataService.getTagsWithCount(None, None)
    val futureTexts = dataService.getTexts(Some(tag), version, limit)
    futureTagsWithCount.flatMap{ tagsWithCount =>
      futureTexts.map{ texts =>
        Ok(views.html.tagged(tagsWithCount, texts, Some(tag)))
      }
    }
  }

  def searchTexts(q: String, tag: Option[String], version: Option[Int], limit: Option[Int]) = Action.async { implicit request =>
    val futureTagsWithCount = dataService.getTagsWithCount(None, None)
    val futureTexts = dataService.searchTexts(q, tag, version, limit)
    futureTagsWithCount.flatMap{ tagsWithCount =>
      futureTexts.map{ texts =>
        Ok(views.html.search(tagsWithCount, texts, tag, q))
      }
    }
  }

  def getText(textId: String) = Action.async { implicit request =>
    val futureRelatedTexts = dataService.searchTexts(textId.replaceAll("-", " "), None, Some(1), Some(10)) //FIXME: not fixed for this limit value
    val futureTagsWithCount = dataService.getTagsWithCount(None, None)
    val futureText = dataService.getText(textId)
    futureRelatedTexts.flatMap{ relatedTexts =>
      futureTagsWithCount.flatMap{ tagsWithCount =>
        futureText.map{ text =>
          Ok(views.html.text(tagsWithCount, text, relatedTexts, None))
        }
      }
    }
  }

  def postText() = Action.async(parse.json) { implicit request =>
    val textRequestModelOpt = request.body.validate[TextRequestModel]
    val textRequestModel = textRequestModelOpt match {
      case error: JsError => logger.warn("Errors: " + JsError.toJson(error)); None
      case model: JsSuccess[_] => Some(model.get.asInstanceOf[TextRequestModel])
    }
    if(textRequestModel.isDefined) {
      dataService.putText(textRequestModel.get).map(result =>
        Ok(result)
      )
    }else{
      Future(BadRequest(""))
    }
  }

  def newText() = Action.async { implicit request =>
    Future {
      request.session.get("user").map { user =>
        Ok(views.html.newText("New Text"))
      }.getOrElse {
        Unauthorized(views.html.login("Oops, you are not connected"))
      }
    }
  }

  def login = Action.async { implicit request =>
    Future {
      request.session.get("user").map { user =>
        Ok(views.html.newText(title)).withSession(
          "user" -> user)
      }.getOrElse {
        Unauthorized(views.html.login("Welcome!"))
      }
    }
  }

}