// src//main//scala//Main.scala

package turing


import scala.io.Source // allows to read files from the filesystem
import scala.util.{Try, Success, Failure} // allows to handle exceptions and recover from them
import play.api.libs.json._ // allows to parse JSON files
import Executor._ // Importa funciones de ejecución
import Types._   // Importa definiciones de tipos


/* 
    TuringMachineValidator object
    - object is a singleton class, similar to a class with only static methods
    - Contains the main method, which is the entry point of the program
    - Contains the showHelp method, which returns a help message
    - Contains the validateArgs method, which validates the arguments
    - Contains the processFile method, which reads the JSON file, validates the JSON content, validates the input string, and validates the transitions
    - Contains the readFile method, which reads a file
    - Contains the validateJson method, which validates the JSON content
    - Contains the validateInput method, which validates the input string
    - Contains the validateTransitions method, which validates the transitions
*/
object TuringMachineValidator { 

/* 
    ValidationError sealed trait
        - sealed trait is a trait that can only be extended in the same file
        - Contains the JsonError and InputError case classes
        - JsonError case class represents a JSON error
        - InputError case class represents an input error
    JsonError case class
        - Represents a JSON error
        - Contains a message
    InputError case class
        - Represents an input error
        - Contains a message
    TuringConfig case class
        - case class is a class with a lot of boilerplate code generated automatically
        - Represents the configuration of a Turing Machine
        - Contains the name, alphabet, blank, states, initial, finals, and transitions
*/
  sealed trait ValidationError

  case class JsonError(message: String) extends ValidationError
  case class InputError(message: String) extends ValidationError 
  

  // main method is the entry point of the program.
  def main(args: Array[String]): Unit = { // Unit is equivalent to void in Java
    if (args.contains("--help") || args.contains("-h")) { // contains checks if an element is in a list
      println(showHelp()) 
    } else {
      validateArgs(args.toList) match { // calls the validateArgs method with the arguments array converted to a list
        case Left(error) => println(s"⛔ Error: $error")  // if the result is a Left, print the error
        case Right((jsonPath, input)) => // if the result is a Right, destructure the tuple
          processFile(jsonPath, input) match { // calls the processFile method with the JSON path and input
            case Left(error) => println(s"⛔ Error: $error") // if the result is a Left, print the error
            case Right(config) => 
              println(s"🥳 Valid JSON file and input.\n🤖 Initializing Turing Machine\n\n")
              runMachine(config, input) // if the result is a Right, destructure the TuringConfig object and call the runMachine method
        }
      }
    }
  }

    // Show help message
  def showHelp(): String =
    """🚨 Usage: ft_turing [-h] jsonfile input
      |
      |positional arguments:
      | jsonfile    json description of the machine
      | input       input of the machine
      |
      |optional arguments:
      | -h, --help  show this help message and exit""".stripMargin // stripMargin removes leading whitespaces

/* 
    Validate arguments
    - Receives a list of strings. jsonPath and input are required
    - Returns a tuple with the JSON file path and input string if the arguments are valid
    - Otherwise, returns an error message
*/
  def validateArgs(args: List[String]): Either[String, (String, String)] = { 
        args match {
            case jsonPath :: input :: Nil if jsonPath.endsWith(".json") => Right((jsonPath, input)) // if the list has two elements and the first one ends with .json, return a Right with a tuple
            case _ => Left("Valid JSON file path and input are required.") // otherwise, return a Left with an error message
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
      jsonContent <- readFile(jsonPath) // calls the readFile method with the JSON path and stores the result in jsonContent which is a string
      config <- validateJson(jsonContent, jsonPath) // calls the validateJson method with the JSON content and JSON path and stores the result in config which is a TuringConfig object
      _ <- validateInput(input, config.alphabet) // calls the validateInput method with the input and alphabet 
      _ <- validateTransitions(config.transitions, config.alphabet, config.states) // calls the validateTransitions method with the transitions, alphabet, and states
    } yield config // if everything is valid, return the TuringConfig object. Otherwise, return an error message. Yield is similar to return in Scala and is used in for comprehensions. In this case, it returns the TuringConfig object
  }

/* 
    Read file
    - Receives a file path
    - Reads the file
    - Returns the file content as a string if successful
    - Otherwise, returns an error message
*/
  def readFile(path: String): Either[ValidationError, String] = {
    Try(Source.fromFile(path).mkString) match { // Try is used to handle exceptions. Source.fromFile reads a file and returns a BufferedSource object (a stream of characters). mkString reads the stream and returns a string. Match is used to handle the result of the Try
      case Success(content) => Right(content) // if the result is a Success, return the content as a Right
      case Failure(_) => Left(JsonError("File can´t be read")) // if the result is a Failure, return a Left with an error message
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
        for { // for is used to chain operations that return an Either
                // asOpt is used to convert a JsValue to an Option which means that if the JsValue is not a string,
                      // it will return None instead of throwing an exception.
                      // or Some is used to convert the Option to a Right.
                // toRight is used to convert an Option to an Either.
                // filterOrElse is used to filter the Option and return an error message if the condition is not met
                // fileName.split("/").last.stripSuffix(".json") is used to extract the name of the file without the path and the extension
          // name line is used to extract the value of the "name" key from the JSON object
          name <- (json \ "name").asOpt[String].toRight(JsonError("Field 'name' is missing or invalid")).filterOrElse(_ == fileName.split("/").last.stripSuffix(".json"), JsonError("Field 'name' must match the file name."))
                // forall is used to check if all elements of a list satisfy a condition
          // alphabet line is used to extract the value of the "alphabet" key from the JSON object and check if all elements are of length 1
          alphabet <- (json \ "alphabet").asOpt[List[String]].toRight(JsonError("Field 'alphabet' is missing or invalid.")).filterOrElse(_.forall(_.length == 1), JsonError("All elements in 'alphabet' must be of length 1."))
                // alphabet.contains is used to check if a list contains an element
          // blank line is used to extract the value of the "blank" key from the JSON object and check if it is in the alphabet
          blank <- (json \ "blank").asOpt[String].toRight(JsonError("Field 'blank' is missing or invalid.")).filterOrElse(alphabet.contains, JsonError("The blank symbol must be in 'alphabet'."))
          // states line is used to extract the value of the "states" key from the JSON object and check if all elements are strings and the initial state is in the states
          states <- (json \ "states").asOpt[List[String]].toRight(JsonError("Field 'states' is missing or invalid."))
          // initial line is used to extract the value of the "initial" key from the JSON object and check if it is in the states
          initial <- (json \ "initial").asOpt[String].toRight(JsonError("Field 'initial' is missing or invalid.")).filterOrElse(states.contains, JsonError("The initial state must be in 'states'."))
          // finals line is used to extract the value of the "finals" key from the JSON object and check if all elements are in the states
          finals <- (json \ "finals").asOpt[List[String]].toRight(JsonError("Field 'finals' is missing or invalid.")).filterOrElse(_.forall(states.contains), JsonError("All final states must be in 'states'."))
          // transitions line is used to extract the value of the "transitions" key from the JSON object and check if all keys are in the states
          transitions <- (json \ "transitions").asOpt[Map[String, List[Map[String, String]]]].toRight(JsonError("Field 'transitions' is missing or invalid.")).filterOrElse(_.keys.forall(states.contains), JsonError("All keys in 'transitions' must be in 'states'."))
        } yield TuringConfig(name, alphabet, blank, states, initial, finals, transitions) // if everything is valid, return a TuringConfig object
      case Failure(_) => Left(JsonError("JSON inválido.")) // if the result is a Failure, return a Left with an error message
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
        // toSet is used to convert a list to a set
        // diff is used to get the difference between two sets
        // mkString is used to convert a list to a string
    // invalidChars line is used to get the characters in the input that are not in the alphabet
    val invalidChars = input.toSet.diff(alphabet.mkString.toSet)
    // Either is used to return an error message if the invalidChars list is not empty
    // cond is used to check a condition and return a Left with an error message if the condition is not met
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
    transitions: Map[String, List[Map[String, String]]], // transitions is a map with a string as a key and a list of maps as a value
    alphabet: List[String], // alphabet is a list of strings
    states: List[String] // states is a list of strings
  ): Either[ValidationError, Unit] = { // Either is used to return an error message if the transitions are not valid or a Unit if they are valid
    val validActions = Set("LEFT", "RIGHT") // validActions is a set with two strings
    // flatMap is used to apply a function to all elements of a list and concatenate the results
    // the result is a list of errors for each state
    // case is used to destructure a tuple in a pattern match.
        // state is the key of the map and rules is the value
    val errors = transitions.flatMap { case (state, rules) =>
      // rules is a list of maps 
      rules.flatMap { rule => // flatMap is used to apply a function to all elements of a list and concatenate the results
            // filter
        // missingKeys line is used to get the keys that are missing in the rule
        val missingKeys = List("read", "to_state", "write", "action").filterNot(rule.contains)
        // get is used to get the value of a key from a map. exists(!alphabet.contains(_)) is used to check if the value is not in the alphabet
        val invalidRead = rule.get("read").exists(!alphabet.contains(_))
        val invalidWrite = rule.get("write").exists(!alphabet.contains(_))
        val invalidState = rule.get("to_state").exists(!states.contains(_))
        val invalidAction = rule.get("action").exists(!validActions.contains(_))
        // List is used to create a list of errors
        val errors =
          missingKeys.map(key => s"Missing key '$key' in state '$state'.") ++
          // if is used to check if the value is true and return a list of errors
          (if (invalidRead) List(s"Field 'read' contains an invalid symbol in state '$state'.") else Nil) ++
          (if (invalidWrite) List(s"Field 'write' contains an invalid symbol in state '$state'.") else Nil) ++
          (if (invalidState) List(s"Field 'to_state' contains an invalid state in state '$state'.") else Nil) ++
          (if (invalidAction) List(s"Field 'action' contains an invalid action in state '$state'.") else Nil)

        errors // return the list of errors
      }
    }
    // Either is used to return an error message if the errors list is not empty
    // cond is used to check a condition and return a Left with an error message if the condition is not met
    Either.cond(errors.isEmpty, (), JsonError(errors.mkString("\n")))
  }

	// Ejecutar la máquina de Turing
  /* def runMachine(config: TuringConfig, input: String): Unit = {
    // Convertimos cada carácter del input a String
    val initialTape = input.map(_.toString).toVector ++ Vector.fill(20)(config.blank)
    val initialState = TuringMachineState(initialTape, head = 0, state = config.initial)

    println(renderMachineHeader(config))
    stepMachine(config, initialState)
  } */
}


