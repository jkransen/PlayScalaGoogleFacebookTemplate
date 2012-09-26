package controllers

import java.util.List
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.apache.commons.lang.StringUtils
import play.Logger
import play.api.libs.ws.WS
import play.api.mvc._;
import models.User
import scala.util.matching.Regex
import com.restfb.DefaultFacebookClient
import com.restfb.types._

object Facebook extends Controller {

  val APP_ID = "YOUR_FB_APP_ID"
  val APP_SECRET = "YOUR_FB_APP_SECRET"
  val redirectUrl = "http://YOUR_FULL_PATH_TO/facebook/login2"

  def login = Action {
    val url = "https://www.facebook.com/dialog/oauth?client_id=" + APP_ID + "&redirect_uri=" + redirectUrl + "&scope=email"
    Redirect(url)
  }

  def login2(code: String) = Action {
    if (StringUtils.isNotBlank(code)) {
      val accessTokenUrl = "https://graph.facebook.com/oauth/access_token?client_id=" + APP_ID + "&client_secret=" + APP_SECRET + "&code=" + code + "&redirect_uri=" + redirectUrl
      val accessTokenBody = WS.url(accessTokenUrl).get().value.get.body
      val regex = new Regex("access_token=(.*)&expires=(.*)")
      accessTokenBody match {
        case regex(accessToken, expires) => {
          val facebookClient = new DefaultFacebookClient(accessToken)
          val fbUser = facebookClient.fetchObject("me", classOf[com.restfb.types.User])
          val user = getOrCreateUser(fbUser)
          Redirect(controllers.routes.Application.index).withSession("connected" -> user.email)
        }
        case _ => {
          Ok("no match")
        }
      }
    } else {
      Redirect(controllers.routes.Facebook.login);
    }
  }

  def getOrCreateUser(fbUser: com.restfb.types.User): User = {
    val facebookUsername = fbUser.getUsername()
    val user = User.findUserByEmail(fbUser.getEmail);
    if (user == null) {
      createFacebookUser(fbUser.getEmail, facebookUsername, fbUser.getName());
    } else {
      user
    }
  }

  def createFacebookUser(email: String, facebookUsername: String, fullName: String): User = {
    User.create(User(email, fullName, facebookUsername))
    User.findUserByEmail(email)
  }
}
