import spray.json._
import spray.json.DefaultJsonProtocol._

// TODO: JSON распарсить ету жолы Spray арқылы
val s: String =
  """
    |{
    |"age":18,
    |"name":"Jhon",
    |"body":{
    |   "wight" : 55.6
    | }
    |}""".stripMargin
// Расспарсить етіп алу
val json: JsValue = s.parseJson
// Поля значениелерін сөздік типте алып алу
val cort: Map[String, JsValue] = json.asJsObject.fields
// Обычный поля алу керек болсы осылай
val name = cort("name").convertTo[String]
// Ішінен тағы обджект бар болса осылай алып
val body = cort("body").asJsObject.fields
// Ішіндегі заттарды ала беред
body("wight").convertTo[Float]


