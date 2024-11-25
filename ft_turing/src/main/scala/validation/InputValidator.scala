// src/validation/InputValidator.scala

/**
 * Validates the input string against the machine's alphabet
 */
package validation

object InputValidator {
  def validate(input: String, alphabet: List[String]): Either[TuringError, String] = {
    val invalidChars = input.toSet.diff(alphabet.mkString.toSet)
    Either.cond(
      invalidChars.isEmpty,
      input,
      TuringError.InputError(s"Invalid characters in input: ${invalidChars.mkString(", ")}")
    )
  }
}