package com.example.BackendSSA.Controllers;

import com.example.BackendSSA.Services.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    // DTO para recibir
    public static class MessageRequest {
        private String message;

        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
    }

    // DTO para responder
    public static class MessageResponse {
        private String response;

        public MessageResponse(String response) {
            this.response = response;
        }

        public String getResponse() {
            return response;
        }
        public void setResponse(String response) {
            this.response = response;
        }
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody MessageRequest request) {
        try {
            String respuesta = chatbotService.chat(request.getMessage());
            return ResponseEntity.ok(new MessageResponse(respuesta));
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorMap);
        }
    }


    // 🔹 Endpoint general
   /*  @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest request) {
        try {
            String response = chatbotService.processUserMessage(request.getMessage());
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("response", response);
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorMap);
        }
    }

    // 🔹 Endpoint de asistente enfocado (puedes personalizarlo luego)
    @PostMapping("/saga")
    public ResponseEntity<?> sendSagaMessage(@RequestBody MessageRequest request) {
        try {
            String response = chatbotService.processUserMessage(request.getMessage());
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("response", response);
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorMap);
        }
    }

    // 🔹 Endpoint de prueba
    @GetMapping("/test")
    public ResponseEntity<?> testConnection() {
        try {
            String response = chatbotService.processUserMessage("Hola, ¿funcionas correctamente?");
            return ResponseEntity.ok("✅ Conexión exitosa con Gemini. Respuesta: " + response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }
}

// 🧩 Clase auxiliar para recibir mensajes del frontend
class MessageRequest {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }*/
}
