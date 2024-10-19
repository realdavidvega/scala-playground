package content

object KafkaStreams {

  object Domain {
    type UserId = String
    type Profile = String
    type Product = String
    type OrderId = String

    case class Order(orderId: OrderId, userId: UserId, products: List[Product], amount: Double)
  }

  def main(args: Array[String]): Unit = {

  }
}
