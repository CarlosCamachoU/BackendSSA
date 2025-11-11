package com.example.BackendSSA.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ChatbotService {

        private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
        private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        private final OkHttpClient client;
        private final ObjectMapper objectMapper;
        private final String apiKey;

        public ChatbotService(@Value("${groq.api.key}") String apiKey) {
                if (apiKey == null || apiKey.isBlank()) {
                        throw new IllegalArgumentException(
                                        "Falta la clave GROQ_API_KEY en el archivo de configuración.");
                }

                this.apiKey = apiKey.trim();
                this.objectMapper = new ObjectMapper();
                this.client = new OkHttpClient.Builder()
                                .connectTimeout(30, TimeUnit.SECONDS)
                                .readTimeout(60, TimeUnit.SECONDS)
                                .writeTimeout(30, TimeUnit.SECONDS)
                                .build();
        }

        // 🔹 Chat especializado: asistente del proyecto SSA de SAGA Falabella
        public String sendSagaGuideMessage(String userMessage) throws IOException {
                if (userMessage == null || userMessage.isBlank()) {
                        throw new IllegalArgumentException("El mensaje del usuario no puede estar vacío.");
                }

                // Cargar DTOs y Entities del proyecto
                String dtoDefinitions = readCodeFiles("src/main/java/com/example/BackendSSA/Dtos");
                String entityDefinitions = readCodeFiles("src/main/java/com/example/BackendSSA/Entities");

                if ((dtoDefinitions.contains("⚠️") && entityDefinitions.contains("⚠️")) ||
                                (dtoDefinitions.isBlank() && entityDefinitions.isBlank())) {
                        throw new IOException("No se encontraron definiciones válidas de DTOs o Entities.");
                }
                String systemPrompt = """
                                Eres el asistente técnico del proyecto **SSA (Sistema de Sugerencias Automatizadas)** desarrollado para **SAGA Falabella**.

                                Tu función es ayudar a comprender el funcionamiento del sistema de recomendaciones, su lógica general, sus entidades de negocio, y la estructura de los datos que maneja.
                                Tienes acceso al conocimiento técnico del proyecto (DTOs, Entities y sus relaciones), pero **nunca debes mencionar los nombres de archivos, clases, rutas o detalles del código fuente**.

                                Responde siempre de manera clara, concisa y profesional, adaptando tus explicaciones al nivel de un equipo de desarrollo o documentación técnica.

                                🧩 **Reglas de comportamiento:**
                                - Solo puedes responder sobre temas relacionados con el sistema SSA, su arquitectura, funcionamiento interno, y su propósito como asistente de recomendaciones de productos.
                                - Si el usuario pregunta algo que no esté relacionado con el proyecto (por ejemplo, sobre deportes, política o temas generales), responde amablemente:
                                  👉 *"Solo puedo responder sobre el proyecto SSA y su sistema de recomendaciones."*
                                - Si se te pide información técnica, explica el concepto basándote en tu conocimiento interno, **sin citar archivos o clases**.
                                - Mantén un tono profesional y colaborativo.

                                Tu conocimiento actual incluye los modelos de datos y componentes del sistema (DTOs y Entities):

                                [Información interna cargada del sistema SSA]
                                DTOs:
                                %s

                                Entities:
                                %s
                                """

                                .formatted(dtoDefinitions, entityDefinitions);

                String jsonBody = String.format("""
                                {
                                    "model": "llama-3.1-8b-instant",
                                    "messages": [
                                        { "role": "system", "content": "%s" },
                                        { "role": "user", "content": "%s" }
                                    ],
                                    "temperature": 0.5,
                                    "max_tokens": 512
                                }
                                """, escapeJson(systemPrompt), escapeJson(userMessage));

                return executeGroqRequest(jsonBody);
        }

        // 🧩 Lee archivos Java de un directorio
        private String readCodeFiles(String directoryPath) {
                try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
                        List<String> javaFiles = paths
                                        .filter(Files::isRegularFile)
                                        .filter(p -> p.toString().endsWith(".java"))
                                        .map(path -> {
                                                try {
                                                        return "📄 " + path.getFileName() + ":\n"
                                                                        + Files.readString(path);
                                                } catch (IOException e) {
                                                        return "⚠️ Error al leer " + path.getFileName() + ": "
                                                                        + e.getMessage();
                                                }
                                        })
                                        .collect(Collectors.toList());
                        return String.join("\n\n", javaFiles);
                } catch (IOException e) {
                        return "⚠️ No se pudieron leer los archivos de " + directoryPath + ": " + e.getMessage();
                }
        }

        // 🔧 Ejecuta la solicitud a Groq
        private String executeGroqRequest(String jsonBody) throws IOException {
                RequestBody body = RequestBody.create(jsonBody, JSON);
                Request request = new Request.Builder()
                                .url(GROQ_API_URL)
                                .addHeader("Authorization", "Bearer " + apiKey)
                                .addHeader("Content-Type", "application/json")
                                .post(body)
                                .build();

                try (Response response = client.newCall(request).execute()) {
                        String responseBody = response.body() != null ? response.body().string() : "";

                        if (!response.isSuccessful()) {
                                throw new IOException("Error en la petición: " + response.code() + " - "
                                                + response.message() + "\nRespuesta: " + responseBody);
                        }

                        JsonNode jsonNode = objectMapper.readTree(responseBody);
                        JsonNode choices = jsonNode.path("choices");

                        if (choices.isEmpty() || choices.get(0).path("message").path("content").isMissingNode()) {
                                return "⚠️ No se recibió una respuesta válida del modelo.";
                        }

                        return choices.get(0).path("message").path("content").asText();
                }
        }

        // 🔧 Escapa correctamente caracteres especiales y saltos de línea para JSON
        private String escapeJson(String text) {
                if (text == null)
                        return "";
                return text
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\r", "\\r")
                                .replace("\n", "\\n")
                                .replace("\t", "\\t");
        }

}
