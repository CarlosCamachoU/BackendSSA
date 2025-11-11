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

    // 🔹 Endpoint general (modo libre)
    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest request) {
        try {
            String response = chatbotService.sendSagaGuideMessage(request.getMessage());
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("response", response);
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            e.printStackTrace(); // 👈 Para ver el error exacto en consola
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorMap);
        }
    }

    // 🔹 Endpoint del asistente SAGA Falabella (modo enfocado)
    @PostMapping("/saga")
    public ResponseEntity<?> sendSagaMessage(@RequestBody MessageRequest request) {
        try {
            String response = chatbotService.sendSagaGuideMessage(request.getMessage());
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

    // 🔹 Endpoint de prueba de conexión
    @GetMapping("/test")
    public ResponseEntity<?> testConnection() {
        try {
            String response = chatbotService.sendSagaGuideMessage("Di 'Hola' si funcionas correctamente");
            return ResponseEntity.ok("✅ Conexión exitosa con Groq. Respuesta: " + response);
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
    }
}
