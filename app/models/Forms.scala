package models

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by jyothi on 10/8/17.
  */
object Forms {

  val textRequestForm = Form(
    mapping(
      "text" -> nonEmptyText,
      "tags" -> list(text),
      "by" -> optional(text),
      "user" -> optional(text)
    )(TextRequestModel.apply)(TextRequestModel.unapply)
  )

}
