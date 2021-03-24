package com.kubukoz.ho2

import io.circe.DecodingFailure
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import io.circe.literal._

class ClientCredentialsAccessTokenResponseDeserializationSpec extends AnyFlatSpec with Matchers with EitherValues {

  "token response JSON" should "be deserialized to proper case class" in {
    val json =
      // language=JSON
      json"""{
            "access_token": "TAeJwlzT",
            "domain": "zoo",
            "expires_in": 2399,
            "scope": "secondapp",
            "token_type": "Bearer"
        }"""

    val response = json.as[ClientCredentialsToken.AccessTokenResponse]
    response shouldBe Right(
      ClientCredentialsToken.AccessTokenResponse(
        accessToken = Secret("TAeJwlzT"),
        domain = "zoo",
        expiresIn = 2399.seconds,
        scope = "secondapp"
      )
    )
  }

  "Token with wrong type" should "not be deserialized" in {
    val json =
      // language=JSON
      json"""{
            "access_token": "TAeJwlzT",
            "domain": "zoo",
            "expires_in": 2399,
            "scope": "secondapp",
            "token_type": "BearerToken"
        }"""

    json.as[ClientCredentialsToken.AccessTokenResponse].left.value shouldBe a[DecodingFailure]
  }

}
