package net.simforge.commons.legacy.misc;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;

public class Mailer {

    public static void sendPlainText(String address, String caption, String body) throws MessagingException {
        String mailHost = Settings.get("mailer.smtp"); // smtp.something.com
        String mailFrom = Settings.get("mailer.from"); // somebody@something.com
        String mailPwd = Settings.get("mailer.password"); // mySecretPassword

        Properties props = new Properties();

        props.put("mail.smtp.host", mailHost);
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(props);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailFrom));
        message.addRecipients(Message.RecipientType.TO, address);
        message.setSubject(caption);
        message.setSentDate(new Date());

        MimeMultipart content = new MimeMultipart();
        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setContent(body, "text/plain; charset=UTF-8");
        content.addBodyPart(textBodyPart);

        message.setContent(content);
        message.saveChanges();

        Transport transp = session.getTransport("smtp");
        transp.connect(mailHost, mailFrom, mailPwd);
        transp.sendMessage(message, message.getAllRecipients());
        transp.close();
    }
}
