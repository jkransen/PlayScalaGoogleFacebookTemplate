package controllers

import play.api.mvc.Action
import play.api.mvc.Controller

import models.User

object Application extends Controller {

  def index = Action {
    request =>
      request.session.get("connected").map { email =>
        val user = User.findUserByEmail(email);
        Ok(views.html.index("Hello " + user.fullName))
      }.getOrElse {
        Ok(views.html.index("Not logged in"))
      }
  }

  def login = Action {
    Ok(views.html.login())
  }

  def logout = Action {
    Redirect(controllers.routes.Application.index).withNewSession
  }
}