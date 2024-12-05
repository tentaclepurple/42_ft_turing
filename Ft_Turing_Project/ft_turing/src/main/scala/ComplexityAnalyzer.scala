package turing

import scala.sys.process._

object ComplexityAnalyzer {
  case class ExecutionResult(inputLength: Int, steps: Int)
  case class ComplexityResult(
    complexity: String,
    executionResults: List[ExecutionResult],
    growthRatio: Double
  )


  def main(args: Array[String]): Unit = {
    args.headOption match {
      case Some(jsonFile) =>
        val result = for {
          inputs <- generateTestInputs(jsonFile)
          results <- executeAllTests(jsonFile, inputs)
          analysis <- analyzeComplexity(results)
          _ <- printResults(analysis)
        } yield ()
        
      case None =>
        println("Usage: FunctionalComplexityAnalyzer <json_file>")
    }
  }


    private def generateTestInputs(jsonFile: String): Option[List[String]] = {
        val fileName = jsonFile.split("/").last.stripSuffix(".json")
        
        val inputs = fileName match {
            case "unary_add" => List(
            "1+1=",
            "11+11=",
            "1111+1111=",
            "11111111+11111111="
            )
            
            case "unary_sub" => List(
            "11-1=",
            "1111-11=",
            "11111111-1111=",
            "1111111111-11111="
            )
            
            case "is_palindrome" => List(
            "101",
            "11011",
            "111101111",
            "11111111011111111"
            )
            
            case "0n1n" => List(
            "01",
            "0011",
            "000111",
            "00001111"
            )
            
            case "02n" => List(
            "00",
            "0000",
            "000000",
            "00000000"
            )
            
            case _ => List.empty
        }
        
        Option(inputs).filterNot(_.isEmpty)
        }

  private def executeAllTests(
    jsonFile: String, 
    inputs: List[String]
  ): Option[List[ExecutionResult]] = {
    val results = inputs.traverse(input => executeProgram(jsonFile, input))
    results.map(_.zip(inputs).map { case (steps, input) => 
      ExecutionResult(input.filterNot(_ == '=').length, steps)
    })
  }

    private def executeProgram(
        jsonFile: String, 
        input: String
        ): Option[Int] = {
        val command = s"sbt \"run $jsonFile $input\""
            
        val steps = Process(command)
            .lazyLines
            .count(line => line.trim == "v")
            
        Option(steps)
        }

    private def analyzeComplexity(
        results: List[ExecutionResult]
        ): Option[ComplexityResult] = {
        val growthRatios = results
            .sliding(2)
            .collect {
            case List(a, b) => 
                val stepRatio = b.steps.toDouble / a.steps
                val inputRatio = b.inputLength.toDouble / a.inputLength
                stepRatio / inputRatio
            }
            .toList

        val avgRatio = if (growthRatios.nonEmpty) growthRatios.sum / growthRatios.length else 0

        println("\nGrowth ratios between consecutive inputs:")
        growthRatios.zipWithIndex.foreach { case (ratio, i) =>
            val a = results(i)
            val b = results(i + 1)
            println(f"n: ${a.inputLength}%2d → ${b.inputLength}%2d, steps: ${a.steps}%3d → ${b.steps}%3d, ratio: $ratio%.2f")
        }

        val complexity = determineComplexity(results, avgRatio)
        Option(ComplexityResult(complexity, results, avgRatio))
        }

  private def determineComplexity(
    results: List[ExecutionResult], 
    avgRatio: Double
  ): String = {
    avgRatio match {
      case r if r <= 0.2 => "O(1)"         // constant
      case r if r <= 1.1 => "O(n)"         // Lineal
      case r if r <= 1.5 => "O(n log n)"   // logarithmic
      case r if r <= 2.2 => "O(n²)"        // Quadratic
      case _ => "O(n³)"                     // qubic
    }
  }

  private def printResults(analysis: ComplexityResult): Option[Unit] = {
    Option {
      println("\n🔍 Complexity Analysis:")
      println("\nExecution results:")
      analysis.executionResults.foreach { r =>
        println(f"Input length: ${r.inputLength}%2d, Steps: ${r.steps}%3d")
      }
      println(s"\nComplexity appears to be: ${analysis.complexity}")
      
      println("\nExplanation:")
      println("- Each step is one transition of the Turing machine")
      println("- Input length is the number of symbols (excluding '=')")
      println("- Complexity is determined by how the number of steps grows with input size")
    }
  }

  implicit class TraverseOps[A](xs: List[A]) {
    def traverse[B](f: A => Option[B]): Option[List[B]] = {
      xs.foldRight[Option[List[B]]](Some(Nil)) { (a, acc) =>
        for {
          b <- f(a)
          bs <- acc
        } yield b :: bs
      }
    }
  }
}