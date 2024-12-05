## sbt project compiled with Scala 3

### Usage

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`, and `sbt console` will start a Scala 3 REPL.

For more information on the sbt-dotty plugin, see the
[scala3-example-project](https://github.com/scala/scala3-example-project/blob/main/README.md).



## compile and execute with scalac and scala
scalac -cp "lib/*" Types.scala SirAlan.scala Executor.scala Main.scala

scala -cp ".:lib/*" turing.TuringMachineValidator

## run complexity calculator with sbt

sbt:Ft_Turing> runMain turing.ComplexityAnalyzer src/main/resources/is_palindrome.json