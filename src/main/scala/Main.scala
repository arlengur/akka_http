import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.util.{Failure, Success}

object Main extends App {

  val host = "0.0.0.0"
  val port = 9000

  // ActorSystem creates actors and look up actors
  implicit val system: ActorSystem = ActorSystem(name = "todoapi")
  // Materializer creates streams and executes them
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  // execution context is used for futures
  import system.dispatcher

  import akka.http.scaladsl.server.Directives._

  def route = path("hello") {
    get {
      complete("Hello, Krisna!")
    }
  }

  val binding = Http().bindAndHandle(route, host, port)
  binding.onComplete {
    case Success(_) => println("Success!")
    case Failure(error) => println(s"Failed: ${error.getMessage}")
  }

  import scala.concurrent.duration._

  Await.result(binding, 3.seconds)
}
