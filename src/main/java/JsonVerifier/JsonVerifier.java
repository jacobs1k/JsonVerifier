package JsonVerifier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

public class JsonVerifier {
    private final ObjectMapper objectMapper;

    public JsonVerifier(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean verifyJson(String filePath) throws JsonVerificationException {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new JsonVerificationException("File not found: " + filePath);
            }

            JsonNode rootNode = objectMapper.readTree(file);

            if (!rootNode.has("PolicyDocument")) {
                throw new JsonVerificationException("JSON is missing 'PolicyDocument' field.");
            }

            JsonNode policyDocumentNode = rootNode.path("PolicyDocument");
            JsonNode statements = policyDocumentNode.path("Statement");

            if (statements.isEmpty()) {
                throw new JsonVerificationException("No statements found in JSON.");
            }

            for (JsonNode statement : statements) {
                if (!statement.has("Resource")) {
                    throw new JsonVerificationException("Statement is missing 'Resource' field.");
                }

                String resource = statement.path("Resource").asText();

                if ("*".equals(resource)) {
                  return false;
                }
            }
        } catch (IOException e) {
            throw new JsonVerificationException("Error reading JSON file: " + e.getMessage(), e);
        }

        return true;
    }

}