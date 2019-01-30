import akka.actor.ActorSystem
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

  val todoRepository = new InMemoryTodoRepository(Seq(
    Todo("1", "Read SB", "Get the book and read", done = false),
    Todo("2", "Temple", "Go to the temple", done = false)
  ))
  val router = new TodoRouter(todoRepository)
  val server = new Server(router, host, port)

  val binding = server.bind()
  binding.onComplete {
    case Success(_) => println("Success!")
    case Failure(error) => println(s"Failed: ${error.getMessage}")
  }

  import scala.concurrent.duration._

  Await.result(binding, 3.seconds)
}
