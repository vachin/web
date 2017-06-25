import controllers.Application
import org.slf4j.LoggerFactory
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator}
import play.api.ApplicationLoader.Context
import play.api._
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

class ApiComponents(context: Context) extends BuiltInComponentsFromContext(context)  with AhcWSComponents {

  val serverHost = configuration.getString("play.server.host").get

  lazy val logger = LoggerFactory.getLogger("VachinLogger")

  lazy val dataService = new DataService(wsClient, serverHost, logger)

  lazy val applicationController = new Application(dataService, logger)

  lazy val assets = new controllers.Assets(httpErrorHandler)

  lazy val router = new Routes(httpErrorHandler, applicationController, assets)

}
