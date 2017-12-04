package controllers

import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

import io.swagger.annotations.{ApiParam, ApiResponse, ApiResponses}
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import repository.CarsRepoImpl
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.time._
import java.time.format.DateTimeFormatter

import scala.concurrent.Future

class CarsController @Inject()(val reactiveMongoApi: ReactiveMongoApi)  extends Controller
  with MongoController with ReactiveMongoComponents {

  import controllers.CarFields._
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def carsRepo = new CarsRepoImpl(reactiveMongoApi)

  def index (orderBy:Option[String]) = Action.async { implicit request =>
    carsRepo.find(orderBy).map(cars => Ok(Json.toJson(cars)))
  }

  def create = Action.async(BodyParsers.parse.json) { implicit request =>
    val title = (request.body \ Title).as[String]
    val fuel = (request.body \ Fuel).as[String]
    val price = (request.body \ Price).as[Int]
    val newCar = (request.body \ NewCar).as[Boolean]
    val mileage = (request.body \ Mileage).asOpt[Int]
    val firstRegistration = (request.body \ FirstRegistration).asOpt[String]
    if(validateData(newCar, mileage, firstRegistration)) {
      carsRepo.save(BSONDocument(
        Title -> title,
        Fuel -> fuel,
        Price -> price,
        NewCar -> newCar,
        Mileage -> mileage.get,
        FirstRegistration -> new SimpleDateFormat("yyyy-MM-dd").parse(firstRegistration.get)
      )).map(result => Created)
    } else {
      Future(BadRequest)
    }
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
    val price = (request.body \ Price).as[Int]
    val newCar = (request.body \ NewCar).as[Boolean]
    val mileage = (request.body \ Mileage).asOpt[Int]
    val firstRegistration = (request.body \ FirstRegistration).asOpt[String]
    if(validateData(newCar, mileage, firstRegistration)) {
      carsRepo.update(BSONDocument(Id -> BSONObjectID(id)),
        BSONDocument("$set" -> BSONDocument(Title -> title, Fuel -> fuel, Price -> price, NewCar -> newCar, Mileage -> mileage.get, FirstRegistration -> new SimpleDateFormat("yyyy-MM-dd").parse(firstRegistration.get))))
          .map(result => Accepted)
    } else {
      Future(BadRequest)
    }
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

  def validateData(newCar:Boolean,  mileage:Option[Int], firstRegistration:Option[String]): Boolean = {
    newCar match {
      case true=> if(mileage != None || firstRegistration != None) false else true
      case _ => true
    }
  }
}


