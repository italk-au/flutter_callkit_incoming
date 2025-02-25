import 'dart:async';

import 'package:flutter/services.dart';

import 'entities/entities.dart';

/// Instance to use library functions.
/// * showCallkitIncoming(dynamic)
/// * startCall(dynamic)
/// * endCall(dynamic)
/// * endAllCalls()

class FlutterCallkitIncoming {
  FlutterCallkitIncoming._();

  static FlutterCallkitIncoming? _instance;

  static FlutterCallkitIncoming get instance =>
      _instance ??= FlutterCallkitIncoming._();

  static const MethodChannel _channel =
      MethodChannel('flutter_callkit_incoming');
  static const EventChannel _eventChannel =
      EventChannel('flutter_callkit_incoming_events');

  /// Listen to event callback from [FlutterCallkitIncoming].
  ///
  /// FlutterCallkitIncoming.onEvent.listen((event) {
  /// Event.ACTION_CALL_INCOMING - Received an incoming call
  /// Event.ACTION_CALL_START - Started an outgoing call
  /// Event.ACTION_CALL_ACCEPT - Accepted an incoming call
  /// Event.ACTION_CALL_DECLINE - Declined an incoming call
  /// Event.ACTION_CALL_ENDED - Ended an incoming/outgoing call
  /// Event.ACTION_CALL_TIMEOUT - Missed an incoming call
  /// Event.ACTION_CALL_CALLBACK - only Android (click action `Call back` from missed call notification)
  /// Event.ACTION_CALL_TOGGLE_HOLD - only iOS
  /// Event.ACTION_CALL_TOGGLE_MUTE - only iOS
  /// Event.ACTION_CALL_TOGGLE_DMTF - only iOS
  /// Event.ACTION_CALL_TOGGLE_GROUP - only iOS
  /// Event.ACTION_CALL_TOGGLE_AUDIO_SESSION - only iOS
  /// Event.DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP - only iOS
  /// }
  Stream<CallEvent?> get onEvent =>
      _eventChannel.receiveBroadcastStream().map(_receiveCallEvent);

  /// Show Callkit Incoming.
  /// On iOS, using Callkit. On Android, using a custom UI.
  Future<void> showCallkitIncoming(CallKit params) async {
    await _channel.invokeMethod<void>("showCallkitIncoming", params.toJson());
  }

  /// Show Miss Call Notification.
  /// Only Android
  Future<void> showMissCallNotification(CallKit params) async {
    await _channel.invokeMethod<void>(
        "showMissCallNotification", params.toJson());
  }

  /// Start an Outgoing call.
  /// On iOS, using Callkit(create a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  Future<void> startCall(CallKit params) async {
    await _channel.invokeMethod<void>("startCall", params.toJson());
  }

  /// End an Incoming/Outgoing call.
  /// On iOS, using Callkit(update a history into the Phone app).
  /// On Android, Nothing(only callback event listener).
  Future<void> endCall(String id) async {
    await _channel.invokeMethod<void>("endCall", {'id': id});
  }

  /// End all calls.
  Future<void> endAllCalls() async {
    await _channel.invokeMethod<void>("endAllCalls");
  }

  /// Get active calls.
  /// On iOS: return active calls from Callkit.
  /// On Android: only return last call
  Future<List<CallKit>?> activeCalls() async {
    final jsonList = await _channel.invokeMethod<List<Object?>?>("activeCalls");

    if (jsonList == null) {
      return null;
    }
    return jsonList
        .map((e) => CallKit.fromJson(
            Map<String, dynamic>.from(e as Map<Object?, Object?>)))
        .toList();
  }

  /// Get device push token VoIP.
  /// On iOS: return deviceToken for VoIP.
  /// On Android: return Empty
  Future<String?> getDevicePushTokenVoIP() async {
    return _channel.invokeMethod("getDevicePushTokenVoIP");
  }

  /// Get latest action
  Future<CallEvent?> getLatestEvent() async {
    final event =
        await _channel.invokeMethod<Map<Object?, Object?>?>("getLatestEvent");
    if (event != null) {
      return Future.value(_receiveCallEvent(event));
    } else {
      return null;
    }
  }

  CallEvent? _receiveCallEvent(dynamic data) {
    if (data is! Map) {
      return null;
    }

    final event = Event.values.firstWhere((e) => e.name == data['event']);
    final callkit = CallKit.fromJson(
        Map<String, dynamic>.from(data['body'] as Map<Object?, Object?>));

    return CallEvent(
      event: event,
      callKit: callkit,
    );
  }
}
