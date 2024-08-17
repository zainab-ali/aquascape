var aquascape = (function () {

    /** Draws an aquascape svg in-browser.
     *
     * @param exampleObject An instance of `aquascape.examples.Example`.
     * @param codeId        The html id of the <code> element that should be populated.
     * @param frameIds      An instance of `aquascape.examples.FrameIds`
     */
    function example(exampleObject, codeId, frameIds) {
	highlightExampleCode(codeId);
	exampleObject.draw(codeId, frameIds);
    };

    /** Draws an aquascape svg in-browser with a single user input.
     *
     * The user-input is provided through an <input> element.
     *
     * @param exampleObject An instance of `aquascape.examples.Example`.
     * @param codeId        The html id of the <code> element that should be populated.
     * @param frameIds      An instance of `aquascape.examples.FrameIds`
     * @param labelId       The html id of the <label> element for the input.
     * @param inputId       The html id of the <input> element.
     */
    function exampleWithInput(exampleObject, codeId, frameIds, labelId, inputId) {
	highlightExampleCode(codeId);
	document.getElementById(inputId).addEventListener("input", function (e) {
	    exampleObject.draw(codeId, frameIds, this.value);
	});
	exampleObject.setup(labelId, inputId, codeId, frameIds);
    };

    /** Highlight Scala code examples.
     *
     * The @:example directive fills in <code> blocks
     * asynchronously. This function adds an observer to highlight the
     * <code> block after its text content has been set.
     */
    function highlightExampleCode(codeId) {
        const targetNode = document.getElementById(codeId);
        const config = { attributes: true, childList: true, subtree: false };

        const callback = (mutationList, observer) => {
            hljs.highlightElement(targetNode);
            observer.disconnect();
        };

        const observer = new MutationObserver(callback);
        observer.observe(targetNode, config);
    };

    return {
	example: example,
	exampleWithInput: exampleWithInput
    };
})();
