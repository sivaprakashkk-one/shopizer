package com.salesmanager.core.business.modules.email;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.mail.MailPreparationException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Component("mailJetEmailSender")
public class MailJetEmailSenderImpl implements EmailModule {

    @Inject
    private Configuration freemarkerMailConfiguration;

    private final static String TEMPLATE_PATH = "templates/email";

    private static final String CHARSET = "UTF-8";

    private EmailConfig emailConfig;

    @Override
    public void send(Email email) throws Exception {

        final String eml = email.getFrom();
        final String from = email.getFromEmail();
        final String to = email.getTo();
        final String subject = email.getSubject();
        final String tmpl = email.getTemplateName();
        final Map<String, String> templateTokens = email.getTemplateTokens();
        final String bcc = email.getBcc();
        List<String> finalBcc = new ArrayList<>();

        if(bcc.contains(",")) {
            finalBcc = Arrays.stream(bcc.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        JSONArray jsonArrayBCC = new JSONArray();
        finalBcc.forEach(str -> {
            JSONObject jsonObject = new JSONObject().put("Email", str)
                    .put("Name", eml);
            jsonArrayBCC.put(jsonObject);
        });

        Map<String, String> message = constructHTMLMessage(tmpl, templateTokens);

        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        client = new MailjetClient(emailConfig.getUsername(), emailConfig.getPassword(), new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", from)
                                        .put("Name", eml))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", to)
                                                .put("Name", templateTokens.get("EMAIL_CUSTOMER_FIRSTNAME"))))
                                .put(Emailv31.Message.BCC, jsonArrayBCC)
                                .put(Emailv31.Message.SUBJECT, subject)
                                .put(Emailv31.Message.TEXTPART, message.get("textPart"))
                                .put(Emailv31.Message.HTMLPART, message.get("htmlPart"))
                                .put(Emailv31.Message.CUSTOMID, "AppGettingStartedTest")));
        response = client.post(request);
        System.out.println(response.getStatus());

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
