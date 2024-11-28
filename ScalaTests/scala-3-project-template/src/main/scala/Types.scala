//src/main/scala/Types.scala

package turing


object Types {
	// Configuración de la máquina de Turing
	case class TuringConfig(
	name: String,
	alphabet: List[String],
	blank: String,
	states: List[String],
	initial: String,
	finals: List[String],
	transitions: Map[String, List[Map[String, String]]]
	)

	// Estado de la máquina de Turing
	case class TuringMachineState(
	tape: Vector[String],
	head: Int,
	state: String
	)
}