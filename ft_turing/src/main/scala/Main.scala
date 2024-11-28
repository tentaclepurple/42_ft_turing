// src/main/scala/Main.scala

import scala.io.Source
import validation.{TuringError, JsonValidator, InputValidator}

object Main {
  private def processArgs(args: List[String]): Either[TuringError, (String, String)] = args match
  {
    case "-h" :: _ | "--help" :: _ => Left(TuringError.ConfigError("help"))
    case file :: input :: Nil => Right((file, input))
    case _ => Left(TuringError.ConfigError("Invalid number of arguments"))
  }

  private def readFile(path: String): Either[TuringError, String] =
    try Right(Source.fromFile(path).mkString)
    catch
    {
      case e: Exception => 
        Left(TuringError.ConfigError(s"Error reading file: ${e.getMessage}"))
    }

  private def showHelp(): String = 
    """usage: ft_turing [-h] jsonfile input
      |
      |positional arguments:
      | jsonfile    json description of the machine
      | input      input of the machine
      |
      |optional arguments:
      | -h, --help show this help message and exit""".stripMargin

  @main def ft_turing(args: String*): Unit =
  {
    val result = processArgs(args.toList).flatMap { case (jsonFile, input) =>
      readFile(jsonFile).flatMap { jsonContent =>
        JsonValidator.validate(jsonContent).flatMap { config =>
          InputValidator.validate(input, config.alphabet).map { validInput =>
            (config, validInput)
          }
        }
      }
    }

    result match
    {
      case Left(TuringError.ConfigError("help")) => 
        println(showHelp())
      case Left(error) => 
        System.err.println(s"Error: ${error.toString}")
      case Right((config, input)) =>
        println("Configuration and input valid!")
        // TODO: Implement Turing machine logic
    }
  }
}

