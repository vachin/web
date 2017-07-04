package controllers

import models.TextRequestModel
import play.api._
import org.slf4j.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._
import services.DataService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Application(dataService: DataService, logger: Logger) extends Controller {

  def index = Action {
    Ok(views.html.index("Vachin - We know what you are thinking..!"))
  }

  def getTexts(version: Option[Int], limit: Option[Int]) = Action.async {
    val futureTagsWithCount = dataService.getTagsWithCount(None, None)
    val futureTexts = dataService.getTexts(None, version, limit)
    futureTagsWithCount.flatMap{ tagsWithCount =>
      futureTexts.map{ texts =>
        Ok(views.html.tagged(tagsWithCount, texts, None))
      }
    }
  }

  def getTaggedTexts(tag: String, version: Option[Int], limit: Option[Int]) = Action.async {
    val futureTagsWithCount = dataService.getTagsWithCount(None, None)
    val futureTexts = dataService.getTexts(Some(tag), version, limit)
    futureTagsWithCount.flatMap{ tagsWithCount =>
      futureTexts.map{ texts =>
        Ok(views.html.tagged(tagsWithCount, texts, Some(tag)))
      }
    }
  }

  def searchTexts(q: String, tag: Option[String]) = Action.async {
    val futureTagsWithCount = dataService.getTagsWithCount(None, None)
    val futureTexts = dataService.searchTexts(q, tag)
    futureTagsWithCount.flatMap{ tagsWithCount =>
      futureTexts.map{ texts =>
        Ok(views.html.search(tagsWithCount, texts, tag, q))
      }
    }
  }

  def getText(textId: String) = Action.async {
    val futureRelatedTexts = dataService.searchTexts(textId.replaceAll("-", " "), None)
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

  def newText() = Action.async {
    Future(Ok(views.html.newText("New Text")))
  }

  def login = Action {
    Ok(views.html.login("Vachin - We know what you are thinking..!"))
  }

}