{%
  pageid = DocsReferenceTopic
%}

# topic

A topic allows communication between multiple concurrent iterative processes, termed publishers and subscribers. 

A publisher stream publishes messages to the topic. Subscriber streams subscribe to the topic to receive messages.

## Basic behaviour

In the following example, one stream iteratively publishes messages to a topic using the `publish` pipe. A subscriber stream is constructed using the `subscribeUnbounded` function.

Each stream is compiled into its own effect. The effects are evaluated concurrently with `parTupled`.

The publisher stream delays by one second before publishing the characters `a` and `b` to the topic. The subscriber stream is pulled on immediately. After a one second delay, `a` and `b` are outputted.

@:example(topic) {
  drawChunked = false
}

Note that all messages published, `a` and `b`, are received by the subscriber.

Once the publisher stream is done, the topic is closed and the subscriber stream is also terminated. The `publish` pipe should therefore only be used in a single publisher system.

## Subscribers only see future messages

Subscribers will only see future messages. 

In the following example, the publisher publishes `a` after two seconds and `b` after four seconds. Experiment with the time at which the subscriber subscribes to the topic.

If the subscription time is one second, before `a` is published, then `a` is received. However, if the time is `3` seconds, after `a` is published, then `a` is missed.

@:exampleWithInput(delayedSubscriber) {
  drawChunked = false
}

## Multiple subscribers can subscribe

A topic can support multiple subscribers. These will each receive messages.

@:example(multipleSubscribers) {
  drawChunked = false
}

Both subscribers receive the messages `a` and `b`.

## Bounds

Because each subscriber and publisher is a different iterative process, they may proceed at different rates.

To manage this, each subscriber maintains a buffer of future messages. The size of this buffer is known as the bound.

### Unbounded subscribers

If the subscriber is unbounded, it maintains a buffer of all future messages.

In the following example, the publisher publishes the messages `a` and `b` after one second.

The subscriber subscribes to the topic immediately, and receives `a` after one second. The `spaced` operator then delays for two second before pulling on the subscriber stream again and receiving `b`.

@:example(slowSubscriber) {
  drawChunked = false
}

There is a time delay of two seconds between `b` being published to the topic and received by the subscriber. During this time, the message is kept in the unbounded buffer.

Unbounded subscribers should be used with caution as they can result in memory errors if their buffer grows too large.

### Bounded subscribers

Subscribers constructed with the `subscribe` function are bounded. They maintain a buffer of messages that is capped by the bound.

When their bound is reached, no more messages can be added to the buffer. The `topic.publish` operator blocks until a message is removed.

If a bound of zero is set, the publisher is kept in sync with the subscriber.

In the following example, the subscriber is constructed with a given bound. The `spaced` operator pulls messages every second.

The publisher stream publishes the messages `a`, `b`, `c` and `d`. The stream is delayed such that `a` is published after one second.

Experiment with different values for the bound.

@:exampleWithInput(boundedSubscriber) {
  drawChunked = false
}

Notice that when the bound is zero, the message `c` are also delayed by one second even though no delay is specified in the publisher stream. This is caused by `topic.publish` blocking when the subscriber has not yet pulled `b`.

### Subscribers with different bounds

In a system with multiple subscribers, each subscriber can have a different bound. The `topic.publish` operator blocks when any bound is reached. The system progresses as fast as the slowest subscriber.

In the following example, two subscribers are constructed. The `subA` subscriber is unbounded and pulls messages without delay. The `subB` subscriber has a bound of zero, and delays for one second between pulls using the `spaced`operator.

The publisher stream publishes the messages `a`, `b`, `c` and `d`. The stream is delayed such that `a` is published after one second.

@:example(multipleSubscribersBounded) {
  drawChunked = false
}

There is a one second delay before `c` is published. The `topic.publish` operator must block until `subB` pulls `b`. This delay is propagated to the `subA` subscriber: it must wait for one second before it receives `c`.
