import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class TodoRouterListSpec extends WordSpec with Matchers with ScalatestRouteTest with TodoMocks {
  // libraries for JSON encoding and decoding for our models
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  private val doneTodo = Todo("2", "Temple", "Go to the temple", done = false)
  private val pendingTodo = Todo("3", "MA", "Go to the mangala-arati", done = true)
  private val todos = Seq(doneTodo, pendingTodo)

  "TodoRouter" should {
    "return all the todos" in {
      val repository = new InMemoryTodoRepository(todos)
      val router = new TodoRouter(repository)

      // send GET request
      Get("/todos") ~> router.route -> check {
        // we expect response 200
        status shouldBe StatusCodes.OK
        val response = responseAs[Seq[Todo]]
        // check response content
        response shouldBe todos
      }
    }

    "return all the done todos" in {
      val repository = new InMemoryTodoRepository(todos)
      val router = new TodoRouter(repository)

      // send GET request
      Get("/todos/done") ~> router.route -> check {
        // we expect response 200
        status shouldBe StatusCodes.OK
        val response = responseAs[Seq[Todo]]
        // check response content
        response shouldBe doneTodo
      }
    }

    "return all the pending todos" in {
      val repository = new InMemoryTodoRepository(todos)
      val router = new TodoRouter(repository)

      // send GET request
      Get("/todos/pending") ~> router.route -> check {
        // we expect response 200
        status shouldBe StatusCodes.OK
        val response = responseAs[Seq[Todo]]
        // check response content
        response shouldBe pendingTodo
      }
    }

    "handle repository failure in the todos route" in {
      val repository = new FailingRepository
      val router = new TodoRouter(repository)

      // send GET request
      Get("/todos") ~> router.route -> check {
        // we expect response 500
        status shouldBe StatusCodes.InternalServerError
      }
    }

    "handle repository failure in the done route" in {
      val repository = new FailingRepository
      val router = new TodoRouter(repository)

      // send GET request
      Get("/todos/done") ~> router.route -> check {
        // we expect response 500
        status shouldBe StatusCodes.InternalServerError
      }
    }

    "handle repository failure in the pending route" in {
      val repository = new FailingRepository
      val router = new TodoRouter(repository)

      // send GET request
      Get("/todos/pending") ~> router.route -> check {
        // we expect response 500
        status shouldBe StatusCodes.InternalServerError
      }
    }
  }

}
