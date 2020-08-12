import java.io.InputStreamReader
import java.util.Base64

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.gmail.{Gmail, GmailScopes}

import scala.jdk.CollectionConverters.IterableHasAsJava

final case class Token(email: String, accessToken: String) {
  def encoded =  {
    val fullToken = s"user=$email\u0001auth=Bearer $accessToken\u0001\u0001"
    Base64.getEncoder.encodeToString(fullToken.getBytes)
  }
}

object Setup {
  val JsonFactory = JacksonFactory.getDefaultInstance
  val AppName = "OAuthTest"
  val CredentialsPath = "/credentials.json"

  val Scopes = List(GmailScopes.MAIL_GOOGLE_COM)

  // change these
  val Recipient1 = "test1@gmail.com"
  val Recipient2 = "test2@gmail.com"

  def getCredentials(transport: NetHttpTransport) = {
    val in = Option(getClass.getResourceAsStream(CredentialsPath)).getOrElse(throw new Exception(s"Failed to find credentials"))
    val secrets = GoogleClientSecrets.load(JsonFactory, new InputStreamReader(in))
    val flow = new GoogleAuthorizationCodeFlow.Builder(transport, JsonFactory, secrets, Scopes.asJavaCollection).setAccessType("offline").build()
    val receiver = new LocalServerReceiver.Builder().setPort(8888).build()
    new AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
  }

  def getToken() = {
    val transport = GoogleNetHttpTransport.newTrustedTransport()
    val credentials = getCredentials(transport)
    val token = credentials.getAccessToken

    // get email from service
    val service = new Gmail.Builder(transport, JsonFactory, credentials).setApplicationName(AppName).build()
    val email = service.users().getProfile("me").execute().getEmailAddress

    Token(email, token)
  }
}
