package repositories

import akka.http.scaladsl.marshallers.sprayjson.*
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import spray.json.DefaultJsonProtocol.*
import spray.json.*
import domain.*

import java.sql.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import spray.json._
  implicit val userFormat: RootJsonFormat[User] = jsonFormat5(User.apply)
  implicit val userListFormat: RootJsonFormat[List[User]] = listFormat(userFormat)
  implicit val userUpdateRequestFormat: RootJsonFormat[UserUpdateRequest] = jsonFormat5(UserUpdateRequest.apply)

  implicit val petitionFormat: RootJsonFormat[Petition] = jsonFormat8(Petition.apply)
  implicit val petitionListFormat: RootJsonFormat[List[Petition]] = listFormat(petitionFormat)
  implicit val petitionCreateRequestFormat: RootJsonFormat[PetitionCreateRequest] = jsonFormat8(PetitionCreateRequest.apply)
  implicit val petitionUpdateRequestFormat: RootJsonFormat[PetitionUpdateRequest] = jsonFormat8(PetitionUpdateRequest.apply)

  implicit val commentFormat:RootJsonFormat[Comment] = jsonFormat5(Comment.apply)
  implicit val commentListFormat:RootJsonFormat[List[Comment]]=listFormat(commentFormat)
  implicit val commentCreateRequestFormat: RootJsonFormat[CommentCreateRequest] = jsonFormat5(CommentCreateRequest.apply)
  implicit val commentUpdateRequestFormat: RootJsonFormat[CommentUpdateRequest] = jsonFormat5(CommentUpdateRequest.apply)
  
  implicit val petitionVotingFormat:RootJsonFormat[PetitionVoting] = jsonFormat4(PetitionVoting.apply)
  implicit val petitionVotingListFormat: RootJsonFormat[List[PetitionVoting]]=listFormat(petitionVotingFormat)
  implicit val petitionVotingCreateRequestFormat: RootJsonFormat[PetitionVotingCreateRequest] = jsonFormat4(PetitionVotingCreateRequest.apply)
  implicit val petitionVotingUpdateRequestFormat: RootJsonFormat[PetitionVotingUpdateRequest] = jsonFormat4(PetitionVotingUpdateRequest.apply)
  
}
