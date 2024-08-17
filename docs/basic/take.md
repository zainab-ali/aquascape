# take

Experiment with taking a number of elements from an infinite stream.

Note that the resulting stream is finite because a fixed number of elements are pulled.

@:exampleWithInput(TakeFromAnInfiniteStream) {
  drawChunked = false
}


## `take` from finite streams

Experiment with taking a number of elements from a finite stream.

@:exampleWithInput(TakeFromAFiniteStream) {
  drawChunked = false
}

## Drained streams

Drained streams output no elements, but may still capture side-effects.

@:example(TakeFromADrainedStream) {
  drawChunked = false
}
