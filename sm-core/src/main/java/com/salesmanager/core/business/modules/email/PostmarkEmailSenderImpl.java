package com.salesmanager.core.business.modules.email;

import com.postmarkapp.postmark.Postmark;
import com.postmarkapp.postmark.client.ApiClient;
import com.postmarkapp.postmark.client.data.model.message.Message;
import com.postmarkapp.postmark.client.data.model.message.MessageResponse;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.MailPreparationException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        final String bcc = email.getBcc();
        List<String> finalBcc = new ArrayList<>();

        if(bcc.contains(",")) {
            finalBcc = Arrays.stream(bcc.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

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

        // Set BCC
        if(!finalBcc.isEmpty()) {
            message.setBcc(finalBcc);
        } else if(StringUtils.isNotBlank(bcc)){
            message.setBcc(bcc);
        }

        message.setMessageStream("outbound");
        MessageResponse response = mailClient.deliverMessage(message);
    }

    @Override
    public void setEmailConfig(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }
}
