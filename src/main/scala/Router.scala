import akka.http.scaladsl.server.{Directives, Route}

trait Router {
  def route: Route
}

class TodoRouter(todoRepository: TodoRepository) extends Router with Directives {
  // libraries for JSON encoding and decoding for our models
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  override def route: Route = pathPrefix("todos") {
    // pathEndOrSingleSlash - to match the base route
    pathEndOrSingleSlash {
      get {
        complete(todoRepository.all())
      }
    }
  }
}