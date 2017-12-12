package services

import javax.inject.Inject

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import domain.User
import play.api.Configuration

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.mvc.Http.Status._

class UserDataServiceImpl @Inject()(playsService: PlaysDataServiceImpl, friendsService: FriendsDataServiceImpl, config: Configuration) {

  /*
  This function combines the results of for all the users received as response from single user combiner.
  */
  def allUserCombinerFunction(playsResponse: (Int, Option[JsonNode]), friendsResponse: (Int, Option[JsonNode])): Future[Array[Future[User]]] = Future {
    val usernames = scala.collection.mutable.SortedSet[String]()
    val listOfUserObjects = new ListBuffer[Future[User]]()

    if (playsResponse._1 == OK ) {
      val array = playsResponse._2.get.get("users").asInstanceOf[ArrayNode]
      import scala.collection.JavaConverters._
      array.forEach(node => usernames += node.get("username").asText())
    }
    if (friendsResponse._1 == OK) {
      val array = friendsResponse._2.get.get("friends").asInstanceOf[ArrayNode]
      import scala.collection.JavaConverters._
      array.forEach(node => usernames += node.get("username").asText())
    }

    usernames.foreach({ user => listOfUserObjects.append(retrieveSingle(user)) })
    listOfUserObjects.toArray
  }

  /*
    This function combines the results of each response received for the User
  */
  def singleUserCombinerFunction(username: String, playsResponse: (Int, Option[JsonNode]), friendsResponse: (Int, Option[JsonNode])): Future[User] = Future {

    var (play, friends, tracks) = (0, 0, 0)

    if (playsResponse._1 == OK) {
      val array = playsResponse._2.get.get("plays").asInstanceOf[ArrayNode]
      play = array.size()
      import scala.collection.JavaConverters._
      tracks = array.elements().asScala.toList.distinct.length
    }

    if (friendsResponse._1 == OK) {
      val array = friendsResponse._2.get.get("friends").asInstanceOf[ArrayNode]
      friends = array.size()
    }

    User(username, play, friends, tracks, s"${config.get[String]("users.uri")}/$username")
  }

  /*
    Function to retrieve and combine results from plays and friends APIs for single user
  */
  def retrieveSingle(username: String): Future[User] = {
    val result = for {
      playsResponse <- playsService.retrieveSingle(username)
      friendsResponse <- friendsService.retrieveSingle(username)
      result <- singleUserCombinerFunction(username, playsResponse, friendsResponse)
    } yield result
    result
  }

  /*
  Function to retrieve and combine results from plays and friends APIs for all the users
  */
  def retrieveAll(): Future[Seq[User]] = {
    val result = for {
      playsResponse <- playsService.retrieveAll()
      friendsResponse <- friendsService.retrieveAll()
      uniqueUsers <- allUserCombinerFunction(playsResponse, friendsResponse)
      result <- Future.sequence(uniqueUsers.toSeq)
    } yield result
    result
  }
}