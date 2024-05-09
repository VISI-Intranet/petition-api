package amqp

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.rabbitmq.client.*
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable

class AmqpActor(connection: Connection,exchangeName: String,serviceName:String) extends Actor with ActorLogging {
  private var channel: Channel = _

  private var senderActor : ActorRef = _
  private var askerActor : ActorRef = _

  val config: Config = ConfigFactory.load("service_app.conf")

  val host: String = config.getString("service.host")
  var hostName = ""
  

  override def preStart(): Unit = {

    channel = connection.createChannel()
    log.info("Соединение с RabbitMQ установлено")
    channel.exchangeDeclare(exchangeName,"topic")

    senderActor = context.actorOf(SenderActor.props(channel,exchangeName,serviceName), "sender")
    askerActor = context.actorOf(AskActor.props(channel,exchangeName, serviceName), "asker")
  }


  override def receive: Receive = {
    case msg@RabbitMQ.Tell(routingKey, content) =>
      senderActor forward msg

    case msg@RabbitMQ.Answer(routingKey,correlationId,content)=>
      senderActor forward msg

    case msg@RabbitMQ.Ask(routingKey, content) =>
      // Запрос передаем дальше
      askerActor forward msg

    case RabbitMQ.DeclareListener(queue,bind_routing_key,actorName, handle) => {
      // Создаем для актора новый канал
      val chanelForReceiver = connection.createChannel()
      // Создаем актора слушателья
      val receiverActor = context.actorOf(ReceiverActor.props(chanelForReceiver, queue, exchangeName, bind_routing_key, handle), actorName)
    }
  }


  override def postStop(): Unit = {
    // Закрываем соединение с RabbitMQ
    channel.close()
    connection.close()

    log.info("Соединение с RabbitMQ закрыто")
  }
}
