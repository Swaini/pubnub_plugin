package cube.com.pubnubplugin;

import android.util.Log;

import com.google.gson.Gson;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Arrays;
import java.util.HashMap;

import cube.com.pubnubplugin.model.Message;
import cube.com.pubnubplugin.util.DateTimeUtil;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;


/**
 * PubnubPlugin
 */
public class PubnubPlugin implements MethodCallHandler
{
	/**
	 * Plugin registration.
	 */
	private PubNub pubnub;
	private String channelName = "test";
	private static EventChannel.EventSink messageSender;
	private static EventChannel.EventSink statusSender;
	private String uuid = "";

	public static void registerWith(Registrar registrar)
	{
		MethodChannel channel = new MethodChannel(registrar.messenger(), "pubnub");
		channel.setMethodCallHandler(new PubnubPlugin());

		EventChannel messageChannel = new EventChannel(registrar.messenger(), "plugins.flutter.io/pubnub_message");

		messageChannel.setStreamHandler(new EventChannel.StreamHandler()
		{
			@Override
			public void onListen(Object arguments, EventChannel.EventSink events)
			{
				Log.d(getClass().getName(), "messageChannel.onListen");
				messageSender = events;
			}

			@Override
			public void onCancel(Object arguments)
			{
				Log.d(getClass().getName(), "messageChannel.onCancel");
			}
		});

		EventChannel statusChannel = new EventChannel(registrar.messenger(), "plugins.flutter.io/pubnub_status");

		statusChannel.setStreamHandler(new EventChannel.StreamHandler()
		{
			@Override public void onListen(Object o, EventChannel.EventSink eventSink)
			{
				Log.d(getClass().getName(), "statusChannel.onListen");
				statusSender = eventSink;
			}

			@Override public void onCancel(Object o)
			{
				Log.d(getClass().getName(), "statusChannel.onCancel");
			}
		});
	}

	@Override public void onMethodCall(MethodCall call, Result result)
	{
		switch (call.method)
		{
			case "create":
				createChannel(call, result);
				break;
			case "subscribe":
				subscribeToChannel(call, result);
				break;
			case "unsubscribe":
				unSubscribeFromChannel(call, result);
				break;
			case "message":
				sendMessageToChannel(call, result);
				break;
			default:
				result.notImplemented();
		}
	}

	private void createChannel(MethodCall call, Result result)
	{
		String publishKey = call.argument("publishKey");
		String subscribeKey = call.argument("subscribeKey");
		uuid = java.util.UUID.randomUUID().toString();

		Log.d(getClass().getName(), "Create pubnub with publishKey " + publishKey + ", subscribeKey " + subscribeKey + " uuid" + uuid);

		if ((publishKey != null && !publishKey.isEmpty()) && (subscribeKey != null && !subscribeKey.isEmpty()))
		{
			PNConfiguration pnConfiguration = new PNConfiguration();
			pnConfiguration.setPublishKey(publishKey);
			pnConfiguration.setSubscribeKey(subscribeKey);
			pnConfiguration.setUuid(uuid);
			pnConfiguration.setSecretKey("sec-c-Y2Q3YjAzYTYtNTRjNS00ZTkyLTkyZGItZDdkOGE0ZTczMGY2");
			pnConfiguration.setSecure(true);
			pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);

			pubnub = new PubNub(pnConfiguration);
			Log.d(getClass().getName(), "PubNub configuration created");
			result.success("PubNub configuration created");
		}
		else
		{
			Log.d(getClass().getName(), "Keys should not be null");
			result.success("Keys should not be null");
		}
	}

	private void subscribeToChannel(MethodCall call, final Result result)
	{
		/* Subscribe to the demo_tutorial channel */
		channelName = call.argument("channelName");

		Log.d(getClass().getName(), "Attempt to Subscribe to channel: " + channelName);
		try
		{
			pubnub.addListener(new SubscribeCallback()
			{
				@Override public void status(PubNub pubnub, PNStatus status)
				{
					if (status.getCategory() == PNStatusCategory.PNConnectedCategory)
					{
						Log.d(getClass().getName(), "Subscription was successful at channel " + channelName);
						statusSender.success("Subscription was successful at channel " + channelName);
						messageSender.success("Ready to listen messages");
						result.success(true);
					}
					else
					{
						Log.d(getClass().getName(), "Subscription failed at channe l" + channelName);
						Log.d(getClass().getName(), status.getErrorData().getInformation());
						statusSender.success("Subscription failed at channel " + channelName + "'\n" + status.getErrorData().getInformation());
						result.success(false);
					}
				}

				@Override public void message(PubNub pubnub, PNMessageResult message)
				{
					//If is not your message
					if (message != null && message.getPublisher().compareToIgnoreCase(uuid) != 0)
					{
						try
						{
							Message receivedMessage = new Gson().fromJson(message.getMessage(), Message.class);
							receivedMessage.setChannel(message.getChannel());
							receivedMessage.setPublisher(message.getPublisher());
							messageSender.success(receivedMessage.getMessage());
						}
						catch (Exception e)
						{
							messageSender.success("Failed to parse message");
							e.printStackTrace();
						}
					}
				}

				@Override public void presence(PubNub pubnub, PNPresenceEventResult presence)
				{
					Log.d(getClass().getName(), "Presence: getChannel " + presence.getChannel() + "getEvent " + presence.getEvent() + "getSubscription " + presence.getSubscription() + "getUuid " + presence.getUuid());
				}
			});

			pubnub.subscribe().channels(Arrays.asList(channelName)).execute();
		}
		catch (Exception e)
		{
			Log.d(getClass().getName(), e.getMessage());
			result.success(false);
		}
	}

	private void unSubscribeFromChannel(MethodCall call, final Result result)
	{
		channelName = call.argument("channelName");
		Log.d(getClass().getName(), "Attempt to Unsubscribe to channel: " + channelName);

		try
		{
			pubnub.addListener(new SubscribeCallback()
			{
				@Override public void status(PubNub pubnub, PNStatus status)
				{
					if (status.getCategory() == PNStatusCategory.PNDisconnectedCategory)
					{
						Log.d(getClass().getName(), "Unsubscribe successfully");
						result.success(true);
					}
					else
					{
						Log.d(getClass().getName(), "Unsubscribe failed");
						Log.d(getClass().getName(), status.getErrorData().getInformation());
						result.success(false);
					}
				}

				@Override public void message(PubNub pubnub, PNMessageResult message)
				{
					try
					{
						Message receivedMessage = new Gson().fromJson(message.getMessage(), Message.class);
						receivedMessage.setChannel(message.getChannel());
						receivedMessage.setPublisher(message.getPublisher());
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				@Override public void presence(PubNub pubnub, PNPresenceEventResult presence)
				{
					Log.d(getClass().getName(), "Presence: getChannel " + presence.getChannel() + "getEvent " + presence.getEvent() + "getSubscription " + presence.getSubscription() + "getUuid " + presence.getUuid());
				}
			});

			pubnub.unsubscribe().channels(Arrays.asList(channelName)).execute();
		}
		catch (Exception e)
		{
			Log.d(getClass().getName(), e.getMessage());
			result.success(false);
		}
	}

	private void sendMessageToChannel(MethodCall call, final Result result)
	{
		HashMap<String, String> message = new HashMap<>();
		message.put("sender", (String)call.argument("sender"));
		message.put("message", (String)call.argument("message"));
		message.put("timestamp", DateTimeUtil.getTimeStampUtc());

		pubnub.publish().channel(channelName).message(message).async(
			new PNCallback()
			{
				@Override public void onResponse(Object object, PNStatus status)
				{
					try
					{
						if (!status.isError())
						{
							Log.v(getClass().getName(), "publish(" + object + ")");
							result.success(true);
						}
						else
						{
							Log.v(getClass().getName(), "publishErr(" + status.getErrorData() + ")");
							result.success(false);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
						result.success(false);
					}
				}

			}
		);
	}
}
