import controllers.{CarsController, routes}
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Result, _}
import play.api.test.Helpers._
import play.api.test.{WithApplication, _}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.LastError
import reactivemongo.bson.BSONDocument
import repository.CarsRepoImpl

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification with Results with Mockito {

  val mockRecipeRepo = mock[CarsRepoImpl]
  val reactiveMongoApi = mock[ReactiveMongoApi]
  val documentId = "56a0ddb6c70000c700344254"
  val lastRequestStatus = new LastError(true, None, None, None, 0, None, false, None, None, false, None, None)

  val carOne = Json.obj(
    "name" -> "car One",
    "description" -> "My first car",
    "user" -> "amit"
  )

  val posts = List(
    carOne,
    Json.obj(
      "name" -> "car Two",
      "description" -> "My second car",
      "user" -> "amit"
    ))

  class TestController() extends CarsController(reactiveMongoApi) {
    override def carsRepo: CarsRepoImpl = mockRecipeRepo
  }

  val controller = new TestController()

  "Application" should {

    "send 404 on a bad request" in {
      new WithApplication() {
        route(FakeRequest(GET, "/boum")) must beSome.which(status(_) == NOT_FOUND)
      }
    }

    "Cars#delete" should {
      "remove recipe" in {
        mockRecipeRepo.remove(any[BSONDocument])(any[ExecutionContext]) returns Future(lastRequestStatus)

        val result: Future[Result] = controller.delete(documentId).apply(FakeRequest())

        status(result) must be equalTo ACCEPTED
        there was one(mockRecipeRepo).remove(any[BSONDocument])(any[ExecutionContext])
      }
    }

    "Cars#list" should {
      "list Cars" in {
        mockRecipeRepo.find()(any[ExecutionContext]) returns Future(posts)

        val result: Future[Result] = controller.index().apply(FakeRequest())

        contentAsJson(result) must be equalTo JsArray(posts)
        there was one(mockRecipeRepo).find()(any[ExecutionContext])
      }
    }

    "Cars#read" should {
      "read recipe" in {
        mockRecipeRepo.select(any[BSONDocument])(any[ExecutionContext]) returns Future(Option(carOne))

        val result: Future[Result] = controller.read(documentId).apply(FakeRequest())

        contentAsJson(result) must be equalTo carOne
        there was one(mockRecipeRepo).select(any[BSONDocument])(any[ExecutionContext])
      }
    }

    "Cars#create" should {
      "create recipe" in {
        mockRecipeRepo.save(any[BSONDocument])(any[ExecutionContext]) returns Future(lastRequestStatus)

        val request = FakeRequest().withBody(carOne)
        val result: Future[Result] = controller.create()(request)

        status(result) must be equalTo CREATED
        there was one(mockRecipeRepo).save(any[BSONDocument])(any[ExecutionContext])
      }
    }

    "Cars#update" should {
      "update recipe" in {
        mockRecipeRepo.update(any[BSONDocument], any[BSONDocument])(any[ExecutionContext]) returns Future(lastRequestStatus)

        val request = FakeRequest().withBody(carOne)
        val result: Future[Result] = controller.update(documentId)(request)

        status(result) must be equalTo ACCEPTED
        there was one(mockRecipeRepo).update(any[BSONDocument], any[BSONDocument])(any[ExecutionContext])
      }
    }
  }
}