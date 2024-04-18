package _3_domain_adts

import java.time.LocalDate

object ADTs:
  @main
  def main(): Unit =
    // Algebraic Data Types
    // The FP-way to model data
    // Two ways to model data:
    // - Putting values together (product)
    // - Giving a choice (sum)
    // Pattern matching to extract values

    // Product types
    // Immutable and cannot be extended
    // Allow pattern matching
    case class User(id: Int, name: String)
    case class Account(id: Int, owner: User, balance: Int)
    case class Debit(account: Account, amount: Int)

    val user = User(1, "John")
    println(user)
    // User(1,John)

    val debitOp = Debit(Account(1, user, 100), 50)
    println(debitOp)
    // Debit(Account(1,User(1,John),100),50)

    // Sealed types
    // Restricted hierarchy of classes
    sealed abstract class Color
    case object Red   extends Color
    case object Green extends Color
    case object Blue  extends Color

    val color: Color = Red
    println(color)
    // Red

    // Sum types
    // Giving a choice, aka, coproduct types
    case class Role(name: String)

    sealed trait SpecialUser                                extends Product with Serializable
    case class Admin(name: String, roles: List[Role])       extends SpecialUser
    case class Customer(name: String, birthDate: LocalDate) extends SpecialUser

    // Pattern matching
    object UserMatch:
      def name(user: SpecialUser): String = user match
        case Admin(name, _)    => name
        case Customer(name, _) => name

    val customerName = UserMatch.name(Customer("John", LocalDate.now()))
    println(customerName)
    // John

    // Exhaustive pattern matching
    def nameWrong(user: SpecialUser): String = user match
      case Admin(name, _) => name
    // match may not be exhaustive. It would fail on pattern case: Customer(_, _)

    // Take into account, that, in Scala, every case class extends Product and Serializable automatically
    // So every element in both does so too
    // Scala includes it in the inferred type

    // Either is a sum type
    sealed trait MyEither[+E, +A]

    case class Left[+E](value: E)  extends MyEither[E, Nothing]
    case class Right[+A](value: A) extends MyEither[Nothing, A]

    // Cats brings many niceties to the table
    import cats.syntax.all.*

    type Error   = String
    type Success = Int

    def toInt(str: String): Either[Error, Success] =
      if str.forall(_.isDigit) then str.toInt.asRight
      else s"Could not convert $str to int".asLeft

    val error = toInt("0a")
    println(error)
    // Left(Could not convert 0a to int)

    // Either as block for sum types
    // Nested Either for arbitrary number of choices

    type Choice = Either[Int, Either[Boolean, String]]

    def typed(from: String): Choice =
      if from.forall(_.isDigit) then from.toInt.asLeft
      else if from == "true" || from == "false" then from.toBoolean.asLeft.asRight
      else from.asRight.asRight

    val typedTrue = typed("true")
    println(typedTrue)
    // Right(Left(true))

    // OOP vs FP
    // OOP classes implementing interfaces
    // - Mixes data with behavior
    // - Uses fields and methods
    // FP function modules transforming data
    // - Decouples data from behavior
    // - Uses data (ADTs) and functions
    // Do not put methods into ADT classes

    // What are those + before the type arguments? -> Variance annotations
    sealed trait Either3[+E, +A]

    // Interaction between types and subtyping
    // Interaction between FP and OOP
    // <: means "subtype of" in OOP
    // Either[E, List[A]] <: Either[E, Seq[A]] <: Either[E, Any]

    // In Scala 3
    // There is direct syntax for ADTs
    enum MyUser:
      case Admin(name: String, roles: List[Role])
      case Customer(name: String, birthDate: LocalDate)

  end main
end ADTs
