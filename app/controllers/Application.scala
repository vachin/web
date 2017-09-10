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
    dataService.getTagsWithCount(None, None).map{ tagsWithCount =>
      Ok(views.html.index(tagsWithCount))
    }
  }

  def getTexts(page: Option[Int], limit: Option[Int]) = Action.async { implicit request =>
    val futureTagsWithCount = dataService.getTagsWithCount(None, None)
    val futureTexts = dataService.getTexts(None, page, limit)
    futureTagsWithCount.flatMap{ tagsWithCount =>
      futureTexts.map{ texts =>
        Ok(views.html.tagged(tagsWithCount, texts, None))
      }
    }
  }

  def getTaggedTexts(tag: String, page: Option[Int], limit: Option[Int]) = Action.async { implicit request =>
    val futureTagsWithCount = dataService.getTagsWithCount(None, None)
    val futureTexts = dataService.getTexts(Some(tag), page, limit)
    futureTagsWithCount.flatMap{ tagsWithCount =>
      futureTexts.map{ texts =>
        Ok(views.html.tagged(tagsWithCount, texts, Some(tag)))
      }
    }
  }

  def searchTexts(q: Option[String], tag: Option[String], page: Option[Int], limit: Option[Int]) = Action.async { implicit request =>
    val futureTagsWithCount = dataService.getTagsWithCount(None, None)
    val query = q.getOrElse("")
    val futureTextsWithWords = dataService.searchTexts(query, tag, page, limit, Some("1"))
    val futureTexts = dataService.searchTexts(query, tag, page, limit, None)
    futureTagsWithCount.flatMap{ tagsWithCount =>
      futureTextsWithWords.flatMap{ texts =>
        if(texts.meta.total > 0){
          Future(Ok(views.html.search(tagsWithCount, texts, tag, query)))
        }else{
          futureTexts.map{ textsFromSearch =>
            Ok(views.html.search(tagsWithCount, textsFromSearch, tag, query))
          }
        }
      }
    }
  }

  def getText(textId: String) = Action.async { implicit request =>
    val futureRelatedTexts = dataService.getRelatedTexts(textId, None) //FIXME:
    val futureText = dataService.getText(textId)
    val futureTagsWithCount = dataService.getTagsWithCount(None, None)
    futureRelatedTexts.flatMap{ relatedTexts =>
      futureText.flatMap{ text =>
        futureTagsWithCount.map{ tagsWithCount =>
          Ok(views.html.text(tagsWithCount, text, relatedTexts, None))
        }
      }
    }
  }

  def updateText(textId: String) = Action.async { implicit request =>
    request.session.get("user").map { user =>
      dataService.getText(textId).map(text =>
        Ok(views.html.updateText(text))
      )
    }.getOrElse {
      Future(Redirect(routes.Application.login()))
    }
  }

  def updatePostText(textId: String) = Action.async(parse.json) { implicit request =>
    request.session.get("user").map { user =>
      val textRequestModelOpt = request.body.validate[TextRequestModel]
      val textRequestModel = textRequestModelOpt match {
        case error: JsError => logger.warn("Errors: " + JsError.toJson(error)); None
        case model: JsSuccess[_] => Some(model.get.asInstanceOf[TextRequestModel])
      }
      if(textRequestModel.isDefined) {
        dataService.updateText(textId, TextRequestModel.withUser(textRequestModel.get, user)).map(result =>
          Ok(result)
        )
      }else{
        Future(BadRequest(""))
      }
    }.getOrElse {
      Future(Unauthorized(""))
    }
  }

  def getTags(limit: Option[Int]) = Action.async { implicit request =>
    dataService.getTagsWithCount(None, limit).map { tagsWithCount =>
      Ok(views.html.tags(tagsWithCount, tagsWithCount))
    }
  }

  def postText() = Action.async(parse.json) { implicit request =>
    request.session.get("user").map { user =>
      val textRequestModelOpt = request.body.validate[TextRequestModel]
      val textRequestModel = textRequestModelOpt match {
        case error: JsError => logger.warn("Errors: " + JsError.toJson(error)); None
        case model: JsSuccess[_] => Some(model.get.asInstanceOf[TextRequestModel])
      }
      if(textRequestModel.isDefined) {
        dataService.insertText(TextRequestModel.withUser(textRequestModel.get, user)).map(result =>
          Ok(result)
        )
      }else{
        Future(BadRequest(""))
      }
    }.getOrElse {
      Future(Unauthorized(""))
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