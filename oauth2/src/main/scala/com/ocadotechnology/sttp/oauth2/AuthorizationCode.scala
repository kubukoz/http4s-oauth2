package com.kubukoz.ho2

import cats.effect.Concurrent
import cats.implicits._
import org.http4s.Method.POST
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

object AuthorizationCode {

  private def prepareLoginLink(baseUri: Uri, clientId: String, redirectUri: String, state: String, scopes: Set[String]): Uri =
    (baseUri / "login")
      .withQueryParam("response_type", "code")
      .withQueryParam("client_id", clientId)
      .withQueryParam("redirect_uri", redirectUri)
      .withQueryParam("state", state)
      .withQueryParam("scope", scopes.mkString(" "))

  private def prepareLogoutLink(baseUri: Uri, clientId: String, redirectUri: String): Uri =
    baseUri
      .addPath("logout")
      .withQueryParam("client_id", clientId)
      .withQueryParam("redirect_uri", redirectUri)

  private def convertAuthCodeToUser[F[_]: Concurrent, UriType](
    tokenUri: Uri,
    authCode: String,
    redirectUri: String,
    clientId: String,
    clientSecret: Secret[String]
  )(
    implicit client: Client[F]
  ): F[Oauth2TokenResponse] = {
    object dsl extends Http4sClientDsl[F]
    import dsl._

    client
      .expect[Oauth2TokenResponse] {
        POST(tokenUri)
          .withEntity(tokenRequestParams(authCode, redirectUri, clientId, clientSecret.value))
      }
  }

  private def tokenRequestParams(authCode: String, redirectUri: String, clientId: String, clientSecret: String) =
    Map(
      "grant_type" -> "authorization_code",
      "client_id" -> clientId,
      "client_secret" -> clientSecret,
      "redirect_uri" -> redirectUri,
      "code" -> authCode
    )

  private def performTokenRefresh[F[_]: Concurrent, UriType](
    tokenUri: Uri,
    refreshToken: String,
    clientId: String,
    clientSecret: Secret[String],
    scopeOverride: ScopeSelection
  )(
    implicit client: Client[F]
  ): F[Oauth2TokenResponse] = {
    object dsl extends Http4sClientDsl[F]
    import dsl._

    client
      .expect[RefreshTokenResponse](
        POST(tokenUri)
          .withEntity(refreshTokenRequestParams(refreshToken, clientId, clientSecret.value, scopeOverride.toRequestMap))
      )
      .map(_.toOauth2Token(refreshToken))
  }

  private def refreshTokenRequestParams(refreshToken: String, clientId: String, clientSecret: String, scopeOverride: Map[String, String]) =
    Map(
      "grant_type" -> "refresh_token",
      "refresh_token" -> refreshToken,
      "client_id" -> clientId,
      "client_secret" -> clientSecret
    ) ++ scopeOverride

  def loginLink[F[_]](
    baseUrl: Uri,
    redirectUri: Uri,
    clientId: String,
    state: Option[String] = None,
    scopes: Set[String] = Set.empty
  ): Uri =
    prepareLoginLink(baseUrl, clientId, redirectUri.toString, state.getOrElse(""), scopes)

  def authCodeToToken[F[_]: Client: Concurrent](
    tokenUri: Uri,
    redirectUri: Uri,
    clientId: String,
    clientSecret: Secret[String],
    authCode: String
  ): F[Oauth2TokenResponse] =
    convertAuthCodeToUser(tokenUri, authCode, redirectUri.toString, clientId, clientSecret)

  def logoutLink[F[_]](
    baseUrl: Uri,
    redirectUri: Uri,
    clientId: String,
    postLogoutRedirect: Option[Uri]
  ): Uri =
    prepareLogoutLink(baseUrl, clientId, postLogoutRedirect.getOrElse(redirectUri).toString())

  def refreshAccessToken[F[_]: Client: Concurrent](
    tokenUri: Uri,
    clientId: String,
    clientSecret: Secret[String],
    refreshToken: String,
    scopeOverride: ScopeSelection = ScopeSelection.KeepExisting
  ): F[Oauth2TokenResponse] =
    performTokenRefresh(tokenUri, refreshToken, clientId, clientSecret, scopeOverride)

}
