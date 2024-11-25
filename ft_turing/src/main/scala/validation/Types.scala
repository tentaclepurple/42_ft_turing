// src/validation/Types.scala

package validation

import play.api.libs.json._

/** Custom error types */
sealed trait TuringError
object TuringError {
  case class JsonParseError(message: String) extends TuringError
  case class ConfigError(message: String) extends TuringError
  case class InputError(message: String) extends TuringError
}

/** Configuration case classes */
case class Transition(
  read: String,
  toState: String,
  write: String,
  action: String
)

case class TuringConfig(
  name: String,
  alphabet: List[String],
  blank: String,
  states: List[String],
  initial: String,
  finals: List[String],
  transitions: Map[String, List[Transition]]
)