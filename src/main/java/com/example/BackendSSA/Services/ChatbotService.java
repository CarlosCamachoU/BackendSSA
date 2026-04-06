package com.example.BackendSSA.Services;

import com.example.BackendSSA.Entities.ProductoEntities;
import com.example.BackendSSA.Repositories.ProductoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Value("${groq.api.key}")
    private String apiKey;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json");

    private final OkHttpClient client = new OkHttpClient();
    private final ProductoRepository productoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatbotService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /**
     * Método principal que usa el controlador.
     * Recibe el mensaje del usuario, busca productos por palabras clave y llama a Groq.
     * Ahora también soporta consultas sobre OFERTAS / PROMOCIONES.
     */
    public String chat(String message) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                return "Error de configuracion: falta la variable GROQ_API_KEY en el backend.";
            }
            // 0) Detectar si el usuario está preguntando por PROMOCIONES / OFERTAS
            boolean esConsultaPromos = isPromotionsQuery(message);

            List<ProductoEntities> productos = Collections.emptyList();

            if (esConsultaPromos) {
                // -----------------------------------------------------------------
                // 🔹 MODO PROMOCIONES: usar SOLO productos en oferta
                // -----------------------------------------------------------------
                List<ProductoEntities> ofertas = productoRepository.findProductosEnOferta();
                if (ofertas != null) {
                    // Limitamos para no inflar contexto
                    int MAX_OFERTAS = 15;
                    productos = ofertas.size() > MAX_OFERTAS
                            ? ofertas.subList(0, MAX_OFERTAS)
                            : ofertas;
                }
            } else {
                // -----------------------------------------------------------------
                // 🔹 MODO NORMAL: búsqueda por palabras clave
                // -----------------------------------------------------------------
                // 1) Obtener palabras clave relevantes del mensaje del usuario
                List<String> keywords = extractKeywords(message);

                // 2) Buscar productos relacionados en la BD usando las keywords
                productos = buscarProductosPorKeywords(keywords);
            }

            boolean hayProductos = productos != null && !productos.isEmpty();

            // 3) Construir contexto de productos (si hay)
            String contextoProductos = buildProductContext(productos);

            // 4) Construir prompts para Groq
            String systemPrompt = buildSystemPrompt();
            String userPrompt;

            if (esConsultaPromos) {
                // Prompt especializado para promociones
                userPrompt = buildUserPromptPromociones(message, contextoProductos, hayProductos);
            } else if (hayProductos) {
                userPrompt = buildUserPromptConContexto(message, contextoProductos);
            } else {
                userPrompt = buildUserPromptSinContexto(message);
            }

            // 5) Crear JSON de la petición a Groq usando Json
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("model", "llama-3.1-8b-instant");

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);

            requestMap.put("messages", List.of(systemMessage, userMessage));

            String jsonBody = objectMapper.writeValueAsString(requestMap);
            RequestBody body = RequestBody.create(JSON_MEDIA, jsonBody);

            Request request = new Request.Builder()
                    .url(GROQ_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                // Logs para depurar
                System.out.println("Groq HTTP status: " + response.code());
                System.out.println("Groq raw body: " + responseBody);

                if (!response.isSuccessful()) {
                    return "❌ Error al llamar al modelo (HTTP " + response.code() + "): " + responseBody;
                }

                return extractContent(responseBody);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error en la comunicación con el asistente. Inténtalo de nuevo en unos momentos.";
        }
    }

    // -------------------------------------------------------------------------
    //  DETECCIÓN DE CONSULTAS SOBRE PROMOCIONES / OFERTAS
    // -------------------------------------------------------------------------

    /**
     * Detecta si el texto del usuario se refiere a promociones, ofertas o descuentos.
     */
    private boolean isPromotionsQuery(String text) {
        if (text == null) return false;
        String normalized = normalize(text);
        return normalized.contains("oferta")
                || normalized.contains("ofertas")
                || normalized.contains("promocion")
                || normalized.contains("promociones")
                || normalized.contains("promo")
                || normalized.contains("descuento")
                || normalized.contains("descuentos");
    }

    // -------------------------------------------------------------------------
    //  BÚSQUEDA POR PALABRAS CLAVE
    // -------------------------------------------------------------------------

    /**
     * Extrae palabras clave del mensaje del usuario.
     * - Pasa a minúsculas
     * - Quita tildes
     * - Elimina palabras vacías (stopwords)
     * - Quita signos de puntuación
     */
    private List<String> extractKeywords(String message) {
        if (message == null || message.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Palabras de relleno muy comunes que no ayudan a buscar
        Set<String> stopwords = new HashSet<>(Arrays.asList(
                "hola", "buenas", "estoy", "buscando", "quiero", "quisiera",
                "dime", "más", "mas", "de", "del", "la", "el", "los", "las",
                "un", "una", "por", "favor", "sobre", "acerca", "me", "ayuda",
                "ayudar", "necesito", "info", "información", "informacion", "porfa",
                "que", "para", "con", "estos", "esas", "este", "esta", "eso",
                // 👉 añadimos también palabras de promo para que NO sean keywords
                "oferta", "ofertas", "promocion", "promociones", "promo", "descuento", "descuentos"
        ));

        // Normalizar: minúsculas, quitar tildes
        String normalized = normalize(message);

        String[] tokens = normalized.split("\\s+");

        List<String> keywords = Arrays.stream(tokens)
                .map(this::cleanToken)
                .filter(t -> t.length() >= 3)             // mínimo 3 letras
                .filter(t -> !stopwords.contains(t))      // quitar palabras vacías
                .distinct()
                .collect(Collectors.toList());

        System.out.println("Keywords extraídas: " + keywords);

        return keywords;
    }

    /**
     * Normaliza una cadena: minúsculas y sin tildes.
     */
    private String normalize(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", ""); // quita marcas diacríticas (tildes)
    }

    /**
     * Limpia un token: elimina puntuación y paréntesis.
     */
    private String cleanToken(String token) {
        if (token == null) return "";
        // quitar caracteres no alfabéticos / numéricos básicos
        return token.replaceAll("[^a-z0-9áéíóúñ]", "").trim();
    }

    /**
     * Busca productos usando cada keyword y combina los resultados sin duplicados.
     */
    private List<ProductoEntities> buscarProductosPorKeywords(List<String> keywords) {
        // Si no hay keywords útiles, devolvemos lista vacía
        if (keywords == null || keywords.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> seenIds = new HashSet<>();
        List<ProductoEntities> resultados = new ArrayList<>();

        // Limitamos tamaño de página por keyword para no explotar el contexto
        Pageable pageable = PageRequest.of(0, 5);

        for (String kw : keywords) {
            List<ProductoEntities> parciales =
                    productoRepository.buscarPorCriterioEnCampos(kw, pageable);

            for (ProductoEntities p : parciales) {
                if (p != null && seenIds.add(p.getIdProducto())) {
                    resultados.add(p);
                }
            }
        }

        System.out.println("Productos encontrados por keywords: " + resultados.size());
        return resultados;
    }

    // -------------------------------------------------------------------------
    //  PROMPTS
    // -------------------------------------------------------------------------

    /**
     * System prompt: define la personalidad y reglas del bot.
     */
   private String buildSystemPrompt() {
    StringBuilder sb = new StringBuilder();
    sb.append("Eres el asistente virtual de una tienda online de moda, tecnología y estilo de vida.\n");
    sb.append("Respondes SIEMPRE en español neutro, de forma clara, breve y amable.\n\n");

    sb.append("Tu objetivo:\n");
    sb.append("- Ayudar al cliente a encontrar productos según lo que busca (ropa, calzado, accesorios, tecnología, electrónica, hogar, etc.).\n");
    sb.append("- Responder preguntas sobre precios, stock disponible, marca, color, talla y categoría.\n");
    sb.append("- Cuando haya un contexto de catálogo, debes basarte SOLO en esa información.\n");
    sb.append("- No inventes productos ni precios. Si no hay productos adecuados en el contexto, dilo claramente.\n\n");

    sb.append("Estilo de respuesta:\n");
    sb.append("- Frases cortas y directas.\n");
    sb.append("- Si hay productos, sugiere de 1 a 3 opciones concretas.\n");
    sb.append("- Cuando menciones precio, usa el formato: S/ xx.xx.\n");
    sb.append("- Asume que YA saludaste al cliente al inicio de la conversación.\n");
    sb.append("- Evita presentarte de nuevo en cada mensaje.\n");
    sb.append("- Solo haz una presentación breve (ej. 'Hola, soy tu asistente virtual...') si el mensaje del cliente es claramente un saludo simple como 'hola', 'buenas', 'buenas tardes' sin más contexto.\n");
    sb.append("- Si el usuario hace una pregunta general (por ejemplo \"¿qué me recomiendas para regalo?\" o \"qué promociones hay?\") usa el contexto disponible para proponer algunas opciones y categorías.\n");
    sb.append("- Si el cliente menciona tecnología o electrónica, usa ejemplos como celulares, audífonos, laptops, tablets, smartwatches, accesorios gamer, etc., no zapatos ni ropa.\n");

    return sb.toString();
}

    /**
     * User prompt cuando SÍ hay productos relacionados (modo normal).
     
    private String buildUserPromptConContexto(String preguntaUsuario, String contextoProductos) {
        StringBuilder sb = new StringBuilder();
        sb.append("A continuación tienes parte del catálogo filtrado de la tienda:\n\n");
        sb.append(contextoProductos).append("\n\n");
        sb.append("Pregunta del cliente:\n");
        sb.append(preguntaUsuario).append("\n\n");
        sb.append("Instrucciones:\n");
        sb.append("- Usa solo los productos del catálogo anterior para responder.\n");
        sb.append("- Menciona nombre del producto, precio y, si es relevante, color y talla.\n");
        sb.append("- No inventes productos, tallas ni precios que no estén en el contexto.\n");
        sb.append("- Propón de 1 a 3 productos que encajen mejor con lo que el cliente pide.\n");
        return sb.toString();
    }*/

    private String buildUserPromptConContexto(String preguntaUsuario, String contextoProductos) {
     StringBuilder sb = new StringBuilder();
     sb.append("A continuación tienes parte del catálogo filtrado de la tienda:\n\n");
     sb.append(contextoProductos).append("\n\n");
     sb.append("Pregunta del cliente:\n");
     sb.append(preguntaUsuario).append("\n\n");
     sb.append("Instrucciones para tu respuesta:\n");
     sb.append("- Responde de forma amable y directa, sin rodeos.\n");
     sb.append("- NO empieces la respuesta con 'Hola', 'Buenas', 'Hola, soy tu asistente', ni ningún saludo ");
     sb.append("si la pregunta del cliente ya es una petición concreta (por ejemplo: 'dame productos sobre tecnología', ");
     sb.append("si el cliente te dice 'dime qué productos tienes' o 'busco zapatillas', ve directo a ayudar).\n");
     sb.append("'muéstrame zapatillas', 'qué ofertas tienes en auriculares').\n");
     sb.append("- Solo puedes usar un saludo breve si el mensaje del cliente es casi exclusivamente un saludo ");
     sb.append("(por ejemplo: 'hola', 'buenas', 'buenas tardes') y nada más.\n");
     sb.append("- Usa SOLO los productos del catálogo anterior para responder.\n");
     sb.append("- Menciona nombre del producto, precio (formato: S/ xx.xx) y, si es relevante, color y talla.\n");
     sb.append("- No inventes productos, tallas ni precios que no estén en el contexto.\n");
     sb.append("- Propón de 1 a 3 productos que encajen mejor con lo que el cliente pide.\n");
     sb.append("- Si el cliente menciona tecnología, prioriza productos tecnológicos del contexto (ej. celulares, auriculares, laptops, smartwatches, accesorios electrónicos).\n");
      return sb.toString();
}

    /**
     * User prompt cuando NO hay productos relacionados (saludo o búsqueda muy general).
     */
    private String buildUserPromptSinContexto(String preguntaUsuario) {
     StringBuilder sb = new StringBuilder();
     sb.append("El cliente ha hecho la siguiente consulta en una tienda online de moda, tecnología y estilo de vida:\n\n");
     sb.append("Pregunta del cliente:\n");
     sb.append(preguntaUsuario).append("\n\n");
     sb.append("Instrucciones para tu respuesta:\n");
     sb.append("- Responde de forma amable y breve (máximo 3 frases).\n");
     sb.append("- NO te presentes otra vez como asistente virtual en cada mensaje.\n");
     sb.append("- Solo puedes presentarte con un saludo inicial si la pregunta del cliente es un saludo muy simple (por ejemplo: 'hola', 'buenas', 'buenas tardes') sin más información.\n");
     sb.append("- Si la pregunta ya es concreta (por ejemplo: 'busco auriculares', 'qué promociones tienes en zapatillas', 'ofertas en celulares'), ve directo a ayudar, SIN presentarte.\n");
     sb.append("- Explica que puedes ayudar a buscar productos, precios y stock, PERO hazlo en una sola frase corta.\n");
     sb.append("- Pídele al cliente que te indique qué tipo de producto busca o qué categoría le interesa (por ejemplo: zapatillas, polos, casacas, celulares, audífonos, laptops, smartwatches, etc.).\n");
     sb.append("- Si el cliente menciona tecnología o electrónica, responde usando ejemplos coherentes con tecnología (celulares, audífonos, laptops, tablets, smartwatches, accesorios gamer, etc.), NO zapatos ni ropa.\n");
     return sb.toString();
   }

    /**
     * User prompt especial cuando el usuario pregunta por PROMOCIONES / OFERTAS.
     */
    private String buildUserPromptPromociones(String preguntaUsuario,
                                              String contextoProductos,
                                              boolean hayProductos) {
        StringBuilder sb = new StringBuilder();

        if (hayProductos) {
            sb.append("A continuación tienes el listado de productos actualmente en OFERTA o con PROMOCIONES activas en la tienda:\n\n");
            sb.append(contextoProductos).append("\n\n");
        } else {
            sb.append("Actualmente no se han encontrado productos en oferta en el catálogo.\n\n");
        }

        sb.append("Pregunta del cliente:\n");
        sb.append(preguntaUsuario).append("\n\n");
        sb.append("Instrucciones:\n");
        sb.append("- Responde hablando explícitamente de las PROMOCIONES u OFERTAS.\n");
        if (hayProductos) {
            sb.append("- Si el cliente menciona un tipo de producto concreto (por ejemplo \"zapatillas\", \"polos\"), ");
            sb.append("menciona solo las ofertas que coincidan con ese tipo (por nombre, categoría o descripción).\n");
            sb.append("- Si no hay ninguna oferta para ese tipo de producto dentro del listado, dilo claramente, ");
            sb.append("por ejemplo: \"Por ahora no tenemos promociones en zapatillas, pero sí en otros productos como ...\".\n");
        } else {
            sb.append("- Indica amablemente que en este momento no hay promociones disponibles o que no se encontraron productos en oferta.\n");
        }
        sb.append("- No inventes productos, tallas ni precios que no estén en el contexto (si hay catálogo).\n");

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    //  CONTEXTO DE PRODUCTOS
    // -------------------------------------------------------------------------

    /**
     * Convierte una lista de ProductoEntities en texto entendible para el modelo.
     * Si no hay productos, devuelve cadena vacía.
     */
    private String buildProductContext(List<ProductoEntities> productos) {
    if (productos == null || productos.isEmpty()) {
        return "";
    }

    // ✅ Ordenar los productos:
    // 1) Por categoría (idCategoria)
    // 2) Dentro de la categoría, por nombre alfabético
    List<ProductoEntities> ordenados = productos.stream()
            .sorted(
                Comparator
                    .comparing((ProductoEntities p) ->
                            p.getIdCategoria() != null ? p.getIdCategoria() : Integer.MAX_VALUE
                    )
                    .thenComparing(p ->
                            p.getNombre() != null
                                    ? p.getNombre().toLowerCase(Locale.ROOT)
                                    : ""
                    )
            )
            .toList();

    StringBuilder sb = new StringBuilder();
    int index = 1;

    for (ProductoEntities p : ordenados) {
        String nombre       = p.getNombre() != null ? p.getNombre() : "Producto sin nombre";
        String marca        = p.getMarca() != null ? p.getMarca() : "Sin marca específica";
        String color        = p.getColor() != null ? p.getColor() : "No especificado";
        String talla        = p.getTalla() != null ? p.getTalla() : "No especificada";
        String estilo       = p.getEstilo() != null ? p.getEstilo() : "No especificado";
        String descripcion  = p.getDescripcion() != null ? p.getDescripcion() : "Sin descripción.";
        Integer stock       = p.getStockActual() != null ? p.getStockActual() : 0;

        // Si tienes campos de oferta:
        BigDecimal precioBase   = p.getPrecioBase() != null ? p.getPrecioBase() : BigDecimal.ZERO;
        BigDecimal precioOferta = null;
        boolean enOferta        = false;

        try {
            // Si tu entidad tiene estos getters, úsalo:
            // (si no, borra esta parte y deja solo precioBase)
            precioOferta = (BigDecimal) ProductoEntities.class
                    .getMethod("getPrecioOferta")
                    .invoke(p);
            Boolean enOfertaObj = (Boolean) ProductoEntities.class
                    .getMethod("getEnOferta")
                    .invoke(p);
            enOferta = enOfertaObj != null && enOfertaObj;
        } catch (Exception ignored) {
            // Si no existen esos métodos, simplemente usamos precioBase
        }

        BigDecimal precioMostrar = (enOferta && precioOferta != null)
                ? precioOferta
                : precioBase;

        String categoriaTexto = p.getIdCategoria() != null
                ? "Categoría ID: " + p.getIdCategoria()
                : "Categoría: sin especificar";

        // 🔹 SIN ID, usando numeración amigable:
        sb.append(index++).append(") ")
          .append(nombre)
          .append(" (").append(marca).append(")")
          .append("\n");

        sb.append("   ").append(categoriaTexto)
          .append(" | Precio actual: S/ ").append(precioMostrar.toPlainString());

        if (enOferta && precioOferta != null) {
            sb.append(" (precio regular S/ ").append(precioBase.toPlainString()).append(")");
        }
        sb.append(" | Stock: ").append(stock).append("\n");

        sb.append("   Color: ").append(color)
          .append(" | Talla: ").append(talla)
          .append(" | Estilo: ").append(estilo).append("\n");

        sb.append("   Descripción: ").append(descripcion).append("\n\n");
    }

    return sb.toString();
}

    // -------------------------------------------------------------------------
    //  LECTURA DE RESPUESTA DE GROQ
    // -------------------------------------------------------------------------

    /**
     * Extrae el "content" de la respuesta de Groq usando Jackson.
     */
    private String extractContent(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            // Si viene un error del lado de Groq
            if (root.has("error")) {
                JsonNode err = root.get("error");
                String msg = err.has("message") ? err.get("message").asText() : "Error desconocido del modelo.";
                return "❌ El modelo devolvió un error: " + msg;
            }

            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return "❌ No se encontró respuesta en el servidor.";
            }

            JsonNode messageNode = choices.get(0).path("message");
            String content = messageNode.path("content").asText(null);

            if (content == null || content.isEmpty()) {
                return "❌ La respuesta del modelo no contiene contenido.";
            }

            return content;

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error al procesar la respuesta del modelo.";
        }
    }
}
