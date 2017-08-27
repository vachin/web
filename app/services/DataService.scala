package services

import models._
import org.slf4j.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import utils.Utils

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by jyothi on 25/6/17.
  */
class DataService(ws: WSClient, serverHost: String, logger: Logger) {

  def getText(textId: String) = {
    val api = serverHost + "texts/" + textId
    ws.url(api).get().map { response =>
      val result = response.status match {
        case 200 => Json.toJson(response.json)
        case _ => Json.toJson(false)
      }
      result.validate[TextModel].asOpt
    }
  }

  def insertText(body: TextRequestModel) = {
    val api = serverHost + "texts"
    ws.url(api).post(Json.toJson(body)).map { response => 
      val result = response.status match {
        case 200 => Json.toJson(response.json)
        case _ => Json.toJson("")
      }
      result.validate[String].getOrElse("")
    }
  }

  def updateText(textId: String, body: TextRequestModel) = {
    val api = serverHost + "texts"
    val queryStrings = Utils.generateQueryParams(Some(textId), None, None, None, None)
    ws.url(api).withQueryString(queryStrings: _*).put(Json.toJson(body)).map { response =>
      val result = response.status match {
        case 200 => Json.toJson(response.json)
        case _ => Json.toJson("")
      }
      result.validate[String].getOrElse("")
    }
  }

  def getTexts(tagId: Option[String], version: Option[Int], limit: Option[Int]) = {
    val api = serverHost + "texts"
    val queryStrings = Utils.generateQueryParams(None, tagId, version, limit, None)
    ws.url(api).withQueryString(queryStrings: _*).get().map { response => 
      val result = response.status match {
        case 200 => Json.toJson(response.json)
        case _ => Json.toJson(false)
      }
      result.validate[TextPaginatedModel].getOrElse(TextPaginatedModel.empty)
    }
  }

  def searchTexts(q: String, tagId: Option[String], version: Option[Int], limit: Option[Int], words: Option[String]) = {
    val api = serverHost + "texts/search"
    val queryStrings = Utils.generateQueryParams(None, tagId, version, limit, Some(q), words)
    ws.url(api).withQueryString(queryStrings: _*).get().map { response =>
      val result = response.status match {
        case 200 => Json.toJson(response.json)
        case _ => Json.toJson(false)
      }
      result.validate[TextPaginatedModel].getOrElse(TextPaginatedModel.empty)
    }
  }

  def getTag(tagId: String) = {
    val api = serverHost + "tags/" + tagId
    val queryStrings = Utils.generateQueryParams(None, None, None, None, None)
    ws.url(api).withQueryString(queryStrings: _*).get().map { response =>
      val result = response.status match {
        case 200 => Json.toJson(response.json)
        case _ => Json.toJson(false)
      }
      result.validate[TagModel].asOpt
    }
  }

  def putTag(tag: String) = {
    val api = serverHost + "tags/" + tag
    val queryStrings = Utils.generateQueryParams(None, None, None, None, None)
    ws.url(api).withQueryString(queryStrings: _*).get().map { response =>
      val result = response.status match {
        case 200 => Json.toJson(response.json)
        case _ => Json.toJson("")
      }
      result.validate[String].getOrElse("")
    }
  }

  def getTags() = {
    val api = serverHost + "tags"
    val queryStrings = Utils.generateQueryParams(None, None, None, None, None)
    ws.url(api).withQueryString(queryStrings: _*).get().map { response => 
      val result = response.status match {
        case 200 => Json.toJson(response.json)
        case _ => Json.toJson(false)
      }
      result.validate[List[TagModel]].getOrElse(List.empty[TagModel])
    }
  }

  def getTagsWithCount(version: Option[Int], limit: Option[Int]) = {
    val api = serverHost + "tag-counts"
    val queryStrings = Utils.generateQueryParams(None, None, version, limit, None)
    ws.url(api).withQueryString(queryStrings: _*).get().map { response => 
      val result = response.status match {
        case 200 => Json.toJson(response.json)
        case _ => Json.toJson(false)
      }
      result.validate[List[TagWithCount]].getOrElse(List.empty[TagWithCount])
    }
  }

  def searchTags(q: String) = {
    val api = serverHost + "tags/search"
    val queryStrings = Utils.generateQueryParams(None, None, None, None, Some(q))
    ws.url(api).withQueryString(queryStrings: _*).get().map { response => 
      val result = response.status match {
        case 200 => Json.toJson(response.json)
        case _ => Json.toJson(false)
      }
      result.validate[List[TagModel]].getOrElse(List.empty[TagModel])
    }
  }

  def getRelatedTexts(keywords: String, tag: Option[String]): Future[TextPaginatedModel] = {
    val futureTextsFromKeywords = searchTexts(keywords.replaceAll("-", " "), None, Some(1), Some(15), Some("1")) //FIXME: not fixed for this limit value
    val futureTaggedTexts = tag match {
      case Some(someTag) => getTexts(tag, None, None)
      case None => Future(TextPaginatedModel.empty)
    }
    futureTextsFromKeywords.flatMap{ textsFromKeywords =>
      if(textsFromKeywords.meta.total < 5){
        futureTaggedTexts.map { taggedTexts =>
          TextPaginatedModel.emptyWithList(textsFromKeywords.texts ++ taggedTexts.texts)
        }
      }else{
        Future(textsFromKeywords)
      }
    }
  }
  
}
