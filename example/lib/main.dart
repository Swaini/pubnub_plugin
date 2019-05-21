import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:pubnub_plugin/pubnub_plugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override MyAppState createState() => MyAppState();
}

class MyAppState extends State<MyApp> {
  PubnubPlugin pubNubFlutter;
  String receivedStatus = 'Status: Unknown';
  String receivedMessage = '';
  String sendMessage = '';

  @override void initState() {
    super.initState();
    pubNubFlutter = PubnubPlugin("pub-c-123", "sub-c-123", "sec-c-123");

    pubNubFlutter.onStatusReceived.listen((status) {
      setState(() {
        receivedStatus = status;
      });
    });
    pubNubFlutter.onMessageReceived.listen((message) {
      setState(() {
        receivedMessage = message;
      });
    });
  }

  @override Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('PubNub'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              new Expanded(
                child: new Text(
                  receivedStatus,
                  style: new TextStyle(color: Colors.black45),
                ),
              ),
              new Expanded(
                child: new Text(
                  receivedMessage,
                  style: new TextStyle(color: Colors.black45),
                ),
              ),
              TextField(
                maxLength: 80,
                onChanged: (text){
                  sendMessage = text;
                },
                decoration: InputDecoration(
                  border: OutlineInputBorder(),
                  hintText: "Message to send",
                  hintStyle: TextStyle(fontWeight: FontWeight.w300, color: Colors.grey)

                ),
                style: TextStyle(color: Colors.black, fontWeight: FontWeight.w300),
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: <Widget>[
                  FlatButton(color: Colors.black12, onPressed: () {
                    pubNubFlutter.unsubscribe("phonics");
                  },
                    child: Text("Unsubscribe")),
                  FlatButton(color: Colors.black12, onPressed: () {
                    pubNubFlutter.subscribe("phonics");
                  },
                    child: Text("Subscribe"))
                ]),
              FlatButton(color: Colors.black12, onPressed: () {
                pubNubFlutter.sendMessage(sendMessage);
              },
                child: Text("Send Message")),
            ],)
        ),
      ),
    );
  }
}
