package mainFile

import com.rabbitmq.client.{Connection, ConnectionFactory}
import com.typesafe.config.ConfigFactory

import scala.annotation.tailrec
import scala.util.control.Breaks.{break, breakable}

object RabbitMQ_initConnection {
  
  var Connection:Connection = null
  
  def connection():Connection ={
    val config = ConfigFactory.load("service_app.conf")

    val DbName = config.getString("service.dbName")
    val host = config.getString("service.host")
    val serviceName = config.getString("service.serviceName")
    var hostName = ""
    
    if (host == "localhost") {
      hostName = "localhost"
    }
    else if (host == "docker") {
      hostName = "rabbitmq"
    } else {
      throw new Exception("Не правильно указан режим работы для соеденения с брокером сообщений RabbitMQ!")
    }
    // Создаем соединение с RabbitMQ
    val factory = new ConnectionFactory()
    factory.setHost(hostName)
    factory.setPort(5672)
    factory.setUsername("guest")
    factory.setPassword("guest")
    var connectionAttempt = 0
    breakable {
      while (true) {
        try {
          Connection = factory.newConnection()
          return Connection
        }catch
          case ex:Exception=>{
            connectionAttempt += 1
            if (connectionAttempt == 4) {
              println("Невозможно соедениться к брокеру сообщений RabbitMQ!")
              break()
            }
            println(s"Ошибка при соеденений к брокеру сообщений RabbitMQ. Повторное попытка($connectionAttempt) соеденение через 10 секунд...")
            Thread.sleep(10000)
          }
      }
    }
    throw new Exception("Невозможно соедениться к брокеру сообщений RabbitMQ!")
  }
}
