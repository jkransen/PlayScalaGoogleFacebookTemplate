package controllers

import anorm._
import play.api._
import play.api.http._
import play.api.libs.json.JsValue
import play.api.libs.ws.WS
import play.api.mvc._
import models.User
import anorm.NotAssigned

object Google extends Controller {
  
  val clientId = "YOUR_CLIENT_ID.apps.googleusercontent.com"
  val clientSecret = "YOUR_CLIENT_SECRET"
  val redirectUrl = "http://YOUR_FULL_PATH_TO//google/oauth2callback"

  def loginGoogle = Action {
    val scope = """https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile&state=%2Fprofile&"""
    Redirect("https://accounts.google.com/o/oauth2/auth?client_id=" + clientId + "&redirect_uri=" + redirectUrl + "&response_type=code&scope=" + scope)
  }

  def strip(quoted: String): String = {
    quoted.filter(char => char != '\"')
  }

  def oauth2callback(state: String, code: String) = Action {
    val postBody = "code=" + code + "&client_id=" + clientId + "&client_secret=" + clientSecret + "&redirect_uri=" + redirectUrl + "&grant_type=authorization_code"
    val body = WS.url("https://accounts.google.com/o/oauth2/token").withHeaders("Content-Type" -> "application/x-www-form-urlencoded").post(postBody)
    val accessJson = body.await.get.json
    val accessToken = strip((accessJson \ "access_token").toString)
    val userJson = WS.url("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + accessToken).get.await.get.json
    val user = getOrCreateUser(userJson)
    Redirect(controllers.routes.Application.index).withSession("connected" -> user.email)
  }

  def getOrCreateUser(googleUser: JsValue): User = {
    def getExistingUser(email: String): User = {
      User.findUserByEmail(email)
    }
    val email = strip((googleUser \ "email").toString())
    val existingUser = getExistingUser(email)
    if (existingUser == null) {
      val fullName = strip((googleUser \ "name").toString())
      User.create(User(email, fullName))
      getExistingUser(email)
    } else {
      existingUser
    }
  }
}
