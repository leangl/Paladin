package com.nanospark.gard.sms;

import android.os.Handler;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanospark.gard.BuildConfig;
import com.nanospark.gard.GarD;
import com.nanospark.gard.events.DoorStateChanged;
import com.nanospark.gard.events.SmsSuspended;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.user.User;
import com.nanospark.gard.model.user.UserManager;
import com.nanospark.gard.sms.twilio.TwilioAccount;
import com.nanospark.gard.sms.twilio.TwilioApi;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import mobi.tattu.utils.StringUtils;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.persistance.datastore.DataStore;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import roboguice.RoboGuice;
import roboguice.util.Ln;
import rx.Observable;

import static com.nanospark.gard.sms.twilio.TwilioApi.DATE_FORMAT;

/**
 * Created by Leandro on 26/7/2015.
 */
@Singleton
public class SmsManager {

    private static final int RATE_LIMIT_1_MINUTE = 8; // Max allowed SMSs within 1 minute
    private static final int RATE_LIMITE_10_MINUTE = 24; // Max allowed SMSs within 10 minutes
    private static long MESSAGES_CHECK_TIME = TimeUnit.SECONDS.toMillis(1);
    private static long MESSAGES_RETRY_TIME = TimeUnit.SECONDS.toMillis(30);

    private SmsConfig mConfig;
    private TwilioApi mApi;
    private Handler smsHandler;
    private List<Long> mLastSentTimestamps;

    private String fakeSms;

    @Inject
    private UserManager mUserManager;

    public SmsManager() {
        Gson gson = new GsonBuilder()
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.twilio.com")
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(mBasicAuthInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL).setLog(Ln::d)
                .build();

        mApi = restAdapter.create(TwilioApi.class);
        smsHandler = new Handler();
        mLastSentTimestamps = new ArrayList<>(RATE_LIMITE_10_MINUTE);

        Tattu.register(this);

        mConfig = DataStore.getInstance().get(SmsConfig.class.getSimpleName(), SmsConfig.class).get();
        if (mConfig == null) {
            mConfig = new SmsConfig();
        }
    }

    public void fakeSms(String sms) {
        fakeSms = sms;
        startChecking();
    }

    public static final SmsManager getInstance() {
        return RoboGuice.getInjector(GarD.instance).getInstance(SmsManager.class);
    }

    public void startChecking() {
        // Start checking for incoming SMS messages
        smsHandler.removeCallbacksAndMessages(null);
        smsHandler.post(checkMessages);
    }

    public void stopChecking() {
        if (smsHandler != null) {
            smsHandler.removeCallbacksAndMessages(null);
        }
    }

    private RequestInterceptor mBasicAuthInterceptor = request -> {
        TwilioAccount account = getAccount();
        if (account != null && account.isValid()) {
            String credentials = account.getSid() + ":" + account.getToken();
            String string = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            request.addHeader("Authorization", string);
        }
    };

    public void saveConfig() {
        DataStore.getInstance().put(SmsConfig.class.getSimpleName(), mConfig);
        startChecking();
    }

    public void setTwilioAccount(TwilioAccount account) {
        mConfig.account = account;
        saveConfig();
    }

    public void enableSms() {
        mConfig.enabled = true;
        saveConfig();
    }

    public void disableSms() {
        mConfig.enabled = false;
        saveConfig();
    }

    public boolean isSmsEnabled() {
        return mConfig.enabled;
    }

    public void suspendSms() {
        mConfig.suspended = true;
        saveConfig();
        Tattu.post(new SmsSuspended());
    }

    public void resumeSms() {
        mConfig.suspended = false;
        saveConfig();
    }

    public boolean isSmsSuspended() {
        return mConfig.suspended;
    }

    public Observable<Void> sendMessage(String message, String to) {
        if (!isSmsEnabled()) {
            return Observable.empty();
        }

        if (exceededLimit()) {
            suspendSms();
            return Observable.empty();
        }

        if (BuildConfig.DEBUG) return Observable.empty();

        TwilioAccount account = getAccount();
        if (account != null && account.isValid()) {
            return mApi.sendMessage(account.getSid(), message, account.getPhone(), to).map(result -> {
                Ln.i("Messages sent: " + message + " to " + to);
                mLastSentTimestamps.add(System.currentTimeMillis());
                return null;
            });
        } else {
            Ln.e("SMS enabled but account not set!");
            return Observable.error(new Exception());
        }
    }

    private boolean exceededLimit() {
        long currentTime = System.currentTimeMillis();
        if (mLastSentTimestamps.size() >= RATE_LIMIT_1_MINUTE) {
            long delta = currentTime - mLastSentTimestamps.get(mLastSentTimestamps.size() - RATE_LIMIT_1_MINUTE);
            if (delta < TimeUnit.MINUTES.toMillis(1)) return true;
        }

        if (mLastSentTimestamps.size() >= RATE_LIMITE_10_MINUTE) {
            long delta = currentTime - mLastSentTimestamps.get(mLastSentTimestamps.size() - RATE_LIMITE_10_MINUTE);
            if (delta < TimeUnit.MINUTES.toMillis(10)) return true;
        }

        return false;
    }

    public TwilioAccount getAccount() {
        return mConfig.account;
    }

    /**
     * Returns the last new message received since the last check.
     * If more than one message was received, then only the newest is returned.
     * If no new messages, then null is returned.
     *
     * @return
     */
    public Observable<JsonObject> getNewMessage() {
        if (fakeSms != null) {
            JsonObject sms = new JsonObject();
            sms.addProperty("body", fakeSms);
            sms.addProperty("from", "19");
            fakeSms = null;
            return Observable.just(sms);
        }
        if (BuildConfig.DEBUG) return Observable.error(new Exception());

        TwilioAccount account = getAccount();
        if (isSmsEnabled() && account != null && account.isValid()) {
            return mApi.getMessages(account.getSid(), account.getPhone()).map(response -> {
                JsonArray messages = response.getAsJsonObject().getAsJsonArray("messages");
                if (messages.size() > 0) {
                    for (JsonElement message : messages) {
                        JsonObject messageObj = message.getAsJsonObject();
                        String direction = messageObj.get("direction").getAsString();
                        if ("inbound".equals(direction)) {
                            try {
                                Date timestamp = DATE_FORMAT.parse(messageObj.get("date_sent").getAsString());
                                boolean after = true;
                                Calendar lastCal = Calendar.getInstance();
                                Date lastTimestamp = DataStore.getInstance().get("LAST_TIMESTAMP", Date.class).get();
                                if (lastTimestamp != null) {
                                    lastCal.setTime(lastTimestamp);

                                    Calendar messageCal = Calendar.getInstance();
                                    messageCal.setTime(timestamp);

                                    after = messageCal.after(lastCal);
                                }
                                if (after) {
                                    DataStore.getInstance().put("LAST_TIMESTAMP", timestamp);
                                    return messageObj;
                                }
                            } catch (ParseException e) {
                                System.out.println(e.getMessage());
                            }
                            break;
                        }
                    }
                }
                return null;
            });
        } else {
            Ln.e("Account not set!");
            return Observable.error(new IllegalStateException());
        }
    }

    private Map<String, SmsCommand> pendingCommands = new HashMap<>();

    /**
     * Periodically checks Twilio Messages Log for new messages
     */
    private Runnable checkMessages = new Runnable() {
        @Override
        public void run() {
            if (!isSmsEnabled()) {
                Ln.d("SMS is disabled");
                return;
            }
            if (isSmsSuspended()) {
                Ln.d("SMS is suspended");
                return;
            }
            getNewMessage().subscribe(message -> {
                // if new message is received
                if (message != null) {
                    // check if the body matches the current door status
                    String body = message.get("body").getAsString().trim();
                    String from = message.get("from").getAsString().trim();
                    String replyMessage = null;

                    User fromUser = mUserManager.findByPhone(from);
                    try {
                        if (SmsCommand.isSmsCommand(body)) {
                            replyMessage = handle(SmsCommand.fromBody(fromUser, from, body));
                        } else {
                            replyMessage = handleAuthentication(from, body);
                        }
                    } catch (Exception e) {
                        Ln.e("Invalid command: " + body, e);
                        replyMessage = "Invalid command. Format has to be: {command} {door name}";
                        if (fromUser != null && fromUser.isPasswordRequired()) {
                            replyMessage = replyMessage + " {pass code}";
                        }
                    }

                    Ln.i(replyMessage);

                    if (StringUtils.isNotBlank(replyMessage)) { // do not send empty messages
                        sendMessage(replyMessage, from).subscribe(success -> {
                            Ln.i("Reply sent successfully");
                        }, error -> {
                            Ln.e("Error sending reply.", error);
                        });
                    }
                }
                // Reschedule message log check
                if (smsHandler != null) {
                    smsHandler.removeCallbacksAndMessages(null);
                    smsHandler.postDelayed(checkMessages, MESSAGES_CHECK_TIME);
                }
            }, error -> {
                smsHandler.removeCallbacksAndMessages(null);
                smsHandler.postDelayed(checkMessages, MESSAGES_RETRY_TIME);
            });
        }
    };

    private String handle(SmsCommand smsCommand) throws Exception {
        if (smsCommand.user == null) {
            pendingCommands.put(smsCommand.from, smsCommand);
            return "Seems like you're using a different phone, please text your user name and pass code (if you have one) to process command";
        }
        if (smsCommand.user.isPasswordRequired() && !smsCommand.user.isPasswordCorrect(smsCommand.password)) {
            return "Pass code is invalid";
        }
        if (!smsCommand.user.isAllowed()) {
            return "You don't have permission to operate the door(s) at this time.";
        }
        if (smsCommand.is(SmsCommand.STATUS)) {
            StringBuilder response = new StringBuilder();
            for (Door door : smsCommand.doors) {
                if (door.isEnabled()) {
                    if (response.length() > 0) response.append(" and ");
                    response.append(door.getName() + " is " + door.getState().toString().toLowerCase());
                }
            }
            return response.toString();
        } else {
            for (Door door : smsCommand.doors) { // TODO right now multiple door command works only for STATUS
                if (door.isEnabled()) {
                    if (smsCommand.is(SmsCommand.OPEN)) {
                        if (door.isOpen()) {
                            return "The door is already open.";
                        }
                        door.send(new Door.Open(smsCommand.user, "Message received, door is in motion", false, smsCommand.user));
                        return "Open door command received.";
                    } else if (smsCommand.is(SmsCommand.CLOSE)) {
                        if (door.isClosed()) {
                            return "The door is already closed.";
                        }
                        door.send(new Door.Close(smsCommand.user, "Message received, door is in motion", false, smsCommand.user));
                        return "Close door command received.";
                    }
                }
            }
        }
        throw new Exception("Invalid command " + smsCommand.command);
    }

    private String handleAuthentication(String from, String body) throws Exception {
        if (!pendingCommands.containsKey(from)) {
            throw new Exception("No pending authentication for " + from);
        }
        SmsCommand command = pendingCommands.get(from);
        if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - command.timestamp) >= 5) {
            Ln.w("Authentication challenge expired for " + from);
            return "Your permission to operate the door(s) has expired.";
        }
        if (StringUtils.isEmpty(body)) {
            throw new Exception("Invalid authentication format");
        }
        String[] bodyParts = body.split(" ");

        User user = mUserManager.findByName(bodyParts[0]);
        if (user == null) {
            return "There is no user by that name";
        }
        command.user = user;

        // clear pending command if authentication is successful (pass is correct or not required)
        if (command.user.isPasswordRequired()) {
            if (bodyParts.length > 1) {
                command.password = bodyParts[1].trim();
                if (command.user.isPasswordCorrect(command.password)) {
                    pendingCommands.remove(from);
                } else {
                    return "Pass code is invalid (or was left blank and is required).";
                }
            }
        } else {
            pendingCommands.remove(from);
        }

        return handle(command);
    }

    @Subscribe
    public void on(DoorStateChanged event) {
        if (event.door.isEnabled()) {
            String message;
            if (event.source != null) {
                message = event.door + " has been " + event.state.toString().toLowerCase() + " by " + event.source.getSourceDescription();
            } else {
                message = event.door + " is " + event.state.toString().toLowerCase() + ", not by a command through your PALADIN system.";
            }
            sendDoorAlert(message, event.state);
        }
    }

    /**
     * Send SMS message to users configured to receive door status alerts
     * <p>
     * If state is NULL then SMS is sent to every user subscribed to at least one state
     *
     * @param alert SMS
     * @param state only to users subscribed to this state or everyone if null
     */
    public void sendDoorAlert(String alert, Door.State state) {
        int count = 0;
        for (User user : mUserManager.getAll()) {
            if (StringUtils.isNotBlank(user.getPhone())
                    && ((state == null && !user.getNotify().equals(User.Notify.NONE))
                    || user.getNotify().notify(state))) {
                count++;
                sendMessage(alert, user.getPhone()).subscribe(success -> {
                    Ln.d("SMS success");
                }, error -> {
                    Ln.e("SMS error", error);
                });
            }
        }
        Ln.d("Sending alert to #" + count);
    }

    @Produce
    public SmsSuspended produceSuspended() {
        if (isSmsSuspended()) {
            return new SmsSuspended();
        }
        return null;
    }

    public static class SmsConfig {
        private static final String DEFAULT_SID = BuildConfig.DEBUG ? "AC83aacf8a55784375290210a9eb924ad4" : "";
        private static final String DEFAULT_TOKEN = BuildConfig.DEBUG ? "3f5dab33644d33cacb4bf300194299eb" : "";

        public TwilioAccount account = new TwilioAccount("", DEFAULT_SID, DEFAULT_TOKEN);
        public boolean enabled = true;
        public boolean suspended = false;
    }

}
