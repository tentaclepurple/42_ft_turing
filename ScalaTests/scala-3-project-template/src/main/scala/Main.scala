import scala.io.Source // allows to read files from the filesystem
import scala.util.{Try, Success, Failure} // allows to handle exceptions and recover from them
import play.api.libs.json._ // allows to parse JSON files


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
  case class TuringConfig(
    name: String,
    alphabet: List[String],
    blank: String,
    states: List[String],
    initial: String,
    finals: List[String],
    transitions: Map[String, List[Map[String, String]]]
  )

  // main method is the entry point of the program
  def main(args: Array[String]): Unit = { // Unit is equivalent to void in Java
    if (args.contains("--help") || args.contains("-h")) { // contains checks if an element is in a list
      println(showHelp()) 
    } else {
      validateArgs(args.toList) match { // calls the validateArgs method with the arguments array converted to a list
        case Left(error) => println(s"Error: $error")  // if the result is a Left, print the error
        case Right((jsonPath, input)) => // if the result is a Right, destructure the tuple
          processFile(jsonPath, input) match { // calls the processFile method with the JSON path and input
            case Left(error) => println(s"Error: $error") // if the result is a Left, print the error
            case Right(config) => println("Valid JSON file and input. Initializing Turing Machine") // if the result is a Right, print a success message
        }
      }
    }
  }

    // Show help message
  def showHelp(): String =
    """usage: ft_turing [-h] jsonfile input
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

  // Validar JSON
  def validateJson(content: String, fileName: String): Either[ValidationError, TuringConfig] = {
    Try(Json.parse(content)) match {
      case Success(json) =>
        for {
          name <- (json \ "name").asOpt[String].toRight(JsonError("Campo 'name' faltante o inválido.")).filterOrElse(_ == fileName.split("/").last.stripSuffix(".json"), JsonError("El campo 'name' debe coincidir con el nombre del archivo."))
          alphabet <- (json \ "alphabet").asOpt[List[String]].toRight(JsonError("Campo 'alphabet' faltante o inválido.")).filterOrElse(_.forall(_.length == 1), JsonError("Todos los símbolos en 'alphabet' deben ser de longitud 1."))
          blank <- (json \ "blank").asOpt[String].toRight(JsonError("Campo 'blank' faltante o inválido.")).filterOrElse(alphabet.contains, JsonError("El campo 'blank' debe estar en 'alphabet'."))
          states <- (json \ "states").asOpt[List[String]].toRight(JsonError("Campo 'states' faltante o inválido."))
          initial <- (json \ "initial").asOpt[String].toRight(JsonError("Campo 'initial' faltante o inválido.")).filterOrElse(states.contains, JsonError("El estado inicial debe estar en 'states'."))
          finals <- (json \ "finals").asOpt[List[String]].toRight(JsonError("Campo 'finals' faltante o inválido.")).filterOrElse(_.forall(states.contains), JsonError("Todos los estados finales deben estar en 'states'."))
          transitions <- (json \ "transitions").asOpt[Map[String, List[Map[String, String]]]].toRight(JsonError("Campo 'transitions' faltante o inválido.")).filterOrElse(_.keys.forall(states.contains), JsonError("Las claves de 'transitions' deben ser estados válidos."))
        } yield TuringConfig(name, alphabet, blank, states, initial, finals, transitions)
      case Failure(_) => Left(JsonError("JSON inválido."))
    }
  }

  // Validar entrada
  def validateInput(input: String, alphabet: List[String]): Either[ValidationError, Unit] = {
    val invalidChars = input.toSet.diff(alphabet.mkString.toSet)
    Either.cond(invalidChars.isEmpty, (), InputError(s"Caracteres inválidos en la entrada: ${invalidChars.mkString(", ")}"))
  }

  // Validar transiciones
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
          missingKeys.map(key => s"Falta la clave '$key' en una transición de estado '$state'.") ++
          (if (invalidRead) List(s"El campo 'read' contiene un símbolo no válido en estado '$state'.") else Nil) ++
          (if (invalidWrite) List(s"El campo 'write' contiene un símbolo no válido en estado '$state'.") else Nil) ++
          (if (invalidState) List(s"El campo 'to_state' apunta a un estado no válido desde '$state'.") else Nil) ++
          (if (invalidAction) List(s"El campo 'action' contiene un valor no válido en estado '$state'.") else Nil)

        errors
      }
    }

    Either.cond(errors.isEmpty, (), JsonError(errors.mkString("\n")))
  }
}
