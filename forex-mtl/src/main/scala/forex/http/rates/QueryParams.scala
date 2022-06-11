package forex.http.rates

import org.http4s.dsl.impl.QueryParamDecoderMatcher

object QueryParams {
  object FromQueryParam extends QueryParamDecoderMatcher[String]("from")
  object ToQueryParam extends QueryParamDecoderMatcher[String]("to")

}
