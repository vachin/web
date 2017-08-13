package controllers

import com.google.inject.Inject
import models.{LoginModel, TextRequestModel}
import models.Forms._
import play.api._
import org.slf4j.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._
import services.DataService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import utils.Utils._

@Inject
class Application (dataService: DataService, logger: Logger, val messagesApi: MessagesApi, authStore: List[LoginModel]) extends Controller with I18nSupport {

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

  def getTags(limit: Option[Int]) = Action.async { implicit request =>
    dataService.getTagsWithCount(None, limit).map { tagsWithCount =>
      Ok(views.html.tags(tagsWithCount, tagsWithCount))
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
        Redirect(routes.Application.login())
      }
    }
  }

  def login() = Action.async { implicit request =>
    Future {
      request.session.get("user").map { user =>
        Redirect(routes.Application.newText()).withSession(
          "user" -> user)
      }.getOrElse {
        Ok(views.html.login("Welcome!"))
      }
    }
  }

  def loginPost = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => {
        Ok(views.html.login("Invalid Request"))
      },
      userData => {
        if(isObjectInList(authStore, userData)){
          Redirect(routes.Application.newText()).withSession(
            "user" -> userData.username)
        }else{
          Redirect(routes.Application.login())
        }
      }
    )
  }

  def logout = Action.async { implicit request =>
    Future{
      Redirect(routes.Application.login()).withNewSession
    }
  }

  def about = Action.async { implicit request =>
    dataService.getTagsWithCount(None, None).map{ tags =>
      Ok(views.html.about(tags))
    }
  }

  def terms = Action.async { implicit request =>
    dataService.getTagsWithCount(None, None).map{ tags =>
      Ok(views.html.terms(tags))
    }
  }

  def privacy = Action.async { implicit request =>
    dataService.getTagsWithCount(None, None).map{ tags =>
      Ok(views.html.privacy(tags))
    }
  }

  def contact = Action.async { implicit request =>
    dataService.getTagsWithCount(None, None).map{ tags =>
      Ok(views.html.contact(tags))
    }
  }

}