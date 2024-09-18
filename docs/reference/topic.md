{%
  pageid = DocsReferenceTopic
%}

# Topic

A topic allows communication between multiple concurrent streams, termed publishers and subscribers. It allows a single publisher to broadcast messages to multiple subscribers.

This guide describes:

 - The basic behaviour of topics, publishers and subscribers.
 - How subscribers are constructed dynamically.
 - How the bound of a subscriber affects the system.

## Basic behaviour

A topic allows communication between multiple concurrent streams, termed publishers and subscribers. 

 - A publisher is a stream that is piped through `topic.publish`.
 - A subscriber is a stream that is constructed from `topic.subscribe` or `topic.subscribeUnbounded`.
 - A message is an element that is outputted by the publisher stream.
 
We say that messages are sent from the publisher and received by the subscribers. Like all streams, publishers and subscribers are pull-based, iterative processes. The sending and receiving are done through pulls and outputs.

A topic is designed to have one publisher and multiple subscribers. Subscribers can be constructed dynamically, throughout the lifetime of the topic.

The subscribers associated with the topic will all receive the same messages. The topic broadcasts the messages from the publisher to all subscribers.

### A basic publisher and subscriber

In the following example, the `pub` publisher stream is piped through `topic.publish`.  This pipe repeatedly pulls elements from the stream and sends them to the topic.

A subscriber stream `sub` is constructed using the `subscribeUnbounded` function. This repeatedly pulls elements off the topic.

Each stream is compiled into its own separate `IO` effect. The effects are evaluated concurrently with `parTupled`.

The publisher delays by one second before outputting the characters `a` and `b`, which are published to the topic. The subscriber stream is pulled on immediately and receives `a` and `b` after a one second delay.

@:example(topic) {
  drawChunked = false
}

Once the publisher stream is done, the topic is closed and the subscriber stream is terminated.

## Constructing subscribers

Subscribers can be constructed with `topic.subscribe` or `topic.subscribeUnbounded`. They will only receive messages published after their construction. 

In the following example, the publisher publishes `a` after two seconds and `b` after four seconds. Experiment with the time at which the subscriber is constructed.

If the construction time is one second, before `a` is published, then `a` is received. However, if the time is `3` seconds, after `a` is published, then `a` is missed.

@:exampleWithInput(delayedSubscriber) {
  drawChunked = false
}

### Constructing multiple subscribers

Multiple subscribers can be constructed concurrently with `topic.subscribe`. Each subscriber will receive the same messages. 

In the following example, both `subA` and `subB` are constructed.

@:example(multipleSubscribers) {
  drawChunked = false
}

Note that both subscribers receive the messages `a` and `b`.

## Bounds

Each subscriber and publisher is a different iterative process, and may proceed at different rates. A subscriber may pull messages at a rate slower than they are published.

To manage this, each subscriber maintains a buffer of messages that have been sent by the publisher, but not yet pulled. The size of this buffer is known as the bound.

### Unbounded subscribers

If the subscriber is unbounded, it maintains an unlimited buffer.

In the following example, the publisher publishes the messages `a` and `b` after one second.

The subscriber is constructed immediately, and receives `a` after one second. The `spaced` operator then delays for two second before pulling on the subscriber stream again and receiving `b`.

@:example(slowSubscriber) {
  drawChunked = false
}

There is a time delay of two seconds between `b` being published to the topic and received by the subscriber. During this time, the message is kept in the unbounded buffer.

Unbounded subscribers should be used with caution. They can result in memory errors if their buffer grows too large.

### Bounded subscribers

Subscribers constructed with the `subscribe(bound)` function are bounded. They maintain a buffer of messages that is capped by the `bound`.

When their bound is reached, no more messages can be added to the buffer. The `topic.publish` pipe blocks the publisher until a message is removed.

In the following example, the subscriber is constructed with a given bound. The `spaced` operator pulls messages every second.

The publisher stream publishes the messages `a`, `b`, `c` and `d`. The stream is delayed such that `a` is published after one second.

Experiment with different values for the bound.

@:exampleWithInput(boundedSubscriber) {
  drawChunked = false
}

Notice that when the bound is zero, the message `c` are also delayed by one second even though no delay is specified in the publisher stream. This is caused by `topic.publish` blocking when the subscriber has not yet pulled `b`. A bound of zero keeps the publisher and subscriber in sync.

### Subscribers with different bounds

In a system with multiple subscribers, each subscriber can have a different bound. The `topic.publish` operator blocks when any bound is reached. The system progresses as fast as the slowest subscriber.

In the following example, two subscribers are constructed. The `subA` subscriber is unbounded and pulls messages without delay. The `subB` subscriber has a bound of zero, and delays for one second between pulls using the `spaced` operator.

The publisher stream publishes the messages `a`, `b`, `c` and `d`. The stream is delayed such that `a` is published after one second.

@:example(multipleSubscribersBounded) {
  drawChunked = false
}

Note that there is a one second delay before `c` is published. The `topic.publish` operator must block until `subB` pulls `b`. The `subA` subscriber is also delayed because it must wait for `c` to be published.
