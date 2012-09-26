package models
 
import play.api.db._
import play.api.Play.current
 
import anorm._
import anorm.SqlParser._
import anorm.NotAssigned
 
case class User(email: String, fullName: String, facebookUsername: String = null, id: Pk[Long] = NotAssigned)

object User {
 
  val simple = {
    get[Pk[Long]]("id") ~
    get[String]("email") ~
    get[Option[String]]("facebookUsername") ~
    get[String]("fullName") map {
      case id~email~facebookUsername~fullName => User(email, fullName, facebookUsername.getOrElse(null), id)
    }
  }
 
  def findAll(): Seq[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from Users").as(User.simple *)
    }
  }
  
  def findUserByEmail(email: String): User = {
    val users = findAll.filter(_.email.equals(email))
    if (!users.isEmpty) users.head else null
  }
 
  def create(user: User) {
    DB.withConnection { implicit connection =>
      SQL("insert into Users(email, facebookUsername, fullName) values ({email}, {facebookUsername}, {fullName})").on(
        "email" -> user.email, "facebookUsername" -> user.facebookUsername, "fullName" -> user.fullName
      ).executeUpdate()
    }
  }
}