package models

import play.api.libs.json.{Json, OFormat}

case class BlogWithCommentsResponse(
                             blog: Blog,
                             comments: Seq[Comments]
                           )

object BlogWithCommentsResponse {
  implicit val format: OFormat[BlogWithCommentsResponse] = Json.format[BlogWithCommentsResponse]
}

