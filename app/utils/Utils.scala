package utils

import models.LoginModel

import scala.collection.mutable.ListBuffer

/**
  * Created by jyothi on 25/6/17.
  */
object Utils {

  def generateQueryParams(textId: Option[String], tagId: Option[String], version: Option[Int], limit: Option[Int], q: Option[String]): List[(String, String)] = {
    var returnList = new ListBuffer[(String, String)]
    if(textId.isDefined)
      returnList += ("textId" -> textId.get)
    if(tagId.isDefined)
      returnList += ("tag" -> tagId.get)
    if(version.isDefined)
      returnList += ("version" -> version.get.toString)
    if(limit.isDefined)
      returnList += ("limit" -> limit.get.toString)
    if(q.isDefined)
      returnList += ("q" -> q.get)
    returnList.toList
  }

  val title = "Vachin | Let's express..!"

  def isObjectInList(authStore: List[LoginModel], loginModel: LoginModel): Boolean = {
    for(model <- authStore){
      if(model.username.equals(loginModel.username) && model.password.equals(loginModel.password)){
        return true
      }
    }
    false
  }

  val keywords = "vachin, messages, texts, quotes, social, chat, feelings, inbox, online, online quotes, online messages"

  def getMetaKeywords(tag: String): String = {
    s"$keywords $tag messages, $tag tagged messages, $tag quotes, $tag texts, $tag sentences, $tag memos"
  }

  val description = "A message aggregator with much more to do.!"


}
