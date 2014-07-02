package models

import reactivemongo.bson._
import play.api.libs.json._
import scala.util.Try
import play.modules.reactivemongo.json.BSONFormats.PartialFormat

object BSONFormats {

  implicit object BSONObjectIDFormat extends PartialFormat[BSONObjectID] {
    def partialReads: PartialFunction[JsValue, JsResult[BSONObjectID]] = {
      case JsString(x) => {
        val maybeOID: Try[BSONObjectID] = BSONObjectID.parse(x)
        if(maybeOID.isSuccess) JsSuccess(maybeOID.get) else {
          JsError("Expected BSONObjectID as JsString")
        }
      }
    }
    val partialWrites: PartialFunction[BSONValue, JsValue] = {
      case oid: BSONObjectID => JsString(oid.stringify)
    }
  }

}
