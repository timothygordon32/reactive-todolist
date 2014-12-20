package security

import play.Play
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

object HttpsRedirectFilter extends Filter {

  lazy val requiredHttpsPort =
    Play.application().configuration().getString("https.redirectPort",
      Play.application().configuration().getString("https.port"))

  lazy val httpsPortSuffix = {
    val port = requiredHttpsPort
    if (port == null) "" else ":" + port
  }

  lazy val httpsRequired =
    Play.isProd || requiredHttpsPort != null

  val hostnameMatcher = ".*(?=:[0-9]*)".r
  val portMatcher = "(?<=:)[0-9]*".r

  def apply(nextFilter: (RequestHeader) => Future[Result])
           (requestHeader: RequestHeader): Future[Result] =
    if (httpsRequired && !isHttps(requestHeader)) redirectToHttps(requestHeader)
    else nextFilter(requestHeader)

  private def isHttps(requestHeader: RequestHeader): Boolean =
    httpsForwarded(requestHeader) || httpsPortSpecified(requestHeader)

  private def httpsForwarded(requestHeader: RequestHeader) =
    Play.isProd && requestHeader.headers.get("X-Forwarded-Proto").getOrElse("").contains("https")

  private def httpsPortSpecified(requestHeader: RequestHeader) =
    portMatcher.findFirstIn(requestHeader.host) == Some(requiredHttpsPort)

  private def redirectToHttps(requestHeader: RequestHeader): Future[Result] = {
    val hostname = hostnameMatcher.findFirstIn(requestHeader.host).getOrElse(requestHeader.host)
    val url = s"https://$hostname$httpsPortSuffix${requestHeader.uri}"
    Future.successful(redirect(url))
  }

  private def redirect(url: String) = if (Play.isProd) MovedPermanently(url) else Redirect(url)
}
