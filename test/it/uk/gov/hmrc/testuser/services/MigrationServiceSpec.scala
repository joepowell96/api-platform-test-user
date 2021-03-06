/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.uk.gov.hmrc.testuser.services

import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.testuser.models._
import uk.gov.hmrc.testuser.repository.TestUserMongoRepository
import uk.gov.hmrc.testuser.services.MigrationService

import scala.concurrent.ExecutionContext.Implicits.global

class MigrationServiceSpec extends UnitSpec with MongoSpecSupport with WithFakeApplication with BeforeAndAfterEach {

  val repository = new TestUserMongoRepository()

  trait Setup {
    val underTest = new MigrationService {
      override lazy val mongoConnector = mongoConnectorForTest
    }

    val jsonCollection = repository.collection
  }

  override def beforeEach() {
    await(repository.drop)
  }

  "migrate" should {

    "add userFullName and emailAddress in existing records with random values" in new Setup {

      await(jsonCollection.save(Json.parse(
        """
          |{
          | "userId": "userId",
          | "password": "password",
          | "userType": "AGENT",
          | "services": []
          |}
        """.stripMargin
      )))

      await(underTest.migrate())

      val agent = await(repository.fetchByUserId("userId")).map(_.asInstanceOf[TestAgent]).get
      agent.userFullName should not be null
      agent.emailAddress should not be null
    }
  }
}
