import 'dart:async';

import 'package:flutter/services.dart';

/** PubnubPlugin */
class PubnubPlugin {
  static var messageReceived;
  static var statusReceived;

  static const MethodChannel channelPubNub = const MethodChannel('pubnub');
  static const EventChannel messageChannel = const EventChannel('plugins.flutter.io/pubnub_message');
  static const EventChannel statusChannel = const EventChannel('plugins.flutter.io/pubnub_status');

  PubnubPlugin(String publishKey, String subscribeKey, String secretkey) {
    var args = {
      'publishKey': publishKey,
      'subscribeKey': subscribeKey
    };
    channelPubNub.invokeMethod('create', args);
  }

  unsubscribe(String channel)  {
    Object result = new Object();
    var args = {
      'channelName': channel
    };
    if (channelPubNub != null) {
       channelPubNub.invokeMethod('unsubscribe', args);
    }
    else {
      new NullThrownError();
    }
  }

  subscribe(String channel) {
    var args = {
      'channelName': channel
    };
    if (channelPubNub != null) {
      channelPubNub.invokeMethod('subscribe', args);
    }
    else {
      new NullThrownError();
    }
  }

  sendMessage(String message) {
    var args = {
      'sender': 'Flutter',
      'message': message
    };
    if (channelPubNub != null) {
      channelPubNub.invokeMethod('message', args);
    }
    else {
      new NullThrownError();
    }
  }

  /// Fires whenever the a message is received.
  Stream<dynamic> get onMessageReceived {
    if (messageReceived == null) {
      messageReceived = messageChannel
        .receiveBroadcastStream()
        .map((dynamic event) => _parseMessage(event));
    }
    return messageReceived;
  }

  /// Fires whenever the status changes.
  Stream<dynamic> get onStatusReceived {
    if (statusReceived == null) {
      statusReceived = statusChannel
        .receiveBroadcastStream()
        .map((dynamic event) => _parseStatus(event));
    }
    return statusReceived;
  }

  dynamic _parseMessage(message) {
    return message;
  }

  dynamic _parseStatus(status) {
    return status;
  }
}
