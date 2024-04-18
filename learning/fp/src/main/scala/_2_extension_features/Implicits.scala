package _2_extension_features

import scala.language.{implicitConversions, postfixOps}

object Implicits:
  @main
  def main(): Unit =
    // IMPORTANT !!!
    // In Scala 2, we make use of implicit, to create DSLs

    // In Scala 3
    // Has removed implicits in favor of "Contextual Abstractions"
    // 'using' / 'given' instead of implicit
    // 'extension' for extension methods

    // Apply methods
    val sum: (Int, Int) => Int = _ + _
    // sum: (Int, Int) => Int = <function2>

    val sum1 = sum.apply(1, 2)
    // sum1: Int = 3

    // Equivalent to, we can drop the apply
    val sum2 = sum(1, 2)
    // sum2: Int = 3

    // This in Scala very versatile syntax
    // Index/key access to collections, smart constructions, function invocation...

    // Infix notation
    // No dot or parenthesis required on invocation
    val sum3 = 1.+(1)
    // sum3: Int = 2

    // Equivalent
    val sum4 = 1 + 1
    // sum4: Int = 2

    // Postfix notation (but discouraged)
    // val seconds = 2 seconds

    // Curly braces
    // {} allows using code blocks instead of single expressions
    // You may drop the usual () for arguments
    // Nice to crate control-flow-like code
    //  dfa transitions { transition =>
    //    transition on '0' from S0 to S1
    //  }

    // Implicits and type classes
    // The compiler "fills in" the missing arguments

    object Ports:
      implicit val http: Int = 80

    // Lexical scope lookup
    // Similar to how identifiers lookup works
    // Nearest val in current function or file
    // Import statement

    // import Ports._
    // val defaultPort = implicitly[Int]
    // defaultPort: Int = 80

    // Implicit scope lookup
    // Fallback when lexical scope fails
    // Companion objets, source and target, companion of type arguments
    // Package objects

    // No ambiguous implicits
    // Compilation fails when more than one apply
    // Given the rules about priority
    // Remove them until there is only one

    implicit val defaultOne: Int = 80
    implicit val defaultTwo: Int = 443

    def connect(implicit port: Int): Unit = println(s"Connecting to port $port")

    // connect
    // Ambiguous given instances: both value http in object Ports and value defaultOne in
    // object SyntacticSugar match type Int of parameter port of method connect in object SyntacticSugar

    // Implicit composition
    case class NoSpacer(text: String)

    implicit val defaultSeparator: String = "-"

    implicit def spaceReplacer(input: String)(implicit separator: String): NoSpacer =
      NoSpacer(input.replaceAll(" ", separator))

    val removeSpaces: NoSpacer = "merry go round"
    println(removeSpaces)
    // removeSpaces: NoSpacer = NoSpacer(merry-go-round)

    // Generic implicits
    implicit val intCombine = (x: Int, y: Int) => x + y

    def reduce[A](values: A*)(implicit reducer: (A, A) => A): A =
      values.reduce(reducer)

    val sumNumbers = reduce(1, 2, 3)
    println(sumNumbers)
    // sumNumbers: Int = 6

    // Context bounds
    // Another way to express implicits
    trait Order[T]:
      def compare(x: T, y: T): Int

    def max[T](x: T, y: T)(implicit order: Order[T]): T =
      if order.compare(x, y) > 0 then x else y

    // Could also be like this, removing implicit parameter
    def maxBound[T: Order](x: T, y: T): T =
      if implicitly[Order[T]].compare(x, y) > 0 then x else y

    // Extension methods
    // Class is closed, we have no control over it
    // We can extend them with implicits
    class NoVowels(value: String):
      private val str: String          = value.replaceAll("[aeiou]", "")
      def +(other: NoVowels): NoVowels = NoVowels(str + other.str)
      override def toString            = s"NoVowels($str)"

    implicit def stringToNoVowels(s: String): NoVowels = NoVowels(s)

    val hll: NoVowels = "hello"
    println(hll)
    // hll: NoVowels = NoVowels(hll)

    val hellWrld: NoVowels = hll + "world"
    println(hellWrld)
    // hellWrld: NoVowels = NoVowels(hellWrld)

    // Implicit class
    // Some restrictions apply: defined in the same scope, only one constructor argument
    // Also no other method or class with the same name
    // implicit class NoVowels2(value: String):

  end main
end Implicits
