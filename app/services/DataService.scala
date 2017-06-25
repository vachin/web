package services

import models.{TagModel, TagWithCount, TextModel, TextRequestModel}
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

  def putText(body: TextRequestModel) = {
    val api = serverHost + "texts"
    ws.url(api).post(Json.toJson(body)).map { response => 
      val result = response.status match {
        case 200 => Json.toJson(response.json)
        case _ => Json.toJson(false)
      }
      result.validate[Boolean].getOrElse(false)
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
      result.validate[List[TextModel]].getOrElse(List.empty[TextModel])
    }
  }

  def searchTexts(q: String, tagId: Option[String]) = {
    val api = serverHost + "texts/search"
    val queryStrings = Utils.generateQueryParams(None, tagId, None, None, Some(q))
    ws.url(api).withQueryString(queryStrings: _*).get().map { response => 
      val result = response.status match {
        case 200 => Json.toJson(response.json)
        case _ => Json.toJson(false)
      }
      result.validate[List[TextModel]].getOrElse(List.empty[TextModel])
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
        case _ => Json.toJson(false)
      }
      result.validate[Boolean].getOrElse(false)
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
  
}
