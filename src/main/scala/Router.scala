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
      } ~ path(Segment) { id: String =>
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
    }
  }
}