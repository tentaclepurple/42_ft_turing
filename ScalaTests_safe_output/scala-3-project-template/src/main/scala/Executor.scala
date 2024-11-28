package turing

import turing.Types._
import turing.Art._

object Executor {

  /**
   * Renders the header of the Turing Machine with its configuration details.
   * @param config Turing machine configuration containing alphabet, states, initial state, and final states.
   * @return A formatted string with the header of the machine.
   */
  def renderMachineHeader(config: TuringConfig): String = {
    s"""
	
	${sirAlan.stripMargin}                                                                               
 
    |********************************************************************************
    |*                                                                              *
    |* ${config.name}                                                               *
    |*                                                                              *
    |********************************************************************************
    |Alphabet: [ ${config.alphabet.mkString(", ")} ]
    |States   : [ ${config.states.mkString(", ")} ]
    |Initial  : ${config.initial}
    |Finals   : [ ${config.finals.mkString(", ")} ]
    |********************************************************************************
    """.stripMargin
  }

  /**
   * Renders the tape of the Turing Machine, marking the position of the head.
   * @param tape The tape represented as a vector of symbols.
   * @param head The position of the head on the tape.
   * @return A formatted string representing the tape.
   */
  def renderTape(tape: Vector[String], head: Int): String = {
    tape.zipWithIndex.map {
      case (symbol, index) if index == head => s"<${symbol}>" // Highlight the symbol under the head.
      case (symbol, _) => symbol
    }.mkString("[", "", "]")
  }

  /**
   * Executes one step of the Turing Machine, updating its state and tape based on the transitions.
   * This function is tail-recursive to ensure it can handle large inputs efficiently.
   * 
   * @param config The Turing Machine configuration containing states, transitions, etc.
   * @param state The current state of the Turing Machine, including tape and head position.
   * @return An optional string indicating the final status or error message.
   */
  @scala.annotation.tailrec
  def stepMachine(config: TuringConfig, state: TuringMachineState): Option[String] = {
    // Print the current tape and head position.
    println(s"📜 Tape: ${renderTape(state.tape, state.head)}")

    // Check if the current state is a final state.
    if (config.finals.contains(state.state)) {
      val msg = s"🎉 Machine halted in final state: ${state.state}"
      println(msg)
      Some(msg)
    } 
    // Check if the head is out of bounds.
    else if (state.head < 0 || state.head >= state.tape.length) {
      val error = s"⚠️ Error: Head is out of tape bounds at position ${state.head}"
      println(error)
      Some(error)
    } 
    // Process the current state and symbol under the head.
    else {
      config.transitions.get(state.state) match {
        case Some(rules) =>
          val currentSymbol = state.tape(state.head)
          rules.find(rule => rule("read") == currentSymbol) match {
            case Some(rule) =>
              println(s"🔄 Transition: (${state.state}, ${rule("read")}) -> (${rule("to_state")}, ${rule("write")}, ${rule("action")})")

              // Update the tape and calculate the new head position.
              val newTape = state.tape.updated(state.head, rule("write"))
              val newHead = rule("action") match {
                case "LEFT"  => state.head - 1
                case "RIGHT" => state.head + 1
              }

              // Recursively call stepMachine with the updated state.
              stepMachine(config, TuringMachineState(newTape, newHead, rule("to_state")))

            case None =>
              val msg = s"⛔ Deadlock: No valid transition from state '${state.state}' with symbol '${state.tape(state.head)}'"
              println(msg)
              Some(msg)
          }

        case None =>
          val error = s"❌ Error: No transitions defined for state '${state.state}'"
          println(error)
          Some(error)
      }
    }
  }

  /**
   * Runs the Turing Machine with the given input and configuration.
   * @param config The Turing Machine configuration.
   * @param input The initial input string to load onto the tape.
   */
  def runMachine(config: TuringConfig, input: String): Unit = {
    // Convert the input string to a vector of symbols and initialize the machine state.
    val initialTape = input.map(_.toString).toVector
    val initialState = TuringMachineState(initialTape, head = 0, state = config.initial)

    // Print the machine header.
    println(renderMachineHeader(config))

    // Start executing the machine.
    stepMachine(config, initialState)
  }
}
