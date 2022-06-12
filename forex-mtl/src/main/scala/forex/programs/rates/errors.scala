package forex.programs.rates

import forex.services.rates.errors.{Error => RatesServiceError}

object errors {

  sealed abstract class Error(msg: String) extends Exception(msg)
  object Error {
    final case class IllegalArgument(msg: String) extends Error(msg)
    final case class RateLookupFailed(msg: String) extends Error(msg)
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.OneFrameLookupFailed(msg) => Error.RateLookupFailed(msg)
  }
}
