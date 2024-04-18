package _3_domain_adts

import cats.{Eval, Semigroup}
import cats.data.NonEmptyList

import java.time.LocalDate

object CatsDataTypes:
  @main
  def main3(): Unit =
    // NonEmptyList, which is a Semigroup
    // They can be combined
    // We also have ValidatedNel[E, A], which is an alias for Validated[E, NonEmptyList[A]]

    // NonEmptyChain, which is a Semigroup
    // Alternative to List, with constant time append and prepend
    // We have NonEmptyChain too

    // Complexity
    //            Chain   List
    // Prepend   O(1)     O(1)
    // Append    O(1)     O(n)
    // Traverse  O(n)     O(n2)

    // Scala is eagerly evaluated
    // Arguments are eagerly evaluated before the function is called
    val y = List(1, 2, 3).map(x => x + 1)

    // Scala has lazy to avoid eager evaluation
    // Quite heavy since it uses @volatile
    // def only evaluated when called
    // by-name parameters only evaluated when used

    // Eval, allows to control how pure expressions must be evaluated
    // Eval.now -> compute once + cache the result
    // Eval.later -> more efficient, lazy
    // Eval.always -> evaluate every time
    // Eval is stack-safe
    val lazyEval = Eval.later {
      println("Working...")
      1 + 2 * 3
    }

    val do1 = lazyEval.value
    // Working...
    // 1 + 2 * 3

    val do2 = lazyEval.value
    // 1 + 2 * 3

    // Option
    import cats.syntax.all.*

    val empty: Option[Nothing] = None
    // val empty: Option[Int] = None
    val emptyInf: Option[Int] = none[Int]
    // val emptyInf: Option[Int] = None

    val maybeFive: Option[Int] = Some(5)
    // val maybeFive: Option[Int] = Some(5)
    val maybeFiveInf: Option[Int] = 5.some
    // val maybeFiveInf: Option[Int] = Some(5)

    // Either
    val left: Either[String, Nothing] = Left("Error")
    // val left: Either[String, Int] = Left("Error")
    val leftInf: Either[String, Int] = "Error".asLeft

    val right: Either[Nothing, Int] = Right(5)
    // val right: Either[String, Int] = Right(5)
    val rightInf: Either[String, Int] = 5.asRight

    // Type safe replacements
    // Using === or !==
    // Order is the type-safe replacement for compareTo
    // Show is the type-safe replacement for toString

  end main3
end CatsDataTypes
