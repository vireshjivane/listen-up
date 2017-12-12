package services

import com.fasterxml.jackson.databind.JsonNode
import scala.concurrent.Future

trait DataService {

  /*
    Implementation of this method would fetch all the available resources
   */
  def retrieveAll(): Future[(Int, Option[JsonNode])]

  /*
  Implementation of this method would fetch single resource
 */
  def retrieveSingle(username: String): Future[(Int, Option[JsonNode])]
}
