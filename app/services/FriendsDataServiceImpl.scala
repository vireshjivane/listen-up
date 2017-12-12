package services

import javax.inject.Inject
import com.fasterxml.jackson.databind.JsonNode
import play.api.Configuration
import play.api.libs.ws.WSResponse
import play.libs.Json
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FriendsDataServiceImpl @Inject()(rc: RestClient, config: Configuration) extends DataService {

  override def retrieveAll(): Future[(Int, Option[JsonNode])] = {
    val response: Future[WSResponse] = rc.fetch(config.get[String]("friends.endpoint"))
    response.map { jsResponse => (jsResponse.status, Some(Json.parse(jsResponse.body))) }
  }

  override def retrieveSingle(username: String): Future[(Int, Option[JsonNode])] = {
    val response: Future[WSResponse] = rc.fetch(config.get[String]("friends.endpoint") + "/" + username)
    response.map { jsResponse => (jsResponse.status, Some(Json.parse(jsResponse.body))) }
  }
}

