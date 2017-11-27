package com.fourplay.handler;

import static com.github.messenger4j.MessengerPlatform.CHALLENGE_REQUEST_PARAM_NAME;
import static com.github.messenger4j.MessengerPlatform.MODE_REQUEST_PARAM_NAME;
import static com.github.messenger4j.MessengerPlatform.SIGNATURE_HEADER_NAME;
import static com.github.messenger4j.MessengerPlatform.VERIFY_TOKEN_REQUEST_PARAM_NAME;


import com.fourplay.model.CompetitionLog;
import com.fourplay.model.Question;
import com.fourplay.model.QuestionLog;
import com.fourplay.model.User;
import com.fourplay.repository.CompetitionLogRepository;
import com.fourplay.repository.QuestionLogRepository;
import com.fourplay.repository.QuestionRepository;
import com.fourplay.repository.UserRepository;
import com.github.messenger4j.MessengerPlatform;
import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.exceptions.MessengerVerificationException;
import com.github.messenger4j.receive.MessengerReceiveClient;
import com.github.messenger4j.receive.handlers.FallbackEventHandler;
import com.github.messenger4j.receive.handlers.MessageDeliveredEventHandler;
import com.github.messenger4j.receive.handlers.MessageReadEventHandler;
import com.github.messenger4j.receive.handlers.PostbackEventHandler;
import com.github.messenger4j.receive.handlers.QuickReplyMessageEventHandler;
import com.github.messenger4j.receive.handlers.TextMessageEventHandler;
import com.github.messenger4j.send.MessengerSendClient;
import com.github.messenger4j.send.NotificationType;
import com.github.messenger4j.send.QuickReply;
import com.github.messenger4j.send.Recipient;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.github.messenger4j.send.buttons.Button;
import com.github.messenger4j.send.templates.ButtonTemplate;
import com.github.messenger4j.setup.MessengerSetupClient;
import com.github.messenger4j.setup.SetupResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is the main class for inbound and outbound communication with the Facebook Messenger Platform.
 * The callback handler is responsible for the webhook verification and processing of the inbound messages and events.
 * It showcases the features of the Messenger Platform.
 *
 * @author Max Grabenhorst
 */
@RestController
@RequestMapping("/callback")
public class MessengerPlatformCallbackHandler {

    @Autowired
    UserRepository userRepository;

    @Autowired
    QuestionLogRepository questionLogRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    CompetitionLogRepository competitionLogRepository;

    private static final String RESOURCE_URL =
            "https://raw.githubusercontent.com/fbsamples/messenger-platform-samples/master/node/public";

    private static final Logger logger = LoggerFactory.getLogger(MessengerPlatformCallbackHandler.class);

    private final MessengerReceiveClient receiveClient;
    private final MessengerSendClient sendClient;
    private MessengerSetupClient setupClient;
    SetupResponse setupResponse;


    /**
     * Constructs the {@code MessengerPlatformCallbackHandler} and initializes the {@code MessengerReceiveClient}.
     *
     * @param appSecret   the {@code Application Secret}
     * @param verifyToken the {@code Verification Token} that has been provided by you during the setup of the {@code
     *                    Webhook}
     * @param sendClient  the initialized {@code MessengerSendClient}
     */

    @Autowired
    public MessengerPlatformCallbackHandler(@Value("${messenger4j.appSecret}") final String appSecret,
                                            @Value("${messenger4j.verifyToken}") final String verifyToken,
                                            @Value("${messenger4j.pageAccessToken}") final String accessToken,
                                            final MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {

        logger.debug("Initializing MessengerReceiveClient - appSecret: {} | verifyToken: {}", appSecret, verifyToken);

        this.receiveClient = MessengerPlatform.newReceiveClientBuilder(appSecret, verifyToken)
                .onTextMessageEvent(newTextMessageEventHandler())
                .onQuickReplyMessageEvent(newQuickReplyMessageEventHandler())
                .onPostbackEvent(newPostbackEventHandler())
                .onMessageDeliveredEvent(newMessageDeliveredEventHandler())
                .onMessageReadEvent(newMessageReadEventHandler())
                .fallbackEventHandler(newFallbackEventHandler())
                .build();
        this.sendClient = sendClient;

        this.setupClient=MessengerPlatform.newSetupClientBuilder(accessToken).build();
        this.setupClient.setupWelcomeMessage("Kim 1 GB İster? kanalının Chatbot'una hoşgeldin. " +
                "Bu sohbette Sıkça Sorulan Soruları okuyabilir önerilerini bildirebilir ve Mini Kim 1 GB İster oynayarak kendini deneyebilirsin. " + "\n" +
                "Kendini denemek için 'BAŞLA' yaz. " + "\n" +
                "Sıkça sorulan soruları okumak için 'SSS' yaz. " + "\n" +
                "Önerilerini bize iletmek için 'ÖNERİ' yaz. " + "\n" +
                "Kim 1 GB İster? şu an sadece Android cihaz ve Turkcell ile uyumludur.");
//        this.setupResponse= this.setupClient.setupStartButton("get started");

    }

    /**
     * Webhook verification endpoint.
     *
     * The passed verification token (as query parameter) must match the configured verification token.
     * In case this is true, the passed challenge string must be returned by this endpoint.
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> verifyWebhook(@RequestParam(MODE_REQUEST_PARAM_NAME) final String mode,
                                                @RequestParam(VERIFY_TOKEN_REQUEST_PARAM_NAME) final String verifyToken,
                                                @RequestParam(CHALLENGE_REQUEST_PARAM_NAME) final String challenge) throws MessengerApiException, MessengerIOException {


        logger.debug("Received Webhook verification request - mode: {} | verifyToken: {} | challenge: {}", mode,
                verifyToken, challenge);
        try {
            return ResponseEntity.ok(this.receiveClient.verifyWebhook(mode, verifyToken, challenge));
        } catch (MessengerVerificationException e) {
            logger.warn("Webhook verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }


    }

    /**
     * Callback endpoint responsible for processing the inbound messages and events.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> handleCallback(@RequestBody final String payload,
                                               @RequestHeader(SIGNATURE_HEADER_NAME) final String signature) throws MessengerApiException, MessengerIOException {

        logger.debug("Received Messenger Platform callback - payload: {} | signature: {}", payload, signature);

        try {
            this.receiveClient.processCallbackPayload(payload, signature);
            logger.debug("Processed callback payload successfully");
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (MessengerVerificationException e) {
            logger.warn("Processing of callback payload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private TextMessageEventHandler newTextMessageEventHandler() {
        return event -> {
            logger.debug("Received TextMessageEvent: {}", event);

            final String messageId = event.getMid();
            final String messageText = event.getText();
            final String senderId = event.getSender().getId();
            final Date timestamp = event.getTimestamp();
            Timestamp time=new Timestamp(timestamp.getTime());

            logger.info("Received message '{}' with text '{}' from user '{}' at '{}'",
                    messageId, messageText, senderId, timestamp);

            try {
                switch (messageText.toLowerCase()) {

                    case "suggestion":
                        sendRecommendationButton(senderId);
                        break;

                    case "faq":
                        sendFaqButton(senderId);
                        break;

                    case "start":
                        sendDownloadWhenCommandIsStartButton(senderId);
                        CompetitionLog unfinishedCompetitionLog=competitionLogRepository.findCompetitionLogByCompetitionStatusAndUser(CompetitionLog.CompetitionStatus.INCOMPITION, userRepository.findUserByFacebookId(senderId));
                        QuestionLog unansweredQuestionLog=questionLogRepository.findQuestionLogByUserAndQuestionStatusAndCompetitionLogOrderByQuestionNumberAsc(userRepository.findUserByFacebookId(senderId), QuestionLog.QuestionStatus.NOTANSWERED, competitionLogRepository.findCompetitionLogByCompetitionStatusAndUser(CompetitionLog.CompetitionStatus.INCOMPITION, userRepository.findUserByFacebookId(senderId)));
                        if (unfinishedCompetitionLog!=null && unansweredQuestionLog!=null ){
                            unansweredQuestionLog.setQuestionStatus(QuestionLog.QuestionStatus.ASKED);
                            unfinishedCompetitionLog.setCompetitionStatus(CompetitionLog.CompetitionStatus.FINISHED);
                            unfinishedCompetitionLog.setEndTime(time);
                        }
                        registerToDataBase(senderId);
                        CompetitionLog competitionLog=new CompetitionLog(time, userRepository.findUserByFacebookId(senderId), CompetitionLog.CompetitionStatus.INCOMPITION);
                        competitionLogRepository.save(competitionLog);
                        sendQuestion(senderId);
                        break;

                    case "quick reply":
                        sendQuickReply(senderId);
                        break;

                    default:
                        sendTextMessage(senderId, "Yazdığın kelimeye uygun bir karşılık bulamadık. Lütfen yeniden dene. " + "\n" +
                                "Oyuna başlamak için başla yaz. " + "\n" +
                                "Sıkça sorulan soruları okumak için sss yaz " + "\n" +
                                "Önerilerini bize iletmek için öneri yaz");
                }
            } catch (MessengerApiException | MessengerIOException e) {
                handleSendException(e);
            }

        };
    }

    private void sendFaqButton(String recipientId) throws MessengerApiException, MessengerIOException {
        final List<Button> buttons = Button.newListBuilder()
                .addUrlButton("SSS için tıkla!", "https://www.oculus.com/en-us/rift/").toList()
                .build();

        final ButtonTemplate buttonTemplate = ButtonTemplate.newBuilder("Sıkça Sorulan Soruları okumak için aşağıdaki butona bas. " + "\n" +
                "Kendini denemek için başla yaz. " + "\n" +
                "Sıkça sorulan soruları okumak için sss yaz.", buttons).build();
        this.sendClient.sendTemplate(recipientId, buttonTemplate);
    }

    private void sendRecommendationButton(String recipientId) throws MessengerApiException, MessengerIOException {
        final List<Button> buttons = Button.newListBuilder()
                .addUrlButton("Önerin için tıkla!", "https://www.oculus.com/en-us/rift/").toList()
                .build();

        final ButtonTemplate buttonTemplate = ButtonTemplate.newBuilder("Önerini göndermek için aşağıdaki butona bas. " + "\n" +
                "Kendini denemek için başla yaz. " + "\n" +
                "Sıkça sorulan soruları okumak için sss yaz.", buttons).build();
        this.sendClient.sendTemplate(recipientId, buttonTemplate);
    }

    private void sendDownloadWhenCommandIsStartButton(String recipientId) throws MessengerApiException, MessengerIOException {
        final List<Button> buttons = Button.newListBuilder()
                .addUrlButton("İndirmek için tıkla!", "https://www.oculus.com/en-us/rift/").toList()
                .build();

        final ButtonTemplate buttonTemplate = ButtonTemplate.newBuilder("Mini Kim 1 GB İster? oyununda kendini denemek üzere oyunun başlıyor. "+ "\n" +
                       "Bu oyun Kim 1 GB İster?'in simulasyonu olarak tasarlanmıştır. 1 GB büyük ödül ve daha fazlasını " +
                       "kazanabilmek için aşağıdaki butona tıklayarak BİP indirip Kim 1 GB İster?'i takip etmeye başla."
                , buttons).build();
        this.sendClient.sendTemplate(recipientId, buttonTemplate);
    }

    private void sendDownloadWhenAnswerIsWrongButton(String recipientId) throws MessengerApiException, MessengerIOException {
        final List<Button> buttons = Button.newListBuilder()
                .addUrlButton("İndirmek için tıkla!", "https://www.oculus.com/en-us/rift/").toList()
                .build();

        final ButtonTemplate buttonTemplate = ButtonTemplate.newBuilder("Maalesef cevabın yanlış! "+"\n" + "Oyuna başlamak için 'BAŞLA' yaz. " + "\n" +
                        "Sıkça sorulan soruları okumak için 'SSS' yaz. " + "\n" +
                        "Önerilerini bize iletmek için 'ÖNERİ' yaz. " + "\n" +
                        "Oyunun tam halini oynamak ve 1 GB ödül " + "\n" +
                        "kazanma şansını yakalmak için ise için aşağıdaki butona tıklayarak BİP indirip Kim 1 GB İster?'i takip etmeye başla."
                , buttons).build();
        this.sendClient.sendTemplate(recipientId, buttonTemplate);
    }

    private void sendDownloadWhenQuizIsFinishedButton(String recipientId) throws MessengerApiException, MessengerIOException {
        final List<Button> buttons = Button.newListBuilder()
                .addUrlButton("İndirmek için tıkla!", "https://www.oculus.com/en-us/rift/").toList()
                .build();

        final ButtonTemplate buttonTemplate = ButtonTemplate.newBuilder( "Tebrikler! Mini Kim 1 GB İster? oyununu tamamladın."+ "\n" +
                        "Oyuna başlamak için 'BAŞLA' yaz. " + "\n" +
                        "Sıkça sorulan soruları okumak için 'SSS' yaz. " + "\n" +
                        "Önerilerini bize iletmek için 'ÖNERİ' yaz. " + "\n" +
                        "Oyunun tam halini oynamak ve 1 GB ödül " +
                        ""
               , buttons).build();
        this.sendClient.sendTemplate(recipientId, buttonTemplate);
    }


    private void registerToDataBase(String facebookId){
        if (!userRepository.existsByFacebookId(facebookId)){
            User user=new User(facebookId);
            userRepository.save(user);
        }
    }

    private void insertQuestion(){
        if (questionRepository.count()==0){
            Question question1=new Question("https://user-images.githubusercontent.com/22446712/29210937-d303848a-7e9e-11e7-8f3a-3c38b64fb9ce.png", Question.TrueAnswer.C);
            Question question2=new Question("https://user-images.githubusercontent.com/22446712/29210950-df34d0a6-7e9e-11e7-8da1-7fc0ff064ca6.png", Question.TrueAnswer.B);
            Question question3=new Question("https://user-images.githubusercontent.com/22446712/29210958-e8b2f5ea-7e9e-11e7-8967-b6b390c92dbd.png", Question.TrueAnswer.A);
            Question question4=new Question("https://user-images.githubusercontent.com/22446712/29210973-f66cf726-7e9e-11e7-831c-690baea9e3c0.png", Question.TrueAnswer.A);
            Question question5=new Question("https://user-images.githubusercontent.com/22446712/29210997-0d7789a4-7e9f-11e7-9287-e0a255847c3d.png", Question.TrueAnswer.C);

            questionRepository.save(question1);
            questionRepository.save(question2);
            questionRepository.save(question3);
            questionRepository.save(question4);
            questionRepository.save(question5);
        }

    }

    private void sendQuestion(String recipientId) throws MessengerApiException, MessengerIOException {

        if(questionRepository.count()==0){
            insertQuestion();

        }

        Question question=questionRepository.selectRandomQuestion();
        QuestionLog questionLog=new QuestionLog();
        questionLog.setUserId(userRepository.findUserByFacebookId(recipientId));




        String getQuestionURL=question.getQuestionUrl();


        questionLog.setQuestion(question);
        questionLog.setQuestionStatus(QuestionLog.QuestionStatus.NOTANSWERED);
        questionLog.setCompetitionLogs(competitionLogRepository.findCompetitionLogByCompetitionStatusAndUser(CompetitionLog.CompetitionStatus.INCOMPITION, userRepository.findUserByFacebookId(recipientId)));
        questionLog.setQuestionNumber(questionLogRepository.countQuestionLogByCompetitionLogAndUser(competitionLogRepository.findCompetitionLogByCompetitionStatusAndUser(CompetitionLog.CompetitionStatus.INCOMPITION, userRepository.findUserByFacebookId(recipientId)), userRepository.findUserByFacebookId(recipientId))+1);

        questionLogRepository.save(questionLog);

        this.sendClient.sendImageAttachment(recipientId, getQuestionURL);

        sendQuickReply(recipientId);
    }


    private void sendQuickReply(String recipientId) throws MessengerApiException, MessengerIOException {
        final List<QuickReply> quickReplies = QuickReply.newListBuilder()
                .addTextQuickReply("A", "A").toList()
                .addTextQuickReply("B", "B").toList()
                .addTextQuickReply("C", "C").toList()
                .addTextQuickReply("D", "D").toList()
                .build();

        this.sendClient.sendTextMessage(recipientId, "Aşağıdaki seçeneklerden birini seçiniz", quickReplies);

    }


    private QuickReplyMessageEventHandler newQuickReplyMessageEventHandler() {
        return event -> {
            logger.debug("Received QuickReplyMessageEvent: {}", event);

            final String senderId = event.getSender().getId();
            final String messageId = event.getMid();
            final String quickReplyPayload = event.getQuickReply().getPayload();
            final Date timestamp = event.getTimestamp();
            Timestamp answerTime=new Timestamp(timestamp.getTime());

            QuestionLog questionLog = questionLogRepository.findQuestionLogByUserAndQuestionStatusAndCompetitionLogOrderByQuestionNumberAsc(userRepository.findUserByFacebookId(senderId), QuestionLog.QuestionStatus.NOTANSWERED, competitionLogRepository.findCompetitionLogByCompetitionStatusAndUser(CompetitionLog.CompetitionStatus.INCOMPITION, userRepository.findUserByFacebookId(senderId)));

            if(questionLog != null) {
                questionLog.setUserAnswer(quickReplyPayload);
                questionLog.setAnswerTime(answerTime);
                questionLog.setQuestionStatus(QuestionLog.QuestionStatus.ASKED);
                questionLogRepository.save(questionLog);

                if (questionLog.getQuestion().getTrueAnswer().getValue().equals(quickReplyPayload) && questionLog.getQuestionNumber()<5){
                    try {
                        sendTextMessage(senderId, "Tebrikler, doğru cevap!");
                        sendQuestion(senderId);
                    } catch (MessengerApiException e) {
                        e.printStackTrace();
                    } catch (MessengerIOException e) {
                        e.printStackTrace();
                    }
                }
                else if (questionLog.getQuestionNumber()>=5){
                    try {
                        sendDownloadWhenQuizIsFinishedButton(senderId);
                    } catch (MessengerApiException e) {
                        e.printStackTrace();
                    } catch (MessengerIOException e) {
                        e.printStackTrace();
                    }
                    CompetitionLog competitionLog=competitionLogRepository.findCompetitionLogByCompetitionStatusAndUser(CompetitionLog.CompetitionStatus.INCOMPITION, userRepository.findUserByFacebookId(senderId));
                    competitionLog.setCompetitionStatus(CompetitionLog.CompetitionStatus.FINISHED);
                    competitionLog.setEndTime(answerTime);
                    competitionLogRepository.save(competitionLog);
                }
                else{
                    try {
                        sendDownloadWhenAnswerIsWrongButton(senderId);
                    } catch (MessengerApiException e) {
                        e.printStackTrace();
                    } catch (MessengerIOException e) {
                        e.printStackTrace();
                    }
                    CompetitionLog competitionLog=competitionLogRepository.findCompetitionLogByCompetitionStatusAndUser(CompetitionLog.CompetitionStatus.INCOMPITION, userRepository.findUserByFacebookId(senderId));
                    competitionLog.setCompetitionStatus(CompetitionLog.CompetitionStatus.FINISHED);
                    competitionLog.setEndTime(answerTime);
                    competitionLogRepository.save(competitionLog);
                }
            }

            logger.info("Received quick reply for message '{}' with payload '{}'", messageId, quickReplyPayload);

        };
    }

    private PostbackEventHandler newPostbackEventHandler() {
        return event -> {
            logger.debug("Received PostbackEvent: {}", event);

            final String senderId = event.getSender().getId();
            final String recipientId = event.getRecipient().getId();
            final String payload = event.getPayload();
            final Date timestamp = event.getTimestamp();
            Timestamp startTime=new Timestamp(timestamp.getTime());

            logger.info("Received postback for user '{}' and page '{}' with payload '{}' at '{}'",
                    senderId, recipientId, payload, timestamp);


//            if(payload.equalsIgnoreCase("Get Started")){
//                try {
//                    registerToDataBase(senderId);
//                    CompetitionLog competitionLog=new CompetitionLog(startTime, userRepository.findUserByFacebookId(senderId), CompetitionLog.CompetitionStatus.INCOMPITION);
//                    competitionLogRepository.save(competitionLog);
//                    sendQuestion(senderId);
//                } catch (MessengerApiException e) {
//                    e.printStackTrace();
//                } catch (MessengerIOException e) {
//                    e.printStackTrace();
//                }
//            }

        };
    }

    private MessageDeliveredEventHandler newMessageDeliveredEventHandler() {
        return event -> {
            logger.debug("Received MessageDeliveredEvent: {}", event);

            final List<String> messageIds = event.getMids();
            final Date watermark = event.getWatermark();
            final String senderId = event.getSender().getId();

            if (messageIds != null) {
                messageIds.forEach(messageId -> {
                    logger.info("Received delivery confirmation for message '{}'", messageId);
                });
            }

            logger.info("All messages before '{}' were delivered to user '{}'", watermark, senderId);
        };
    }

    private MessageReadEventHandler newMessageReadEventHandler() {
        return event -> {
            logger.debug("Received MessageReadEvent: {}", event);

            final Date watermark = event.getWatermark();
            final String senderId = event.getSender().getId();

            logger.info("All messages before '{}' were read by user '{}'", watermark, senderId);
        };
    }

    /**
     * This handler is called when either the message is unsupported or when the event handler for the actual event type
     * is not registered. In this showcase all event handlers are registered. Hence only in case of an
     * unsupported message the fallback event handler is called.
     */
    private FallbackEventHandler newFallbackEventHandler() {
        return event -> {
            logger.debug("Received FallbackEvent: {}", event);

            final String senderId = event.getSender().getId();
            logger.info("Received unsupported message from user '{}'", senderId);
        };
    }

    private void sendTextMessage(String recipientId, String text) {
        try {
            final Recipient recipient = Recipient.newBuilder().recipientId(recipientId).build();
            final NotificationType notificationType = NotificationType.REGULAR;
            final String metadata = "DEVELOPER_DEFINED_METADATA";

            this.sendClient.sendTextMessage(recipient, notificationType, text, metadata);
        } catch (MessengerApiException | MessengerIOException e) {
            handleSendException(e);
        }
    }

    private void handleSendException(Exception e) {
        logger.error("Message could not be sent. An unexpected error occurred.", e);
    }
}