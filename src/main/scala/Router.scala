import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}

import scala.util.{Failure, Success}

trait Router {
  def route: Route
}

class TodoRouter(todoRepository: TodoRepository) extends Router with Directives {
  // libraries for JSON encoding and decoding for our models
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  /**
    * pathPrefix - matches a prefix of the remaining path after a leading slash
    * GET: /todos -> pathPrefix("todos")
    */
  override def route: Route = pathPrefix("todos") {
    // pathEndOrSingleSlash - to matches a path that has been matches completely or only consist of a leading slash
    // so it would match /todos
    pathEndOrSingleSlash {
      // get only accepts GET requests and rejects all others
      get {
        onComplete(todoRepository.all()) {
          case Success(todos) =>
            complete(todos)
          case Failure(exception) =>
            println(exception.getMessage)
            complete(ApiError.generic.statusCode, ApiError.generic.message)
        }
      }
      // ~ - chains two routes together, if the first one rejects the request, it gives the second route a chance to match it
      // path - matches the remaining path after consuming a leading slash
    } ~ path("done") {
      complete(todoRepository.done())
    } ~ path("pending") {
      complete(todoRepository.pending())
    }
  }
}