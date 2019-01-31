import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

trait TodoRepository {
  def all():Future[Seq[Todo]]
  def done():Future[Seq[Todo]]
  def pending():Future[Seq[Todo]]
  def create(createTodo: CreateTodo):Future[Todo]
}

class InMemoryTodoRepository(initialTodos: Seq[Todo] = Seq.empty)(implicit ec: ExecutionContext) extends TodoRepository {
  private var todos: Vector[Todo] = initialTodos.toVector

  override def all(): Future[Seq[Todo]] = Future.successful(todos)

  override def done(): Future[Seq[Todo]] = Future.successful(todos.filter(_.done))

  override def pending(): Future[Seq[Todo]] = Future.successful(todos.filterNot(_.done))

  override def create(createTodo: CreateTodo): Future[Todo] = Future.successful {
    val todo = Todo(
      UUID.randomUUID().toString,
      createTodo.title,
      createTodo.description,
      done = false
    )
    todos = todos :+ todo
    todo
  }
}
