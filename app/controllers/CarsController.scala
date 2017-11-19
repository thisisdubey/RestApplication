package controllers

import java.util.Date
import javax.inject.Inject

import io.swagger.annotations.{ApiParam, ApiResponse, ApiResponses}
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
    val title = (request.body \ Title).as[String]
    val fuel = (request.body \ Fuel).as[String]
    val price = (request.body \ Price).as[String]
    val newCar = (request.body \ NewCar).as[String]
    val mileage = (request.body \ Mileage).as[String]
    val firstRegistration = (request.body \ FirstRegistration).as[String]
    carsRepo.save(BSONDocument(
      Title -> title,
      Fuel -> fuel,
      Price -> price,
      NewCar -> newCar,
      Mileage -> mileage,
      FirstRegistration -> FirstRegistration
    )).map(result => Created)
  }
 /* @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid ID supplied"),
    new ApiResponse(code = 404, message = "Pet not found")))
  def read(@ApiParam(value = "ID of the pet to fetch") id: String) = Action.async { implicit request =>
    carsRepo.select(BSONDocument(Id -> BSONObjectID(id))).map(car => Ok(Json.toJson(car)))
  }*/

  def read(id: String) = Action.async { implicit request =>
    carsRepo.select(BSONDocument(Id -> BSONObjectID(id))).map(car => Ok(Json.toJson(car)))
  }

  def update(id: String) = Action.async(BodyParsers.parse.json) { implicit request =>
    val title = (request.body \ Title).as[String]
    val fuel = (request.body \ Fuel).as[String]
    val price = (request.body \ Price).as[String]
    val newCar = (request.body \ NewCar).as[String]
    val mileage = (request.body \ Mileage).as[String]
    val firstRegistration = (request.body \ FirstRegistration).as[String]
    carsRepo.update(BSONDocument(Id -> BSONObjectID(id)),
      BSONDocument("$set" -> BSONDocument(Title -> title, Fuel -> fuel, Price -> price, NewCar -> newCar, Mileage -> mileage, FirstRegistration -> firstRegistration)))
      .map(result => Accepted)
  }

  def delete(id: String) = Action.async {
    carsRepo.remove(BSONDocument(Id -> BSONObjectID(id)))
    .map(result => Accepted)
  }
}

object CarFields {
  val Id = "_id"
  val Title ="title"
  val Fuel = "fuel"
  val Price = "price"
  val NewCar = "newCar"
  val Mileage = "mileage"
  val FirstRegistration = "firstRegistration"

}


