package _3_domain_adts

import cats.Semigroup

object SemigroupMonoid:
  @main
  def main2(): Unit =
    // Generic combination function
    // Merge or summarize all elements of a list

    // we need a way to denote valid operations on A
    def incompleteReduce[A](values: A*): A = ???
    // How we inject the "combination behavior"?

    // OOP Answer
    // Based on polymorphic functions
    // <: means "subtype of" in OOP
    // Cannot implement for "closed" types
    trait Combinable[A]:
      def combine(other: A): A

    def subtypingReduce[A <: Combinable[A]](values: A*): A =
      values.reduce(_.combine(_))

    // FP answer: type classes
    trait Combinator[A]:
      def combine(x: A, y: A): A

    object Combinator:
      implicit val intCombinable: Combinator[Int] = new Combinator[Int]:
        override def combine(x: Int, y: Int): Int = x + y

    def reduce[A](values: A*)(implicit combinator: Combinator[A]): A =
      values.reduce(combinator.combine)

    val intSum = reduce(1, 2, 3)
    println(intSum)
    // intSum: Int = 6

    // Semigroup
    // The type class encoding the notion of combination
    trait Semigroup2[A]:
      def combine(x: A, y: A): A

    // Often used with nicer syntax from Cats
    import cats.implicits.catsSyntaxSemigroup
    val result = List(1) |+| List(2, 3)

    // Semigroup laws
    // Associativity
    val associative = (1 |+| (2 |+| 3)) == ((1 |+| 2) |+| 3)
    println(associative)
    // associative: Boolean = true

    // If a type A has lawful semigroup
    // We can arbitrarily distribute the computation of A's in parallel and
    // aggregate the results correctly
    // Parallelization guarantee "for free"

    // Monoid
    // The type class encoding the notion of neutral element
    // E.g. What if the list of elements if empty?
    trait Monoid[A] extends Semigroup[A]:
      def empty: A

    // Monoid laws
    // Left and right identity
    val leftIdentity  = (1 |+| 0) == 1
    val rightIdentity = (0 |+| 1) == 1

    // Recursive instances
    // Building bigger instances out of smaller ones
    implicit def optionSemigroup[A: Semigroup]: Semigroup[Option[A]] =
      case (Some(x), Some(y)) => Some(Semigroup[A].combine(x, y))
      case (Some(x), None)    => Some(x)
      case (None, Some(x))    => Some(x)
      case (None, None)       => None

    val sumMaybeNumbers =
      Semigroup[Option[Int]].combine(Some(1), Some(2))

    println(sumMaybeNumbers)
    // sumMaybeNumbers: Option[Int] = Some(3)

  end main2
end SemigroupMonoid
