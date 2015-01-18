package repository

import models.User
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.modules.reactivemongo.json.BSONFormats
import reactivemongo.bson.BSONObjectID
import securesocial.core.{AuthenticationMethod, PasswordInfo, OAuth2Info, OAuth1Info}

object IdentifiedProfile {
  private implicit val bsonObjectIdFormat = BSONFormats.BSONObjectIDFormat
  private implicit val dateTimeFormat = Formats.dateTimeFormat
  private implicit val oAuth1Format = Json.format[OAuth1Info]
  private implicit val oAuth2Format = Json.format[OAuth2Info]
  private implicit val passwordInfoFormat = Json.format[PasswordInfo]
  private implicit val authMethodFormat = Json.format[AuthenticationMethod]

  implicit val writes = Json.writes[IdentifiedProfile]

  implicit val reads = (
    (__ \ "_id").read[BSONObjectID] and
      (__ \ "providerId").read[String] and
      (__ \ "userId").read[String] and
      (__ \ "firstName").readNullable[String] and
      (__ \ "lastName").readNullable[String] and
      (__ \ "fullName").readNullable[String] and
      (__ \ "email").readNullable[String] and
      (__ \ "avatarUrl").readNullable[String] and
      (__ \ "authMethod").read[AuthenticationMethod] and
      (__ \ "oAuth1Info").readNullable[OAuth1Info] and
      (__ \ "oAuth2Info").readNullable[OAuth2Info] and
      (__ \ "passwordInfo").readNullable[PasswordInfo] and
      (__ \ "authenticators").readNullable[Seq[AuthenticatorDetails]].map(_.getOrElse(Seq.empty))
    )(IdentifiedProfile.apply _)
}

case class IdentifiedProfile(_id: BSONObjectID,
                             providerId: String,
                             userId: String,
                             firstName: Option[String],
                             lastName: Option[String],
                             fullName: Option[String],
                             email: Option[String],
                             avatarUrl: Option[String],
                             authMethod: AuthenticationMethod,
                             oAuth1Info: Option[OAuth1Info] = None,
                             oAuth2Info: Option[OAuth2Info] = None,
                             passwordInfo: Option[PasswordInfo] = None,
                             authenticators: Seq[AuthenticatorDetails]) {

  def toUser = User(_id, userId, firstName)
}
