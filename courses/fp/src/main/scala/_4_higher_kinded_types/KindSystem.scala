package _4_higher_kinded_types

import java.time.LocalDate

object KindSystem:
  @main
  def main2(): Unit =
    // Type represents a finite set of values
    val min = Int.MinValue
    val max = Int.MaxValue

    // Is Option a type?
    // What about List, Vector, etc?
    // No, there are value types
    // Int, Option[Int], List[Int], etc
    // This kind of types are called * ("star")

    // Higher kinded types
    // * -> *, one type parameter
    sealed abstract class Option2[A]

    // * -> * -> *, two type parameters
    sealed abstract class Either2[A, B]

    // Type application
    // Option is a type constructor, it has kind * -> *
    sealed trait Option3[+A]
    case object None extends Option3[Nothing]
    case class Some[A](value: A) extends Option3[A]

    // Int is a value type, it has kind *
    // Option[Int] has kind *
    val maybeInt: Option3[Int] = Some(5)
    // maybeInt: Option3[Int] = Some(5)

    // Scala HKT support
    // F of kind * -> *
    trait Foo[F[_]]:
      def apply[A](fa: A): F[A]

    lazy val maybeFoo = new Foo[Option]:
      override def apply[A](a: A): Option[A] = Option(a)

    val maybeFoo1 = maybeFoo(5)
    println(maybeFoo1)
    // maybeFoo1: Option[Int] = Some(5)

    // type F = Option[_]
    // val maybe: F = Option(5)
    // maybe: Option[_] = Some(5)
    // Option[_] does not mean the same as a type position that it does in the type parameter position

    // Existential types
    // We know what the type is of contained elements
    // But we forget what the type is
    val maybe: Option[_] = Option(5)
    // maybe: Option[Int] = Some(5)

    // There is little we can do about them
    val emptiness = maybe.empty
    // emptiness: Boolean = false

    // Universal types
    // Different from when create a function which operates on any choice of A
    // def something[A](value: A): Option[A] = Some(value)

    // Dealing with multiple parameters
    // Pitfall requires types with one type parameter, kind * -> *
    trait Pitfall[F[_]]:
      def apply[A](a: A): F[A]

    // How do we write an instance for Either, of kind * -> * -> *
    // This does not work
    //    lazy val eitherPitfall = new Pitfall[Either[String, _]]:
    //      override def apply[A](a: A): Either[String, A] = ???

    // We want to "fill the first hole with a String and leave the other"
    // But still we are introducing an existence type of kind *

    // Workaround with partial type application
    // We can introduce an alias of kind * -> *
    type StringOr[A] = Either[String, A]

    trait Foo2[F[_]]:
      def apply[A](fa: A): F[A]

    lazy val maybeFoo2 = new Foo2[StringOr]:
      override def apply[A](a: A): StringOr[A] = Right(a)

    val maybeFoo2a = maybeFoo2(5)
    println(maybeFoo2a)
    // maybeFoo2a: StringOr[Int] = Right(5)

    // Type alias limitations
    // But we want to define Foo for any Either[E, A], not just E as String
    type TOrE[T, A] = Either[T, A]

    // We could do it on a very bad way, but we have the kind-projector plugin
    // def eitherFoo[T] = new Foo[Either[T, *]]:
    //   override def apply[A](a: A): Either[T, A] = Right(a)
    // val eitherFoo = new eitherFoo[String](5)
    // eitherFoo: Either[String, Int] = Right(5)

    // Scala 3 to the RESCUE! Type lambdas
    type T[E] = [A] =>> Either[E, A]

    // lazy val eitherFoo = new Foo[T[E]]:
    //  override def apply[A](a: A): T[A] = Right(a)

  end main2
end KindSystem
