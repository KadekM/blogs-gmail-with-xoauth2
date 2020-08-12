import google._
import javax.mail._
import javax.mail.internet._


object SendWithFixedMessageId extends App {
  class FixedMessageIdMimeMessage(session: Session, msgId: String) extends MimeMessage(session: Session) {
    override def updateMessageID(): Unit = {
      setHeader("Message-ID", msgId)
    }
  }

  val token = Setup.getToken()
  val auth = OAuth2Authenticator.connectToSmtp("smtp.gmail.com", 587, token.email, token.encoded, true)
  val transport = auth.transport
  val session = auth.session

  transport.issueCommand(s"AUTH XOAUTH2 ${token.encoded}", 235)

  val msg1 = new FixedMessageIdMimeMessage(session, "my-msg-id")
  msg1.setFrom(new InternetAddress(token.email))
  msg1.setHeader("To", s"s${Setup.Recipient1}, ${Setup.Recipient2}")
  msg1.setSubject("Some subject")
  msg1.setText("Test message")
  transport.sendMessage(msg1, List(new InternetAddress(Setup.Recipient1)).toArray);

}
