{%
  pageid = DocsReferenceTopic
%}

# topic

@:example(topic) {
  drawChunked = false
}
## multiple subs

Multiple subscribers will receive the same elements.

@:example(multipleSubscribers) {
  drawChunked = false
}

## delayed sub

Subscribers will only see future messages.
Note that `b` is missed.

@:example(delayedSubscriber) {
  drawChunked = false
}
## metered sub
Unbounded subscribers maintain a buffer of future messages. Note that `b` is present.

@:example(meteredSubscriber) {
  drawChunked = false
}

## bounded sub

If bounded with 0, the subscriber is in sync with the publisher.

@:example(boundedSubscriber) {
  drawChunked = false
}

## bounded sub buffer

The subscriber is behind the publisher.

@:example(boundedSubscriberBuffer) {
  drawChunked = false
}

## bounded sub multiple

If there are multiple subscribers, the slowest one blocks.

The subscriber is behind the publisher.

@:example(multipleSubscribersBounded) {
  drawChunked = false
}

