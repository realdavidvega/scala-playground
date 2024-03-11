package _0_introduction_to_fp

import cats.implicits.catsSyntaxEq
import cats.Eq

object IntroductionToFP:
  // pure
  private def addPure(x: Int, y: Int): Int = x + y

  // not pure
  private def addNotTotal(x: Int, y: Int): Int = x match
    case 0 => throw new IllegalArgumentException("zero")
    case _ => x + y

  // not pure
  private def addNotPure(x: Int, y: Int): Int =
    print(x)
    x + y

  private case class Account(balance: Int)

  // pure
  private def mergeAccounts(a: Account, b: Account): Account =
    Account(a.balance + b.balance)

  // db
  private case class AccountDB(private var db: Set[Account] = Set.empty[Account]):
    def remove(accounts: Set[Account]): Unit =
      db = db -- accounts

    def save(account: Account): Unit =
      db = db + account

  // init db
  private val accountDB = AccountDB()

  // merge
  private def mergeAccountsDB(a: Account, b: Account): Account =
    val merged = Account(a.balance + b.balance)
    accountDB.remove(Set(a, b)) // side effect
    accountDB.save(merged)      // side effect
    merged

  // higher order functions (first-class citizens)
  // accept a function as an argument or return a function as a result
  case class OtherAccount(balance: Long):
    // Long to Long, does something with a Long and returns a Long
    def updateBalance(op: Long => Long): Unit => OtherAccount =
      Unit => OtherAccount(op(this.balance))

  // recursion
  // not based in mutability
  // goes better with the substitution model
  // recursion may overflow the stack

  // if n is 0, return 1, else return the product of n and factorial(n - 1)
  private def factorial(n: Int): Int = n match
    case 0 => 1
    case _ => n * factorial(n - 1)

  // if list is empty, return 0, else return the sum of the head and the rest
  def sum(list: List[Int]): Int = list match
    case Nil          => 0
    case head :: tail => head + sum(tail)

  // tail recursion
  // specific case of recursion, in which the recursive call is the last step in the function
  // we can use the annotation @tailrec to make it safer
  private def sumSafer(list: List[Int]): Int =
    @scala.annotation.tailrec
    def doSum(in: List[Int], acc: Int): Int = in match
      case Nil          => acc
      case head :: tail => doSum(tail, head + acc)
    doSum(list, 0)

  @main
  def main(): Unit =
    // 3 - pure
    println(addPure(1, 2))

    // 1500 - pure
    println(mergeAccounts(Account(500), Account(1000)))

    // referential transparency
    val two = addPure(1, 1)
    // could be replaced by 2 everywhere in the program

    // true
    val equals =
      two == addPure(1, 1) &&
        two == 1 + 1 &&
        two == 2

    // true
    val mergedAcc = mergeAccounts(Account(500), Account(1000))
    val equal =
      mergedAcc == mergeAccounts(Account(500), Account(1000)) &&
        mergedAcc == Account(500 + 1000) &&
        mergedAcc == Account(1500)

    // side effects
    val merged = mergeAccountsDB(Account(500), Account(1000)) // 1500L

    // supposed to be empty, but not
    val updatedDB = accountDB

    // Not referentially transparent despite same type and value, because of side effects
    val notEquivalent = Account(1500)

    // Higher order update
    val otherAccount = OtherAccount(1000)

    // Passing a function as an argument, and returning a function
    val updateFun = otherAccount.updateBalance(_ + 40000000)

    // Passing a function as an argument, and returning an account
    val millonaireAccount = updateFun(())
    println(s"Millonaire account ${millonaireAccount}")

    val largeSum = sumSafer(List.fill(10000000)(1))
    println(s"Large sum ${largeSum}")

    // Cats
    val useIt = Eq.eqv(3, 3)
    val nicer = 3 === 3

    println(s"Is equals: $useIt")
    println(s"Is equals, but nicer: $nicer")
  end main
end IntroductionToFP
