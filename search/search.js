function renderDoc(hit) {
  // Check if the path has a leading slash, if so, remove it
  const path = hit.fields.path.startsWith("/") ? hit.fields.path.slice(1) : hit.fields.path
  const htmlPath = `${path}.html`
  const link = new URL(htmlPath, baseUrl)
  const title = hit.highlights["title"] || hit.fields["title"]
  const preview = hit.highlights["body"]
  const score = hit.score.toFixed(4)
  return (
`
<ol>
  <div class="card">
    <div class="card-content">
      <div class="level-left">
        <p class="title is-capitalized is-flex-wrap-wrap">
          <a href="${link}" target="_blank">
            <span>${title}</span>
          </a>
        </p>
      </div>
      <p class="subtitle">${preview}</p>
      <p class="is-size-7 has-text-grey-light">
        <span>score: ${score}</span>
        <span>path: ${path}</span>
      </p>
    </div>
  </div>
</ol>
`
  )
}
function renderScaladoc(hit) {
  const title = hit.fields.functionName
  const description = hit.fields.description
  const returnType = hit.fields.returnType
  const params = hit.fields.params
  return (
`
<ol>
  <div class="card">
    <div class="card-content">
      <div class="level-left">
        <p class="title is-capitalized is-flex-wrap-wrap">
          <span>${title}</span>
        </p>
      </div>
      <p class="subtitle">${description}</p>
      <p class="subtitle">Parameters: ${params}</p>
      <p class="subtitle">Return type: ${returnType}</p>
    </div>
  </div>
</ol>
`
  )
}

async function main() {
  var app = document.getElementById("app")
  var searchBar = document.getElementById("search_input")
  const urlParams = new URLSearchParams(location.search)


  // Optional Scaladoc rendering
  const renderFunction = urlParams.get("type") == "scaladoc" ? renderScaladoc : renderDoc
  urlParams.delete("type")

  // Pass remaining query params to worker.js
  const params = urlParams.toString()
  const workerJS = urlParams.size > 0 ? `worker.js?${params}` : "worker.js"

  const worker = new Worker(workerJS)
  worker.onmessage = function(e) {
    const markup = e.data.map(renderFunction).join("\n")
    app.innerHTML = markup
  }

  searchBar.addEventListener('input', function () {
    worker.postMessage(this.value)
  })

  // If query param `q` is set, use it as query input
  // e.g. search.html?q=hello
  const maybeQuery = urlParams.get("q")
  if (maybeQuery) {
    searchBar.value = maybeQuery
    worker.postMessage(maybeQuery)
  }
}

// Only run once page has finished loading
const baseUrl = new URL("../", document.currentScript.src)
window.onload = function() {
  main()
}
