package _2_extension_features

object TypeClasses:
  @main
  def main2(): Unit =
    // Type classes
    // Type safe ad hoc polymorphism
    // Type class -> generic interface defining a behavior
    // Instance -> implementation for concrete types

    // Eq
    // Type safe replacement for ==
    // val makesSense = 1 == 1

    // Scala 2 (fixed in Scala 3)
    // val notSoMuch = "true" == 2

    // Cats
    // import cats.syntax.all._
    // val betterNow = "true" === 2

    // Declaring a type class
    // Generic interface for behavior, a trait with a set of methods
    // At least one type parameter
    trait Equal[A]:
      def equals(a: A, b: A): Boolean

    trait Converter[A, B]:
      def convert(a: A): B
      def revert(b: B): A

    // Declaring instances
    // Implementing the concrete type
    // Instances are usually declared on companion object
    object Equal {
      implicit val intInstance: Equal[Int] = new Equal[Int] {
        def equals(a: Int, b: Int): Boolean = a == b
      }

      implicit val stringInstance: Equal[String] = new Equal[String] {
        def equals(a: String, b: String): Boolean = a == b
      }

      // Instances may require other instances
      implicit def listInstance[A](implicit eq: Equal[A]): Equal[List[A]] = new Equal[List[A]] {
        def equals(a: List[A], b: List[A]): Boolean = a == b
      }
    }

    object MyEqual:
      // New syntax from type class operations
      // Use apply as implicitly
      implicit val intEquals: Equal[Int] = (a: Int, b: Int) => a == b
      implicit val stringEquals: Equal[String] = (a: String, b: String) => a == b

      def apply[A](implicit eq: Equal[A]): Equal[A] = eq

      implicit class EqualOps[A](a: A) {
        def myEq(b: A)(implicit eq: Equal[A]): Boolean = eq.equals(a, b)
      }

    import MyEqual.EqualOps
    val notEqualNumbers = 1 myEq 2
    println(notEqualNumbers)

    // Laws
    // Some type classes require laws compliance
