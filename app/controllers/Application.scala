package controllers

import play.api._
import org.slf4j.Logger
import play.api.mvc._
import services.DataService

class Application(dataService: DataService, logger: Logger) extends Controller with {

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
        Ok(views.html.tagged(tagsWithCount, texts, tag))
      }
    }
  }

  def getText(textId: String) = Action.async {
    val futureRelatedTexts = dataService.searchTexts(textId.replaceAll("-", " "))
    val futureTagsWithCount = dataService.getTagsWithCount(None, None)
    val futureTexts = dataService.getText(textId)
    futureRelatedTexts.flatMap{ relatedTexts =>
      futureTagsWithCount.flatMap{ tagsWithCount =>
        futureTexts.map{ texts =>
          Ok(views.html.text(tagsWithCount, text, relatedTexts, None))
        }
      }
    }
  }

}