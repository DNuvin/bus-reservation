window.onload = function() {
  const ui = SwaggerUIBundle({
    url: "/bus-reservation/swagger-ui/swagger/openapi.json",
    dom_id: '#swagger-ui',
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    layout: "BaseLayout",
    deepLinking: true,
    showExtensions: true,
    showCommonExtensions: true
  });

  window.ui = ui;
};
