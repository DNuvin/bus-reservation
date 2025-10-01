window.onload = function() {
  window.ui = SwaggerUIBundle({
    url: "/bus-reservation/api/openapi.json",   // 👈 your servlet that serves OpenAPI
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout"
  });
};
