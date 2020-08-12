import google._
import javax.mail.internet._
import javax.mail._

object SendSimpleEmail extends App {
  val token = Setup.getToken()
  val auth = OAuth2Authenticator.connectToSmtp("smtp.gmail.com", 587, token.email, token.encoded, true)
  val transport = auth.transport
  val session = auth.session

  transport.issueCommand(s"AUTH XOAUTH2 ${token.encoded}", 235)

  val message = new MimeMessage(session)
  message.setFrom(new InternetAddress(token.email))
  message.addRecipient(Message.RecipientType.TO, new InternetAddress(Setup.Recipient1))
  message.setSubject("Some subject")
  message.setText("Some message")
  transport.sendMessage(message, message.getAllRecipients)

}
