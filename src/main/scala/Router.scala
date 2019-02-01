import akka.http.scaladsl.server.{Directives, Route}

trait Router {
  def route: Route
}

class TodoRouter(todoRepository: TodoRepository) extends Router with Directives with TodoDirectives with ValidatorDirectives {
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
        handleWithGeneric(todoRepository.all()) { todos =>
          complete(todos)
        }
      } ~ post {
        entity(as[CreateTodo]) { createTodo =>
          validateWith(CreateTodoValidator)(createTodo) {
            handleWithGeneric(todoRepository.save(createTodo)) { todos =>
              complete(todos)
            }
          }
        }
      }
      // ~ - chains two routes together, if the first one rejects the request, it gives the second route a chance to match it
      // path - matches the remaining path after consuming a leading slash
    } ~ path("done") {
      handleWithGeneric(todoRepository.done()) {
        todos => complete(todos)
      }
    } ~ path("pending") {
      handleWithGeneric(todoRepository.pending()) {
        todos => complete(todos)
      }
    } ~ path(Segment) { id: String =>
      /*
       This route has to be outside the directive `pathEndOrSingleSlash`,
       because it only matches paths that are matched completely, so if the request has anything
       after the last / it won't match, for example, it will match `/todos/` or `/todos`,
       but it won't match `/todos/1`.

       For reference, it's documentation says:

          Only passes on the request to its inner route if the request path has been matched
          completely or only consists of exactly one remaining slash.
        */
      put {
        entity(as[UpdateTodo]) { updateTodo =>
          validateWith(UpdateTodoValidator)(updateTodo) {
            handle(todoRepository.update(id, updateTodo)) {
              case TodoRepository.TodoNotFound(_) =>
                ApiError.todoNotFound(id)
              case _ =>
                ApiError.generic
            } { todo =>
              complete(todo)
            }
          }
        }
      }
    }
  }
}
