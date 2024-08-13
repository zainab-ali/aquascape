var hljsWrapper = (function () {
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
	highlightExampleCode: highlightExampleCode
    };
})();
