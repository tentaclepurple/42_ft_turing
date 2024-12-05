// src//main//scala//Main.scala

package turing


import scala.io.Source
import scala.util.{Try, Success, Failure}
import play.api.libs.json._
import Executor._
import Types._
import turing.Art._


object TuringMachineValidator { 

  sealed trait ValidationError

  case class JsonError(message: String) extends ValidationError
  case class InputError(message: String) extends ValidationError 
  

  def main(args: Array[String]): Unit = {
    if (args.contains("--help") || args.contains("-h")) {
      println(showHelp()) 
    } else {
      validateArgs(args.toList) match { 
        case Left(error) => println(s"⛔ Error: $error")
        case Right((jsonPath, input)) =>
          processFile(jsonPath, input) match {
            case Left(error) => println(s"⛔ Error: $error")
            case Right(config) => 
              println(s"\n\n🥳 Valid JSON file and input.\n🤖 Initializing Turing Machine\n\n")
              Thread.sleep(1000)
              println(s"\n${sirAlan}\n")
              Thread.sleep(1000)
              runMachine(config, input) 
        }
      }
    }
  }


  def showHelp(): String =
    """🚨 Usage: ft_turing [-h] jsonfile input
      |
      |positional arguments:
      | jsonfile    json description of the machine
      | input       input of the machine
      |
      |optional arguments:
      | -h, --help  show this help message and exit""".stripMargin 

/* 
    Validate arguments
    - Receives a list of strings. jsonPath and input are required
    - Returns a tuple with the JSON file path and input string if the arguments are valid
    - Otherwise, returns an error message
*/
  def validateArgs(args: List[String]): Either[String, (String, String)] = { 
        args match {
            case jsonPath :: input :: Nil if jsonPath.endsWith(".json") => Right((jsonPath, input))
            case _ => Left("Valid JSON file path and input are required.")
        }
  }

/* 
    Process file
    - Receives a JSON file path and an input string
    - Reads the JSON file
    - Validates the JSON content
    - Validates the input string
    - Validates the transitions
    - Returns a TuringConfig object if everything is valid
    - Otherwise, returns an error message
*/
  def processFile(jsonPath: String, input: String): Either[ValidationError, TuringConfig] = {
    for {
      jsonContent <- readFile(jsonPath) 
      config <- validateJson(jsonContent, jsonPath)
      _ <- validateInput(input, config.alphabet)
      _ <- validateTransitions(config.transitions, config.alphabet, config.states)
    } yield config
  }

/* 
    Read file
    - Receives a file path
    - Reads the file
    - Returns the file content as a string if successful
    - Otherwise, returns an error message
*/
  def readFile(path: String): Either[ValidationError, String] = {
    Try(Source.fromFile(path).mkString) match {
      case Success(content) => Right(content)
      case Failure(_) => Left(JsonError("File can´t be read"))
    }
  }

/*
    Validate JSON
    - Receives a JSON content and a file name
    - Validates the JSON content
    - Returns a TuringConfig object if the JSON content is valid
    - Otherwise, returns an error message
*/
  def validateJson(content: String, fileName: String): Either[ValidationError, TuringConfig] = {
    Try(Json.parse(content)) match { // Try is used to handle exceptions. Json.parse parses a JSON string and returns a JsValue object. Match is used to handle the result of the Try
      case Success(json) => // if the result is a Success
        for {
          name <- (json \ "name").asOpt[String].toRight(JsonError("Field 'name' is missing or invalid")).filterOrElse(_ == fileName.split("/").last.stripSuffix(".json"), JsonError("Field 'name' must match the file name."))
          alphabet <- (json \ "alphabet").asOpt[List[String]].toRight(JsonError("Field 'alphabet' is missing or invalid.")).filterOrElse(_.forall(_.length == 1), JsonError("All elements in 'alphabet' must be of length 1."))
          blank <- (json \ "blank").asOpt[String].toRight(JsonError("Field 'blank' is missing or invalid.")).filterOrElse(alphabet.contains, JsonError("The blank symbol must be in 'alphabet'."))
          states <- (json \ "states").asOpt[List[String]].toRight(JsonError("Field 'states' is missing or invalid."))
          initial <- (json \ "initial").asOpt[String].toRight(JsonError("Field 'initial' is missing or invalid.")).filterOrElse(states.contains, JsonError("The initial state must be in 'states'."))
          finals <- (json \ "finals").asOpt[List[String]].toRight(JsonError("Field 'finals' is missing or invalid.")).filterOrElse(_.forall(states.contains), JsonError("All final states must be in 'states'."))
          transitions <- (json \ "transitions").asOpt[Map[String, List[Map[String, String]]]].toRight(JsonError("Field 'transitions' is missing or invalid.")).filterOrElse(_.keys.forall(states.contains), JsonError("All keys in 'transitions' must be in 'states'."))
        } yield TuringConfig(name, alphabet, blank, states, initial, finals, transitions)
      case Failure(_) => Left(JsonError("JSON inválido."))
    }
  }

/* 
    Validate input
    - Receives an input string and an alphabet
    - Validates the input string
    - Returns a Unit if the input string is valid
    - Otherwise, returns an error message
*/
  def validateInput(input: String, alphabet: List[String]): Either[ValidationError, Unit] = {
    val invalidChars = input.toSet.diff(alphabet.mkString.toSet)
    Either.cond(invalidChars.isEmpty, (), InputError(s"Invalid characters in input: ${invalidChars.mkString(", ")}"))
  }

/* 
    Validate transitions
    - Receives transitions, an alphabet, and states
    - Validates the transitions
    - Returns a Unit if the transitions are valid
    - Otherwise, returns an error message
*/
  def validateTransitions(
    transitions: Map[String, List[Map[String, String]]],
    alphabet: List[String],
    states: List[String]
  ): Either[ValidationError, Unit] = {
    val validActions = Set("LEFT", "RIGHT")

    val errors = transitions.flatMap { case (state, rules) =>
      rules.flatMap { rule =>
        val missingKeys = List("read", "to_state", "write", "action").filterNot(rule.contains)
        val invalidRead = rule.get("read").exists(!alphabet.contains(_))
        val invalidWrite = rule.get("write").exists(!alphabet.contains(_))
        val invalidState = rule.get("to_state").exists(!states.contains(_))
        val invalidAction = rule.get("action").exists(!validActions.contains(_))
        val errors =
          missingKeys.map(key => s"Missing key '$key' in state '$state'.") ++
          (if (invalidRead) List(s"Field 'read' contains an invalid symbol in state '$state'.") else Nil) ++
          (if (invalidWrite) List(s"Field 'write' contains an invalid symbol in state '$state'.") else Nil) ++
          (if (invalidState) List(s"Field 'to_state' contains an invalid state in state '$state'.") else Nil) ++
          (if (invalidAction) List(s"Field 'action' contains an invalid action in state '$state'.") else Nil)

        errors
      }
    }

    Either.cond(errors.isEmpty, (), JsonError(errors.mkString("\n")))
  }
}


