package turing

import turing.Types._

object Executor {
  def renderMachineHeader(config: TuringConfig): String = {
    s"""
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

  def renderTape(tape: Vector[String], head: Int): String = {
    tape.zipWithIndex.map {
      case (symbol, index) if index == head => s"<${symbol}>"
      case (symbol, _) => symbol
    }.mkString("[", "", "]")
  }

  def stepMachine(config: TuringConfig, state: TuringMachineState): Unit = {
    // Imprimir el estado actual de la cinta
    println(renderTape(state.tape, state.head))

    if (config.finals.contains(state.state)) {
      println(s"Máquina detenida en estado final: ${state.state}")
      return
    }

    if (state.head < 0 || state.head >= state.tape.length) {
      println(s"Error: La cabeza está fuera de los límites de la cinta (posición ${state.head})")
      return
    }

    config.transitions.get(state.state) match {
      case Some(rules) =>
        val currentSymbol = state.tape(state.head)
        rules.find(_("read") == currentSymbol) match {
          case Some(rule) =>
            println(s"(${state.state}, ${rule("read")}) -> (${rule("to_state")}, ${rule("write")}, ${rule("action")})")
            
            // Actualizar la cinta y la cabeza
            val newTape = state.tape.updated(state.head, rule("write"))
            val newHead = rule("action") match {
              case "LEFT" => state.head - 1
              case "RIGHT" => state.head + 1
            }
            
            // Solo continuar si la cabeza está dentro de los límites
            if (newHead >= 0 && newHead < state.tape.length) {
              val newState = TuringMachineState(newTape, newHead, rule("to_state"))
              stepMachine(config, newState)
            } else {
              println(s"Máquina detenida: La cabeza se movió fuera de los límites (posición ${newHead})")
            }
            
          case None =>
            println(s"Bloqueo detectado: No se encontró transición válida desde el estado '${state.state}' con el símbolo '${currentSymbol}'")
        }
      case None =>
        println(s"Error: El estado actual '${state.state}' no tiene transiciones definidas")
    }
  }

  def runMachine(config: TuringConfig, input: String): Unit = {
    val initialTape = input.map(_.toString).toVector
    val initialState = TuringMachineState(initialTape, head = 0, state = config.initial)

    println(renderMachineHeader(config))
    stepMachine(config, initialState)
  }
}