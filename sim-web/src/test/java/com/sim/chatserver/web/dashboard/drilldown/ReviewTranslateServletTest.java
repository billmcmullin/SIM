package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sim.chatserver.service.translation.TranslationResult;
import com.sim.chatserver.service.translation.TranslationService;
/**
 * Parasoft Jtest UTA: Test class for ReviewTranslateServlet
 *
 * @see com.sim.chatserver.web.dashboard.drilldown.ReviewTranslateServlet
 * @author bmcmullin
 */
public class ReviewTranslateServletTest
{

    @Test
    public void manualSourceAndOriginalTextReachTranslationService() throws Exception
    {
        assertTranslationPayload("ja");
    }

    @Test
    public void omittedSourceDefaultsToAutoDetection() throws Exception
    {
        assertTranslationPayload(null);
    }

    private void assertTranslationPayload(String sourceLang) throws Exception
    {
        String text = "  SELECT test_id \u2460 \uff21 \u00c3 \u3053\u308c\u3092\u5b9f\u884c\r\n  ";
        jakarta.json.JsonObjectBuilder payload = jakarta.json.Json.createObjectBuilder().add("text", text).add("targetLang", "en");
        if (sourceLang != null) {
            payload.add("sourceLang", sourceLang);
        }
        String body = payload.build().toString();
        String expectedSource = sourceLang == null ? "auto" : sourceLang;
        TranslationService service = mock(TranslationService.class);
        TranslationResult translation = mock(TranslationResult.class);
        when(translation.isSuccess()).thenReturn(true);
        when(translation.getSourceLang()).thenReturn(expectedSource);
        when(translation.getTargetLang()).thenReturn("en");
        when(translation.getTranslatedText()).thenReturn("Translated text");
        when(service.detectAndTranslate(text, expectedSource, "en")).thenReturn(translation);
        ReviewTranslateServlet servlet = new ReviewTranslateServlet() {
            @Override
            TranslationService translationService() {
                return service;
            }
        };
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Object());
        when(request.getContentLengthLong()).thenReturn((long) body.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        when(request.getReader()).thenReturn(new java.io.BufferedReader(new java.io.StringReader(body)));
        HttpServletResponse response = mock(HttpServletResponse.class);
        java.io.StringWriter output = new java.io.StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(output));

        servlet.doPost(request, response);

        verify(service).detectAndTranslate(text, expectedSource, "en");
        try (jakarta.json.JsonReader reader = jakarta.json.Json.createReader(new java.io.StringReader(output.toString()))) {
            jakarta.json.JsonObject result = reader.readObject();
            assertEquals("ok", result.getString("status"));
            assertEquals(expectedSource, result.getString("sourceLang"));
            assertEquals("Translated text", result.getString("translatedText"));
        }
    }

    @Test
    public void oversizedRequestBodyIsRejectedEvenWhenDeclaredLengthIsSmall() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentLengthLong()).thenReturn(1L);
        when(request.getReader()).thenReturn(new java.io.BufferedReader(new java.io.StringReader("a".repeat(16385))));
        java.lang.reflect.Method method = ReviewTranslateServlet.class.getDeclaredMethod("readRequestBody", HttpServletRequest.class);
        method.setAccessible(true);

        java.lang.reflect.InvocationTargetException failure = assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(new ReviewTranslateServlet(), request));
        assertEquals(IllegalArgumentException.class, failure.getCause().getClass());
    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.ReviewTranslateServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        ReviewTranslateServlet underTest = new ReviewTranslateServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.ReviewTranslateServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        ReviewTranslateServlet underTest = new ReviewTranslateServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.ReviewTranslateServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        ReviewTranslateServlet underTest = new ReviewTranslateServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.drilldown.ReviewTranslateServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        ReviewTranslateServlet underTest = new ReviewTranslateServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

}
