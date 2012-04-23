package com.qcadoo.mail.internal;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.qcadoo.mail.api.InvalidMailAddressException;
import com.qcadoo.mail.api.MailConfigurationException;
import com.qcadoo.mail.api.MailService;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    protected JavaMailSender mailSender;

    @Value("${mail.address}")
    private String defaultSender;

    @Override
    public void sendEmail(final String recipient, final String subject, final String body) {
        sendHtmlTextEmail(getDefaultSender(), recipient, subject, body);
    }

    protected String getDefaultSender() {
        if (isValidEmail(defaultSender)) {
            return defaultSender;
        }
        throw new MailConfigurationException('\'' + defaultSender + "' is not valid e-mail address. Check your mail.properties");
    }

    protected void sendHtmlTextEmail(final String sender, final String recipient, final String subject, final String body) {
        validateEmail(sender);
        validateEmail(recipient);
        Preconditions.checkArgument(StringUtils.isNotBlank(subject), "e-mail subject should not be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(body), "e-mail body should not be blank");

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        try {
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(recipient);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);
        } catch (MessagingException e) {
            throw new MailPreparationException(e);
        }

        mailSender.send(mimeMessage);
    }

    private void validateEmail(final String email) {
        if (!isValidEmail(email)) {
            throw new InvalidMailAddressException('\'' + email + "' is not valid e-mail address");
        }
    }

    public static boolean isValidEmail(final String email) {
        return StringUtils.isNotBlank(email) && EmailValidator.getInstance().isValid(email);
    }

}