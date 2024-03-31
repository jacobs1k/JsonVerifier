import JsonVerifier.JsonVerifier;
import JsonVerifier.JsonVerificationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JsonVerifierTests {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JsonNode rootNode;

    @Mock
    private JsonNode statementNode;

    private JsonVerifier verifier;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        verifier = new JsonVerifier(objectMapper);
    }

    @Test
    public void testVerifyJson_FileNotFound() {
        assertThrows(JsonVerificationException.class, () -> verifier.verifyJson("nonexistent.json"));
    }

    @Test
    public void testVerifyJson_MissingPolicyDocument() throws IOException {
        when(objectMapper.readTree(any(File.class))).thenReturn(rootNode);
        when(rootNode.has("PolicyDocument")).thenReturn(false);

        JsonVerificationException exception = assertThrows(JsonVerificationException.class,
                () -> verifier.verifyJson("src/test/java/missing_policy_document.json"));
        assertEquals("JSON is missing 'PolicyDocument' field.", exception.getMessage());
    }


    @Test
    public void testVerifyJson_NoStatements() throws IOException {
        when(objectMapper.readTree(any(File.class))).thenReturn(rootNode);
        when(rootNode.has("PolicyDocument")).thenReturn(true);

        JsonNode policyDocumentNode = mock(JsonNode.class);
        when(rootNode.path("PolicyDocument")).thenReturn(policyDocumentNode);

        JsonNode emptyStatementNode = mock(JsonNode.class);
        when(policyDocumentNode.path("Statement")).thenReturn(emptyStatementNode);

        when(emptyStatementNode.isEmpty()).thenReturn(true);

        JsonVerificationException exception = assertThrows(JsonVerificationException.class,
                () -> verifier.verifyJson("src/test/java/no_statements.json"));
        assertEquals("No statements found in JSON.", exception.getMessage());
    }


    @Test
    public void testVerifyJson_StatementMissingResource() throws IOException {
        when(objectMapper.readTree(any(File.class))).thenReturn(rootNode);
        when(rootNode.has("PolicyDocument")).thenReturn(true);

        JsonNode policyDocumentNode = mock(JsonNode.class);
        when(rootNode.path("PolicyDocument")).thenReturn(policyDocumentNode);

        statementNode = mock(JsonNode.class);
        when(policyDocumentNode.path("Statement")).thenReturn(statementNode);

        Iterator<JsonNode> iterator = mock(Iterator.class);
        when(statementNode.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(mock(JsonNode.class));

        JsonNode singleStatement = mock(JsonNode.class);
        when(singleStatement.has("Resource")).thenReturn(false);

        when(iterator.next()).thenReturn(singleStatement);

        JsonVerificationException exception = assertThrows(JsonVerificationException.class,
                () -> verifier.verifyJson("src/test/java/statement_missing_resource.json"));
        assertEquals("Statement is missing 'Resource' field.", exception.getMessage());
    }


    @Test
    public void testVerifyJson_ValidJson() throws IOException, JsonVerificationException {
        when(objectMapper.readTree(any(File.class))).thenReturn(rootNode);
        when(rootNode.has("PolicyDocument")).thenReturn(true);

        JsonNode policyDocumentNode = mock(JsonNode.class);
        when(rootNode.path("PolicyDocument")).thenReturn(policyDocumentNode);

        statementNode = mock(JsonNode.class);
        when(policyDocumentNode.path("Statement")).thenReturn(statementNode);

        Iterator<JsonNode> iterator = mock(Iterator.class);
        when(statementNode.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(mock(JsonNode.class));

        JsonNode singleStatement = mock(JsonNode.class);
        when(singleStatement.has("Resource")).thenReturn(true);
        JsonNode resourceNode = mock(JsonNode.class);
        when(singleStatement.path("Resource")).thenReturn(resourceNode);
        when(resourceNode.asText()).thenReturn("example");

        when(iterator.next()).thenReturn(singleStatement);

        assertTrue(verifier.verifyJson("src/test/java/valid_json.json"));
    }


    @Test
    public void testVerifyJson_InvalidResource() throws IOException, JsonVerificationException {
        when(objectMapper.readTree(any(File.class))).thenReturn(rootNode);
        when(rootNode.has("PolicyDocument")).thenReturn(true);

        JsonNode policyDocumentNode = mock(JsonNode.class);
        when(rootNode.path("PolicyDocument")).thenReturn(policyDocumentNode);

        statementNode = mock(JsonNode.class);
        when(policyDocumentNode.path("Statement")).thenReturn(statementNode);

        Iterator<JsonNode> iterator = mock(Iterator.class);
        when(statementNode.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(mock(JsonNode.class));

        JsonNode singleStatement = mock(JsonNode.class);
        when(singleStatement.has("Resource")).thenReturn(true);
        JsonNode resourceNode = mock(JsonNode.class);
        when(singleStatement.path("Resource")).thenReturn(resourceNode);
        when(resourceNode.asText()).thenReturn("*");

        when(iterator.next()).thenReturn(singleStatement);

        assertFalse(verifier.verifyJson("src/test/java/asteriks_json.json"));
    }
}
