package _1_functional_error_handling

import cats.data.*
import cats.syntax.all.*

/*
 * Limitations of flatMap:
 * - Dependencies between the different computations are not explicit
 * - No error accumulation, if one of the computations fails, the whole computation fails
 */
object ValidatedFP:
  // Validated is a data type that can accumulate errors
  // Covariant, error-acculumating friendly type from Cats
  sealed trait Validated2[+E, +A]

  // Looks like Either, but has different operations
  case class Valid2[A](value: A) extends Validated2[Nothing, A]
  case class Invalid2[E](error: E) extends Validated2[E, Nothing]

  class MyError(val message: String)

  // Can't use Validated directly, because it's not a Monad
  // Use ValidatedNec (NonEmptyChain) instead
  // NonEmptyChain is a data type that can't be empty
  // This allows us to accumulate errors

  // Horizontal composition
  // Two pieces of data that depend on each other
  def ageIsPositive(age: Int): ValidatedNec[MyError, Int] =
    if (age >= 0) age.valid
    else MyError("Age must be positive").invalidNec

  def majorInTheUS(age: Int): ValidatedNec[MyError, Int] =
    if age >= 21 then age.valid
    else MyError("You must be 21 or older").invalidNec

  val age = -22

  // Combining the two computations
  // Can't use flatMap, because doesn't have it
  val enterLiquorShop =
    ageIsPositive(age) *> majorInTheUS(age)

  // Vertical composition
  // Two pieces of data that are independent of each other

  class Customer(name: String, age: Int)

  def emptyCheck(name: String): ValidatedNec[MyError, String] =
    if name.isEmpty then MyError("Name can't be empty").invalidNec
    else name.validNec

  def validateCustomer(name: String, age: Int): ValidatedNec[MyError, Customer] =
    (emptyCheck(name), majorInTheUS(age)).mapN((_, _) => Customer(name, age))

  val customer = validateCustomer("John", 22)

  // *> Versus mapN
  // *> is used when we don't care about the result of the first computation
  // mapN is used when we care about the result of both computations
  // In both cases, the errors are accumulated

