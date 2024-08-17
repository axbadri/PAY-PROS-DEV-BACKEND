package com.bigobrains.paypros.dev.messaging;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailReceiver;
import org.springframework.integration.mail.MailReceivingMessageSource;
import org.springframework.messaging.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Properties;
import java.util.concurrent.Executor;

@Configuration
@EnableIntegration
public class AsyncMailConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncMailConfigurer.class);

    private final MailService mailService;
    private final TaskExecutionProperties taskExecutionProperties;

    public AsyncMailConfigurer(MailService mailService, TaskExecutionProperties taskExecutionProperties) {
        this.mailService = mailService;
        this.taskExecutionProperties = taskExecutionProperties;
    }

    @ServiceActivator(inputChannel = "receiveEmailChannel")
    public void receive(Message<?> message) {
        mailService.handleMail((MimeMessage) message.getPayload());
    }

    @Bean("receiveEmailChannel")
    public DirectChannel defaultChannel() {
        DirectChannel directChannel = new DirectChannel();
        directChannel.setDatatypes(jakarta.mail.internet.MimeMessage.class);
        return directChannel;
    }

    @Bean()
    @InboundChannelAdapter(
            channel = "receiveEmailChannel",
            poller = @Poller(fixedDelay = "5000", taskExecutor = "asyncMailTaskExecutor")
    )
    public MailReceivingMessageSource mailMessageSource(MailReceiver mailReceiver) {
        return new MailReceivingMessageSource(mailReceiver);
    }

    @Bean
    public MailReceiver imapMailReceiver() {

        Properties javaMailProperties = new Properties();
        ImapMailReceiver imapMailReceiver = new ImapMailReceiver("imaps://imap.gmail.com/inbox");
        imapMailReceiver.setShouldMarkMessagesAsRead(true);
        imapMailReceiver.setShouldDeleteMessages(false);
        imapMailReceiver.setMaxFetchSize(10);
        imapMailReceiver.setAutoCloseFolder(false);
        imapMailReceiver.setJavaMailAuthenticator(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("genai.paypros@gmail.com", "");
            }
        });
        javaMailProperties.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        javaMailProperties.put("mail.imap.socketFactory.fallback", false);
        javaMailProperties.put("mail.store.protocol", "imaps");
        javaMailProperties.put("mail.debug", true);
        imapMailReceiver.setJavaMailProperties(javaMailProperties);
        return imapMailReceiver;
    }

    @Bean(name = "asyncMailTaskExecutor")
    public Executor asyncMailTaskExecutor() {
        LOG.debug("Creating AsyncMailTaskExecutor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(taskExecutionProperties.getPool().getCoreSize());
        executor.setMaxPoolSize(taskExecutionProperties.getPool().getMaxSize());
        executor.setQueueCapacity(taskExecutionProperties.getPool().getQueueCapacity());
        executor.setThreadNamePrefix(taskExecutionProperties.getThreadNamePrefix());
        return executor;
    }
}
