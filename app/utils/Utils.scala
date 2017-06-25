package utils

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


}
