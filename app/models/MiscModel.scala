package models

import play.api.libs.json.Json

/**
  * Created by jyothi on 29/7/17.
  */
object MiscModel {

}

case class PaginationModel(limit: Int, version: Int, total: Int)

object PaginationModel {
  implicit val paginationModelFormat = Json.format[PaginationModel]

  def empty = PaginationModel(10, 1, 0)

}

case class TextPaginatedModel(meta: PaginationModel, texts: List[TextModel])

object TextPaginatedModel {
  implicit val textPaginatedModelFormat = Json.format[TextPaginatedModel]

  def empty = TextPaginatedModel(PaginationModel(10, 1, 0), List.empty[TextModel])

  def emptyWithList(textModels: List[TextModel]) = TextPaginatedModel(PaginationModel(10, 1, 0), textModels)

}
