package controllers

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import repository.CarsRepoImpl
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class CarsController @Inject()(val reactiveMongoApi: ReactiveMongoApi)  extends Controller
  with MongoController with ReactiveMongoComponents {

  import controllers.CarFields._
  def carsRepo = new CarsRepoImpl(reactiveMongoApi)

  def index = Action.async { implicit request =>
    carsRepo.find().map(cars => Ok(Json.toJson(cars)))
  }

  def create = Action.async(BodyParsers.parse.json) { implicit request =>
    val name = (request.body \ Name).as[String]
    val description = (request.body \ Description).as[String]
    val user = (request.body \ User).as[String]
    carsRepo.save(BSONDocument(
      Name -> name,
      Description -> description,
      User -> user
    )).map(result => Created)
  }

  def read(id: String) = Action.async { implicit request =>
    carsRepo.select(BSONDocument(id -> BSONObjectID(id))).map(car => Ok(Json.toJson(car)))
  }

  def update(id: String) = Action.async(BodyParsers.parse.json) { implicit request =>
    val name = (request.body \ Name).as[String]
    val description = (request.body \ Description).as[String]
    val user = (request.body \ User).as[String]
    carsRepo.update(BSONDocument(Id -> BSONObjectID(id)),
      BSONDocument("$set" -> BSONDocument(Name -> name, Description -> description, User -> user)))
      .map(result => Accepted)
  }

  def delete(id: String) = Action.async {
    carsRepo.remove(BSONDocument(id -> BSONObjectID(id)))
    .map(result => Accepted)
  }
}

object CarFields {
  val Id = "_id"
  val Name ="name"
  val Description = "description"
  val User = "user"
}


