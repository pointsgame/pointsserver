package net.pointsgame.paper

final case class Vector2D[T](width: Int, vector: Vector[T]) extends Iterable[T] {
  assert(vector.nonEmpty, "Vector2D: Vector is empty.")
  assert(vector.size % width == 0, "Vector2D: Vector length is not divisible by width.")
  val height: Int = vector.size / width
  private def toIndex(x: Int, y: Int): Int =
    y * width + x
  private def toX(idx: Int): Int =
    idx % width
  private def toY(idx: Int): Int =
    idx / width
  def apply(x: Int, y: Int): T =
    vector(toIndex(x, y))
  def map[U](f: T => U): Vector2D[U] =
    Vector2D(width, vector.map(f))
  def mapWithIndex[U](f: (Int, Int, T) => U): Vector2D[U] =
    Vector2D(width, vector.zipWithIndex.map {
      case (value, idx) => f(toX(idx), toY(idx), value)
    })
  def updated(x: Int, y: Int, elem: T): Vector2D[T] =
    Vector2D(width, vector.updated(toIndex(x, y), elem))
  override def iterator: Iterator[T] =
    vector.iterator
}

object Vector2D {
  def apply[T](rows: Vector[Vector[T]]): Vector2D[T] = {
    assert(rows.forall(_.size == rows.head.size), "Vector2D: Vectors have different sizes.")
    Vector2D(rows.head.size, rows.flatten)
  }
  def single[T](elem: T): Vector2D[T] =
    Vector2D(1, Vector(elem))
  def fill[T](width: Int, height: Int)(elem: T): Vector2D[T] =
    Vector2D(width, Vector.fill(width * height)(elem))
}
