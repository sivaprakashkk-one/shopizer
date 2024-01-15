package com.salesmanager.core.business.modules.email;

import com.postmarkapp.postmark.Postmark;
import com.postmarkapp.postmark.client.ApiClient;
import com.postmarkapp.postmark.client.data.model.message.Message;
import com.postmarkapp.postmark.client.data.model.message.MessageResponse;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.mail.MailPreparationException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component("postmarkEmailSender")
public class PostmarkEmailSenderImpl implements EmailModule {

    @Inject
    private Configuration freemarkerMailConfiguration;

    private final static String TEMPLATE_PATH = "templates/email";

    private static final String CHARSET = "UTF-8";

    private EmailConfig emailConfig;

    @Override
    public void send(Email email) throws Exception {

        final String eml = "Tire Wheel Warehouse"; //email.getFrom();
        final String from = email.getFromEmail();
        final String to = email.getTo();
        final String subject = email.getSubject();
        final String tmpl = email.getTemplateName();
        final Map<String, String> templateTokens = email.getTemplateTokens();

//        client = new MailjetClient(emailConfig.getUsername(), emailConfig.getPassword(), new ClientOptions("v3.1"));

        freemarkerMailConfiguration.setClassForTemplateLoading(DefaultEmailSenderImpl.class, "/");
        Template textTemplate = freemarkerMailConfiguration.getTemplate(
                new StringBuilder(TEMPLATE_PATH).append("/").append(tmpl).toString());
        final StringWriter textWriter = new StringWriter();
        try {
            textTemplate.process(templateTokens, textWriter);
        } catch (TemplateException e) {
            throw new MailPreparationException("Can't generate text mail", e);
        }

        freemarkerMailConfiguration.setClassForTemplateLoading(DefaultEmailSenderImpl.class, "/");
        Template htmlTemplate = freemarkerMailConfiguration.getTemplate(
                new StringBuilder(TEMPLATE_PATH).append("/").append(tmpl).toString());
        final StringWriter htmlWriter = new StringWriter();
        try {
            htmlTemplate.process(templateTokens, htmlWriter);
        } catch (TemplateException e) {
            throw new MailPreparationException("Can't gener ate HTML mail", e);
        }

        ApiClient mailClient = Postmark.getApiClient("POSTMARK_SEC");
        Message message = new Message("\"Tire wheel warehouse\" info@twwusa.store", to, subject, htmlWriter.toString(), textWriter.toString());
        message.setMessageStream("outbound");
        MessageResponse response = mailClient.deliverMessage(message);
    }

    private Map<String, String> constructHTMLMessage(String tmpl, Map<String, String> templateTokens) throws IOException, MessagingException {
        // Create a "text" Multipart message
        BodyPart textPart = new MimeBodyPart();
        freemarkerMailConfiguration.setClassForTemplateLoading(DefaultEmailSenderImpl.class, "/");
        Template textTemplate = freemarkerMailConfiguration.getTemplate(
                TEMPLATE_PATH + "/" + tmpl);
        final StringWriter textWriter = new StringWriter();
        try {
            textTemplate.process(templateTokens, textWriter);
        } catch (TemplateException | IOException e) {
            throw new MailPreparationException("Can't generate text mail", e);
        }
        textPart.setDataHandler(new javax.activation.DataHandler(new javax.activation.DataSource() {
            public InputStream getInputStream() throws IOException {
                // return new StringBufferInputStream(textWriter
                // .toString());
                return new ByteArrayInputStream(textWriter.toString().getBytes(CHARSET));
            }

            public OutputStream getOutputStream() throws IOException {
                throw new IOException("Read-only data");
            }

            public String getContentType() {
                return "text/plain";
            }

            public String getName() {
                return "main";
            }
        }));


        // Create a "HTML" Multipart message
//        Multipart htmlContent = new MimeMultipart("related");
        BodyPart htmlPage = new MimeBodyPart();
        freemarkerMailConfiguration.setClassForTemplateLoading(DefaultEmailSenderImpl.class, "/");
        Template htmlTemplate = freemarkerMailConfiguration.getTemplate(
                TEMPLATE_PATH + "/" + tmpl);
        final StringWriter htmlWriter = new StringWriter();
        try {
            htmlTemplate.process(templateTokens, htmlWriter);
        } catch (TemplateException e) {
            throw new MailPreparationException("Can't generate HTML mail", e);
        }
        htmlPage.setDataHandler(new javax.activation.DataHandler(new javax.activation.DataSource() {
            public InputStream getInputStream() throws IOException {
                // return new StringBufferInputStream(htmlWriter
                // .toString());
                return new ByteArrayInputStream(textWriter.toString().getBytes(CHARSET));
            }

            public OutputStream getOutputStream() throws IOException {
                throw new IOException("Read-only data");
            }

            public String getContentType() {
                return "text/html";
            }

            public String getName() {
                return "main";
            }
        }));

        Map<String, String> messages = new HashMap<>();
        messages.put("textPart", textWriter.toString());
        messages.put("htmlPart", htmlWriter.toString());
        return messages;
    }

    @Override
    public void setEmailConfig(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }
}
