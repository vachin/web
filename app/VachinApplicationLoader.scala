import controllers.Application
import models.LoginModel
import org.slf4j.LoggerFactory
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator}
import play.api.ApplicationLoader.Context
import play.api._
import play.api.i18n.I18nComponents
import play.api.libs.ws.ahc.AhcWSComponents
import router.Routes
import services.DataService

import scala.concurrent.ExecutionContext.Implicits.global._
/**
  * Created by jyothi on 25/6/17.
  */

class VachinApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach{
      _.configure(context.environment)
    }
    new ApiComponents(context).application
  }
}

class ApiComponents(context: Context) extends BuiltInComponentsFromContext(context)  with AhcWSComponents with I18nComponents {

  val serverHost = configuration.getString("play.server.host").get

  lazy val username = configuration.getString("play.auth.username").get
  lazy val password = configuration.getString("play.auth.password").get

  val authList = configuration.getStringList("play.auth").get
  val authStore = authList.toArray.map(_.toString.split(":")).map(x => LoginModel(x.head, x.last)).toList

  lazy val logger = LoggerFactory.getLogger("VachinLogger")

  lazy val dataService = new DataService(wsClient, serverHost, logger)

  lazy val applicationController = new Application(dataService, logger, messagesApi, authStore)

  lazy val assets = new controllers.Assets(httpErrorHandler)

  lazy val router = new Routes(httpErrorHandler, applicationController, assets)

}
