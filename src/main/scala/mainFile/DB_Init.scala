package mainFile

import com.typesafe.config.ConfigFactory
import domain.*
import slick.jdbc
import slick.jdbc.MySQLProfile
import slick.jdbc.MySQLProfile.api.*

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.concurrent.duration.*
import scala.util.control.Breaks.breakable

object DB_Init {
  def create_tables()(implicit ex:ExecutionContext):slick.jdbc.MySQLProfile.backend.JdbcDatabaseDef = {
    val config = ConfigFactory.load("service_app.conf")

    val DbName = config.getString("service.dbName")
    val host = config.getString("service.host")
    val serviceName = config.getString("service.serviceName")
    var hostName = ""

    if (host=="localhost") {
      hostName = "localhost"
    }
    else if(host == "docker") {
      hostName = "mysql"
    }else{
      throw new Exception("Не правильно указан работы для работы с базой данных!")
    }


    var connectionAttempt = 0
    breakable {
      while (true) {
        try {
          val db: jdbc.MySQLProfile.backend.JdbcDatabaseDef = Database.forURL(
            url = s"jdbc:mysql://$hostName:3306", // MySQL дың портын көрсету керек
            user = "root",
            password = "root",
            driver = "com.mysql.cj.jdbc.Driver"
          )

          val dbExistsQuery = sql"""
          SELECT SCHEMA_NAME
          FROM INFORMATION_SCHEMA.SCHEMATA
          WHERE SCHEMA_NAME = $DbName
        """.as[String]

          // Выполняем запрос
          val dbExistsResult: Future[Seq[String]] = db.run(dbExistsQuery)

          val dbNames = Await.result(dbExistsResult, 5 seconds)

          if (dbNames.contains("petition")) {
            println(s"Связь с базой данных $DbName установлен!")

            val petitionDB: MySQLProfile.backend.JdbcDatabaseDef = Database.forURL(
              url = s"jdbc:mysql://$hostName/petition", // MySQL дың портын көрсету керек
              user = "root",
              password = "root",
              driver = "com.mysql.cj.jdbc.Driver"
            )
            return petitionDB
          } else {
            val newsTable = TableQuery[Petitions]
            val styleTable = TableQuery[UserTable]
            val tagTable = TableQuery[Comments]
            val viewedUsersTable = TableQuery[PetitionVotings]

            val setupAction = DBIO.seq(
              sqlu"CREATE DATABASE IF NOT EXISTS petition",
              sqlu"USE petition",
              (newsTable.schema ++ styleTable.schema ++ tagTable.schema ++ viewedUsersTable.schema).create
            )
            try {
              Await.result(db.run(setupAction), 5 seconds)
              println(s"Создана новая база данных по имени $DbName и таблицы внутри нее!")
            }
            catch {
              case ex: Exception =>
                println(s"Ошибка при созданий базы и таблиц: ${ex.getMessage}")
            }

            val petitionDB = Database.forURL(
              url = s"jdbc:mysql://$hostName/petition", // MySQL дың портын көрсету керек
              user = "root",
              password = "root",
              driver = "com.mysql.cj.jdbc.Driver"
            )
            return petitionDB
          }
        } catch {
          case ex: Exception =>
            connectionAttempt+=1
            if(connectionAttempt == 4){
              println("Невозможно соедениться к базе данных!")
              throw new Exception("Невозможно соедениться к базе данных!")
            }
            println(s"Ошибка при соеденений с базой данных. Повторное попытка($connectionAttempt) соеденение через 10 секунд...")
            Thread.sleep(10000)
        }
      }
    }

    null.asInstanceOf[MySQLProfile.backend.JdbcDatabaseDef]
  }
}
