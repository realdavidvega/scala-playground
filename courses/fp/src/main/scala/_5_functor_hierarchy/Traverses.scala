package _5_functor_hierarchy

import cats.*
import cats.data.{Validated, ValidatedNec}
import cats.syntax.all.*

object Traverses:
  @main
  def main3(): Unit =
    // Intuition for Traverse
    // Functor applies a function to one F[A]
    // Applicative applies a function to any number of independent F[_]
    // In both cases the amount of F[_] is fixed

    // Traverse works over an arbitrary collection type
    // traverse applies f over every element of fa and generated one single F at the top
    trait MyTraverse[F[_]]:
      def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]
      def sequence[G[_]: Applicative, A](fga: F[G[A]]): G[F[A]] =
        traverse(fga)(ga => ga)

    // Traverse example
    val okList    = List(1, 2, 3)
    val errorList = List(1, 0)

    def f(n: Int): ValidatedNec[String, Int] =
      Validated.condNec(n > 0, n, "Must be positive")

    val traverseOk = okList.traverse(f)
    println(traverseOk)
    // Valid(List(1, 2, 3))

    val traverseError = errorList.traverse(f)
    println(traverseError)
    // Invalid(NonEmptyChain(Must be positive))

    // Sequence example
    // You can separate the work of traverse in two phases
    // 1. Apply f to every element (map)
    // 2. Combine the results, or F structure (sequence)
    val mapEL = errorList.map(f)
    println(mapEL)
    // List(Valid(1), Invalid(NonEmptyChain(Must be positive)))

    val result = mapEL.sequence
    println(result)
    // Invalid(NonEmptyChain(Must be positive))

    // Talking about effects
    // We often look at functors as two ways:
    // 1. As containers of elements: List (N amount of elements), Option (1 or 0 elements)
    // 2. As adding effects to value: Option (adds failure effect), Future (async effect)

    // In the type of traverse
    // 1. F is the container of elements
    // 2. G is the container of effects
    // 3. A is the element
    // 4. B is the effect
    trait MyTraverse2[F[_]]:
      def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]

    // Functor is no-op traverse
    // type Id[A] = A
    // implicit def idForApplicative[A]: Applicative[Id] = new Applicative[Id]:
    //   def pure[A](a: A): Id[A] = a
    //   def ap[A, B](fa: Id[A])(f: Id[A => B]): Id[B] = f(fa)

    // We can use it to implement map via traverse
    // trait Traverse2[F[_]] extends Functor[F]:
    //  def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]
    //
    //  override def map[A, B](fa: F[A])(f: A => B): F[B] =
    //    traverse[Id, A, B](fa)(f)

    //              Applies                To
    // Functor      Pure fn.               One F[A]
    // Applicative  Pure fn.               Fixed number of F[A]
    // Traverse     Effect fn.             Every element in a container

  end main3
end Traverses
