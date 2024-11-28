// src/validation/JsonValidator.scala

package validation

import play.api.libs.json._
import cats.implicits._

object JsonValidator {
  def validate(jsonContent: String): Either[TuringError, TuringConfig] = {
    import TuringError._
    
    def parseJson(content: String): Either[TuringError, JsValue] =
      Json.parse(content).asOpt[JsValue]
        .toRight(JsonParseError("Invalid JSON format"))

    def validateStructure(json: JsValue): Either[TuringError, JsValue] = {
      val mandatoryFields = Set("name", "alphabet", "blank", "states", "initial", "finals", "transitions")
      val jsonFields = json.as[JsObject].keys.toSet
      
      Either.cond(
        mandatoryFields.subsetOf(jsonFields),
        json,
        ConfigError(s"Missing mandatory fields: ${mandatoryFields.diff(jsonFields).mkString(", ")}")
      )
    }

    def validateAlphabetSymbols(alphabet: List[String]): Either[TuringError, Unit] = {
        val invalidSymbols = alphabet.filter(_.length != 1)
        Either.cond(
        invalidSymbols.isEmpty,
        (),
        ConfigError(s"Invalid alphabet symbols (must be single characters): ${invalidSymbols.mkString(", ")}")
        )
    }

    def validateField[T](json: JsValue, field: String)(implicit reads: Reads[T]): Either[TuringError, T] =
      (json \ field).validate[T].asOpt
        .toRight(ConfigError(s"Invalid or missing field: $field"))

    def validateTransitions(
      transitions: Map[String, List[JsObject]], 
      states: List[String],
      alphabet: List[String]
    ): Either[TuringError, Map[String, List[Transition]]] = {
      
      def validateTransition(obj: JsObject): Either[TuringError, Transition] = {
        for {
          read <- (obj \ "read").validate[String].asOpt.toRight(ConfigError("Invalid read"))
          toState <- (obj \ "to_state").validate[String].asOpt.toRight(ConfigError("Invalid to_state"))
          write <- (obj \ "write").validate[String].asOpt.toRight(ConfigError("Invalid write"))
          action <- (obj \ "action").validate[String].asOpt.toRight(ConfigError("Invalid action"))
          _ <- validateTransitionValues(read, toState, write, action, states, alphabet)
        } yield Transition(read, toState, write, action)
      }

      def validateTransitionValues(
        read: String,
        toState: String,
        write: String,
        action: String,
        states: List[String],
        alphabet: List[String]
      ): Either[TuringError, Unit] = {
        for {
          _ <- Either.cond(alphabet.contains(read), (), ConfigError(s"Read symbol '$read' not in alphabet"))
          _ <- Either.cond(states.contains(toState), (), ConfigError(s"State '$toState' not in states"))
          _ <- Either.cond(alphabet.contains(write), (), ConfigError(s"Write symbol '$write' not in alphabet"))
          _ <- Either.cond(Set("LEFT", "RIGHT").contains(action), (), ConfigError(s"Invalid action: $action"))
        } yield ()
      }

      transitions.toList.traverse { case (state, trans) =>
        for {
          _ <- Either.cond(states.contains(state), (), ConfigError(s"Invalid state: $state"))
          validTrans <- trans.traverse(validateTransition)
        } yield state -> validTrans
      }.map(_.toMap)
    }

    for {
      json <- parseJson(jsonContent)
      _ <- validateStructure(json)
      name <- validateField[String](json, "name")
      alphabet <- validateField[List[String]](json, "alphabet")
      _ <- validateAlphabetSymbols(alphabet)
      blank <- validateField[String](json, "blank")
      states <- validateField[List[String]](json, "states")
      initial <- validateField[String](json, "initial")
      finals <- validateField[List[String]](json, "finals")
      transitions <- validateField[Map[String, List[JsObject]]](json, "transitions")
      _ <- Either.cond(alphabet.contains(blank), (), ConfigError("Blank not in alphabet"))
      _ <- Either.cond(states.contains(initial), (), ConfigError("Initial state not in states"))
      _ <- Either.cond(finals.forall(states.contains), (), ConfigError("Some final states not in states"))
      validTransitions <- validateTransitions(transitions, states, alphabet)
    } yield TuringConfig(name, alphabet, blank, states, initial, finals, validTransitions)
  }
}