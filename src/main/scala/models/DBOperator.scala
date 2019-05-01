package models

import doobie._

trait DBOperator[A] {
  def field : Fragment
}
