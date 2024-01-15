object FP {
  // pure
  private def addPure(x: Int, y: Int): Int = x + y

  // not pure
  private def addNotTotal(x: Int, y: Int): Int = x match
    case 0 => throw new IllegalArgumentException("zero")
    case _ => x + y

  // not pure
  private def addNotPure(x: Int, y: Int): Int = {
    print(x)
    x + y
  }

  private case class Account(balance: Int)

  // pure
  private def mergeAccounts(a: Account, b: Account): Account =
    Account(a.balance + b.balance)

  // db
  private case class AccountDB(private var db: Set[Account] = Set.empty[Account]) {
    def remove(accounts: Set[Account]): Unit =
      db = db -- accounts

    def save(account: Account): Unit =
      db = db + account
  }

  // init db
  private val accountDB = AccountDB()

  // merge
  private def mergeAccountsDB(a: Account, b: Account): Account = {
    val merged = Account(a.balance + b.balance)
    accountDB.remove(Set(a, b)) // side effect
    accountDB.save(merged) // side effect
    merged
  }

  @main
  def main(): Unit = {
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

    // side effects,
    val merged = mergeAccountsDB(Account(500), Account(1000)) // 1500L

    // supposed to be empty, but not
    val updatedDB = accountDB
  }
}