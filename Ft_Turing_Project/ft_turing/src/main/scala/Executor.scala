package turing

import turing.Types._

object Executor {

  /**
   * Renders the header of the Turing Machine with its configuration details.
   * @param config Turing machine configuration containing alphabet, states, initial state, and final states.
   * @return A formatted string with the header of the machine.
   */
  def renderMachineHeader(config: TuringConfig): String = {
    val transitions = config.transitions.flatMap { case (state, rules) =>
      rules.map { rule =>
        f"(${state}, ${rule("read")}) -> (${rule("to_state")}, ${rule("write")}, ${rule("action")})"
      }
    }.mkString("\n")

    s"""                                                                         
    |************************************************************************************
    |*                                                                              
    |* ${config.name}                                                               
    |*                                                                              
    |************************************************************************************
    |Alphabet: [ ${config.alphabet.mkString(", ")} ]
    |States   : [ ${config.states.mkString(", ")} ]
    |Initial  : ${config.initial}
    |Finals   : [ ${config.finals.mkString(", ")} ]
    |************************************************************************************
    |$transitions
    |************************************************************************************
    """.stripMargin
  }

  /**
   * Renders the tape of the Turing Machine, marking the position of the head.
   * @param tape The tape represented as a vector of symbols.
   * @param head The position of the head on the tape.
   * @return A formatted string representing the tape.
   */
  def renderTapeWithPointer(tape: Vector[String], head: Int): String = {
    val tapeLine = tape.mkString("[", "", "]")
    val pointerLine = " " * (head + 1) + "v" // Adds spaces and places the `v` above the current head.
    s"$pointerLine\n$tapeLine"
  }

  def extendTapeRight(tape: Vector[String], blank: String): Vector[String] = {
    tape ++ Vector.fill(1)(blank)
  }

  def extendTapeLeft(tape: Vector[String], blank: String): (Vector[String], Int) = {
    (Vector.fill(1)(blank) ++ tape, 1)
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
  def stepMachine(config: TuringConfig, state: TuringMachineState, printHeader: Boolean = true): Option[String] = {
    // Print the table header only once, at the beginning.
    if (printHeader) {
      println("📜 TAPE                                                         🔽 CURRENT                           🔄 TRANSITION")
      println("                                                                State                       Read      Next State                   Write    Action")
      println("--------------------------------------------------------------------------------------------------------------------------------------------------")
    }

    if (config.finals.contains(state.state)) {
      val msg = s"🎉 Machine halted in final state: ${state.state}"
      println(renderTapeWithPointer(state.tape, state.head))
      println(msg)
      Some(msg)
    } else {
      val (workingTape, workingHead) = if (state.head < 0) {
        extendTapeLeft(state.tape, config.blank)
      } else if (state.head >= state.tape.length) {
        (extendTapeRight(state.tape, config.blank), state.head)
      } else {
        (state.tape, state.head)
      }

      val renderedTape = renderTapeWithPointer(workingTape, workingHead)

      config.transitions.get(state.state) match {
        case Some(rules) =>
          val currentSymbol = workingTape(workingHead)
          rules.find(rule => rule("read") == currentSymbol) match {
            case Some(rule) =>
              println(f"${renderedTape.split("\n")(0)}%-65s")
              println(f"${renderedTape.split("\n")(1)}%-65s${state.state}%-27s${rule("read")}%-10s${rule("to_state")}%-29s${rule("write")}%-9s${rule("action")}%-12s")

              val newTape = workingTape.updated(workingHead, rule("write"))
              val newHead = rule("action") match {
                case "LEFT"  => workingHead - 1
                case "RIGHT" => workingHead + 1
              }

              stepMachine(config, TuringMachineState(newTape, newHead, rule("to_state")), printHeader = false)

            case None =>
              val msg = s"⛔ Deadlock: No valid transition from state '${state.state}' with symbol '${workingTape(workingHead)}'"
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

  def runMachine(config: TuringConfig, input: String): Unit = {
    //val initialTape = input.map(_.toString).toVector ++ Vector.fill(1)(config.blank)
    val initialTape = input.map(_.toString).toVector
    val initialState = TuringMachineState(initialTape, head = 0, state = config.initial)

    println(renderMachineHeader(config))
    stepMachine(config, initialState)
  }
}