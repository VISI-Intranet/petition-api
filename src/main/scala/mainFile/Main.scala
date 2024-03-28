package mainFile

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.stream.ActorMaterializer
import domain.{Comment, PetitionVoting}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api.{DBIO, Database}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import repositories.*
import routes.*
import amqp.{AmqpActor, RabbitMQ}
import com.typesafe.config.ConfigFactory
import routing.RabbitMQ_Consumer


object Main extends App with JsonSupport {
  val config = ConfigFactory.load("service_app.conf")

  // Извлечение значения параметра serviceName
  val serviceName = config.getString("service.serviceName")

  // Создание акторной системы
  implicit val system: ActorSystem = ActorSystem(serviceName)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Создание актора для брокера сообщений
  val amqpActor = system.actorOf(Props(new AmqpActor("X:routing.topic",serviceName)),"amqpActor")

  // Обявить актора слушателя
  amqpActor ! RabbitMQ.DeclareListener(
    queue =  "petition_api_queue",
    bind_routing_key =  "univer.petition_api.#",
    actorName =  "consumer_actor_1",
    handle = new RabbitMQ_Consumer().handle)

  // Создание связи с базой и обьекты репозиториев
  implicit val db: JdbcProfile#Backend#Database = Database.forConfig("slick.dbs.default")
  implicit val userRepo: UserRepository = UserRepository()
  implicit val petitionVotingRepo: PetitionVotingRepository = PetitionVotingRepository()
  implicit val petitionRepo: PetitionRepository = PetitionRepository()
  implicit val commentRepo: CommentRepository = CommentRepository()

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
  private val bindingFuture = Http().bindAndHandle(allRoutes, "localhost", 8080)
  println(s"Server online at http://localhost:8080/")

  // Остановка сервера при завершении приложения
  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
