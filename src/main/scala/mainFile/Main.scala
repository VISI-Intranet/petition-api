package mainFile

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import slick.jdbc.MySQLProfile

import scala.concurrent.ExecutionContextExecutor
import repositories.*
import routes.*
import amqp.{AmqpActor, RabbitMQ}
import com.typesafe.config.ConfigFactory
import routing.RabbitMQ_Consumer
import slick.jdbc

import scala.language.postfixOps


object Main extends App with JsonSupport {
  val config = ConfigFactory.load("service_app.conf")

  // Извлечение значения параметра serviceName
  val serviceName = config.getString("service.serviceName")
  val exchangeName = config.getString("service.exchangeName")
  private val port = config.getString("service.port").toInt

  // Создание акторной системы
  implicit val system: ActorSystem = ActorSystem(serviceName)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Создание соеденения с базой данных
  implicit val db: jdbc.MySQLProfile.backend.JdbcDatabaseDef = DB_Init.create_tables()


  implicit val userRepo: UserRepository = UserRepository()
  implicit val petitionVotingRepo: PetitionVotingRepository = PetitionVotingRepository()
  implicit val petitionRepo: PetitionRepository = PetitionRepository()
  implicit val commentRepo: CommentRepository = CommentRepository()

  //Создание соеденения с брокером сообщений

  val connection = RabbitMQ_initConnection.connection()
  
  // Создание актора для брокера сообщений
  val amqpActor = system.actorOf(Props(new AmqpActor(connection,exchangeName, serviceName)), "amqpActor")

  // Обявить актора слушателя
  amqpActor ! RabbitMQ.DeclareListener(
    queue = "petition_api_queue",
    bind_routing_key = "univer.petition_api.#",
    actorName = "consumer_actor_1",
    handle = new RabbitMQ_Consumer().handle)
  

  // Создайнеи обьектов роутинга
  private val userRoute = UserRoute()
  private val petitionRoute = PetitionRoute()
  private val commentRoute = CommentRoute()
  private val petitionVotingRoute = PetitionVotingRoute()


  // Добавление путей
  private val allRoutes = userRoute.route ~ 
                          petitionRoute.route ~
                          commentRoute.route ~
                          petitionVotingRoute.route
  
  // Старт сервера
  private val bindingFuture = Http().newServerAt("0.0.0.0", port).bind(allRoutes)
  
  println(s"Server online at http://0.0.0.0:$port/")

  // Остановка сервера при завершении приложения
  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => {
        println("Остановка сервера и закрытие соеденений...")
        db.close()
        connection.close()
        system.terminate()
      })
  }
}
