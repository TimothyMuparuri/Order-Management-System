package za.co.nharire.order_ms.config.email;

// Java Program to Illustrate Creation Of
// Service Interface
// Importing required classes

// Interface
public interface EmailRepository {

    // Method
    // To send a simple email
    String sendSimpleMail(EmailDetails details);

    // Method
    // To send an email with attachment
    String sendMailWithAttachment(EmailDetails details);

}



