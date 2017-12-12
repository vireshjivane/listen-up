package services

import javax.inject.Inject
import play.api.libs.ws._
import scala.concurrent.Future

class RestClient @Inject() (ws: WSClient) {

  def fetch(uri: String): Future[WSResponse] = ws.url(uri).get()

}
