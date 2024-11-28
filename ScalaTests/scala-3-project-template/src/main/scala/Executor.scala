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

  @scala.annotation.tailrec
  def stepMachine(config: TuringConfig, state: TuringMachineState): Option[String] = {
    // Imprimir el estado actual de la cinta
    println(renderTape(state.tape, state.head))

    // Verificar si estamos en un estado final
    if (config.finals.contains(state.state)) {
      val msg = s"Máquina detenida en estado final: ${state.state}"
      println(msg)
      Some(msg)
    } 
    // Verificar si la cabeza está fuera de los límites
    else if (state.head < 0 || state.head >= state.tape.length) {
      val error = s"Error: La cabeza está fuera de los límites de la cinta (posición ${state.head})"
      println(error)
      Some(error)
    } 
    // Manejo de transiciones
    else {
      config.transitions.get(state.state) match {
        case Some(rules) =>
          val currentSymbol = state.tape(state.head)
          rules.find(rule => rule("read") == currentSymbol) match {
            case Some(rule) =>
              println(s"(${state.state}, ${rule("read")}) -> (${rule("to_state")}, ${rule("write")}, ${rule("action")})")

              val newTape = state.tape.updated(state.head, rule("write"))
              val newHead = rule("action") match {
                case "LEFT"  => state.head - 1
                case "RIGHT" => state.head + 1
              }

              val newState = TuringMachineState(newTape, newHead, rule("to_state"))
              stepMachine(config, newState) // Llamada recursiva en posición de cola

            case None =>
              val msg = s"Bloqueo detectado: No se encontró transición válida desde el estado '${state.state}' con el símbolo '${currentSymbol}'"
              println(msg)
              Some(msg)
          }

        case None =>
          val error = s"Error: No hay transiciones definidas para el estado '${state.state}'"
          println(error)
          Some(error)
      }
    }
  }


  def runMachine(config: TuringConfig, input: String): Unit = {
    val initialTape = input.map(_.toString).toVector
    val initialState = TuringMachineState(initialTape, head = 0, state = config.initial)

    println(renderMachineHeader(config))
    stepMachine(config, initialState)
  }
}
