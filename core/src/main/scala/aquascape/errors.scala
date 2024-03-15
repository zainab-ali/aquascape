package aquascape

case class ParentBranchNotFound(parent: String, child: String)
    extends Throwable(
      s"Parent branch `${parent}` not found. Check that the call to `fork($parent, $child)` is correct."
    )

object MissingStageException
      extends Throwable(
        "A stage is missing. Did you forget to `compileStage` ?"
      )
