package com.qingmeng.smartpictureku.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 邮件发送工具
 * @author Wang
 */
@Component
@Slf4j
public class EmailSenderUtil {

    // 测试 如果出现日志出现问题，回复相面内容，并替换log
    //private static final Logger logger = LoggerFactory.getLogger(EmailSenderUtil.class);

    @Value("${spring.mail.from}")
    private String from;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    /**
     * 发送邮件
     * @param toEmail 目标邮箱
     * @param generatedCode 验证码
     */
    public void sendEmail(String toEmail, String generatedCode) {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", String.valueOf(port));
        properties.put("mail.smtp.starttls.enable", "false");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.port", String.valueOf(port));
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

            // 编码邮件主题
            String encodedSubject = MimeUtility.encodeText("共享云图库邮箱验证码", "UTF-8", "B");
            message.setSubject(encodedSubject, "UTF-8");

            String htmlContent = readHTMLFromFile();
            htmlContent = htmlContent.replace(":data=\"verify\"", ":data=\"" + generatedCode + "\"").replace("000000", generatedCode);

            // 设置邮件内容编码
            message.setContent(htmlContent, "text/html;charset=UTF-8");

            Transport.send(message);
            log.info("Sent message successfully to {}", toEmail);
        } catch (MessagingException | IOException e) {
            log.error("Error sending email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * 读取HTML文件内容
     * @return
     * @throws IOException
     */
    private String readHTMLFromFile() throws IOException {
        ClassPathResource resource = new ClassPathResource("html/vericode_email.html");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine())!= null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    /**
     * 发送审核通知邮件
     * @param toEmail
     * @param htmlContent
     */
    public void sendReviewEmail(String toEmail, String htmlContent) {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", String.valueOf(port));
        properties.put("mail.smtp.starttls.enable", "false");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.port", String.valueOf(port));
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

            // 编码邮件主题
            String encodedSubject = MimeUtility.encodeText("共享云图库 内容审核通知", "UTF-8", "B");
            message.setSubject(encodedSubject, "UTF-8");

            // 设置邮件内容
            message.setContent(htmlContent, "text/html;charset=UTF-8");

            Transport.send(message);
            log.info("审核通知邮件发送成功");
        } catch (MessagingException | IOException e) {
            log.error("审核通知邮件发送失败: {}", e.getMessage(), e);
        }
    }
}
