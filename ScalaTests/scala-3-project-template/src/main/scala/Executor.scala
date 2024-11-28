// src/main/scala/turing/Executor.scala

package turing

import turing.Types._
import turing.Art._

object Executor {

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

  def renderTape(tape: Vector[String], head: Int): String = {
    tape.zipWithIndex.map {
      case (symbol, index) if index == head => s"<${symbol}>"
      case (symbol, _) => symbol
    }.mkString("[", "", "]")
  }

  def stepMachine(config: TuringConfig, state: TuringMachineState): Option[String] = {
    // Imprimir el estado actual de la cinta
    println(renderTape(state.tape, state.head))

    // Comprobar si estamos en un estado final
    if (config.finals.contains(state.state)) {
      val msg = s"Máquina detenida en estado final: ${state.state}"
      println(msg)
      return Some(msg)
    }

    // Verificar límites de la cinta
    if (state.head < 0 || state.head >= state.tape.length) {
      val error = s"Error: La cabeza está fuera de los límites de la cinta (posición ${state.head})"
      println(error)
      return Some(error)
    }

    // Obtener las transiciones para el estado actual
    val nextState = for {
      rules <- config.transitions.get(state.state) // Obtener las reglas del estado actual
      currentSymbol = state.tape(state.head)       // Leer el símbolo actual
      rule <- rules.find(_("read") == currentSymbol) // Encontrar una regla que coincida
    } yield {
      // Mostrar la transición aplicada
      println(s"(${state.state}, ${rule("read")}) -> (${rule("to_state")}, ${rule("write")}, ${rule("action")})")

      // Actualizar la cinta y la cabeza
      val newTape = state.tape.updated(state.head, rule("write"))
      val newHead = rule("action") match {
        case "LEFT"  => state.head - 1
        case "RIGHT" => state.head + 1
      }

      TuringMachineState(newTape, newHead, rule("to_state"))
    }

    nextState match {
      case Some(newState) => stepMachine(config, newState)
      case None =>
        val msg = s"Bloqueo detectado: No se encontró transición válida desde el estado '${state.state}' con el símbolo '${state.tape(state.head)}'"
        println(msg)
        Some(msg)
    }
  }

  def runMachine(config: TuringConfig, input: String): Unit = {
    val initialTape = input.map(_.toString).toVector
    val initialState = TuringMachineState(initialTape, head = 0, state = config.initial)

    println(renderMachineHeader(config))
    stepMachine(config, initialState)
  }
}
