package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def swagger = Action {
    request =>
      Ok(views.html.swagger())
  }

  /*def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }*/

}