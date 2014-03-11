import play.api.mvc.WithFilters

object Global extends WithFilters(HttpsRedirectFilter)
