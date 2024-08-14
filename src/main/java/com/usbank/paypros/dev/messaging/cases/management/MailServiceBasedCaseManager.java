package com.usbank.paypros.dev.messaging.cases.management;

import com.usbank.paypros.dev.messaging.cases.Case;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.mail2.jakarta.util.MimeMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class MailServiceBasedCaseManager implements CaseManager<MimeMessage> {

    private final ChatModel chatModel;
    private final Resource caseManagementResource;
    private final Resource caseEvaluationResource;
    private final Resource caseValidationResource;
    private final CaseRepository caseRepository;

    private final BeanOutputConverter<Case> beanOutputConverter = new BeanOutputConverter<>(Case.class);
    private static final Logger LOG = LoggerFactory.getLogger(CaseManager.class);

    public MailServiceBasedCaseManager(ChatModel chatModel, ResourceLoader resourceLoader, CaseRepository caseRepository) {
        this.chatModel = chatModel;
        this.caseManagementResource = resourceLoader.getResource("classpath:prompts/case-management.st");
        this.caseEvaluationResource = resourceLoader.getResource("classpath:prompts/case-evaluation.st");
        this.caseValidationResource = resourceLoader.getResource("classpath:prompts/case-validation.st");
        this.caseRepository = caseRepository;
    }

    public void evaluate(Case newCase) {

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(caseManagementResource);
        PromptTemplate userPromptTemplate = new PromptTemplate(caseEvaluationResource);
        ChatResponse response = chatModel.call(
                new Prompt(
                        List.of(
                                systemPromptTemplate.createMessage(Map.of("format", beanOutputConverter.getFormat())),
                                userPromptTemplate.createMessage(Map.of("case", newCase.toString()))
                        ),
                        AzureOpenAiChatOptions.builder()
                                .withFunctions(Set.of("validateAccountID", "validateCreatedBy"))
                                .build()
                ));
        Case managedCase = beanOutputConverter.convert(response.getResult().getOutput().getContent());
        caseRepository.save(managedCase);
    }

    public Case newCase(MimeMessage message) {

        try {
            MimeMessageParser mimeMessageParser = new MimeMessageParser(message).parse();
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(caseValidationResource);
            PromptTemplate userPromptTemplate = new PromptTemplate(mimeMessageParser.getPlainContent());
            ChatResponse response = chatModel.call(
                    new Prompt(
                            List.of(
                                    systemPromptTemplate.createMessage(Map.of("format", beanOutputConverter.getFormat())),
                                    userPromptTemplate.createMessage()
                            )
                    ));
            Case newCase = beanOutputConverter.convert(response.getResult().getOutput().getContent());
            if (newCase != null && newCase.caseID() != null) {
                Case managedCase = caseRepository.findById(newCase.caseID());
                if (managedCase != null) {
                    return new Case(
                            managedCase.caseID(),
                            managedCase.createdBy(),
                            mimeMessageParser.getSubject(),
                            Base64.encodeBase64String(mimeMessageParser.getPlainContent().getBytes()),
                            managedCase.status(),
                            null
                    );
                }
            }
            return new Case(
                    UUID.randomUUID().toString(),
                    mimeMessageParser.getFrom(),
                    mimeMessageParser.getSubject(),
                    Base64.encodeBase64String(mimeMessageParser.getPlainContent().getBytes()),
                    Case.Status.ACKNOWLEDGED.toString(),
                    null
            );
        } catch (MessagingException | IOException ignored) {}

        return null;
    }
}
