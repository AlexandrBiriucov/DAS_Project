package com.faf.jiggly.pocket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // 32 zero bytes, base64. Test key only, never use it for real data.
                "jiggly.encryption-key=FSC4ukfGdXLhZkfugiE+MukosyckKOospVojwqYwa1Y=",
                "jiggly.storage-dir=target/test-uploads",
                // tests use plain http, and a Secure cookie would not be sent over http
                "server.servlet.session.cookie.secure=false",
                "spring.servlet.multipart.max-file-size=1MB",
                "spring.servlet.multipart.max-request-size=1MB",
        })
class ApiFlowTests {

    private static final String PASSWORD = "supersecret123";

    @Value("${local.server.port}")
    private int port;

    /** One "user" = one HTTP client with its own cookie jar. */
    private HttpClient newClient() {
        return HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@test.com";
    }

    private HttpResponse<byte[]> postJson(HttpClient client, String path, String json) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    private HttpResponse<byte[]> register(HttpClient client, String email, String password) throws Exception {
        return postJson(client, "/auth/register",
                "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");
    }

    private HttpResponse<byte[]> login(HttpClient client, String email, String password) throws Exception {
        return postJson(client, "/auth/login",
                "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");
    }

    private HttpResponse<byte[]> get(HttpClient client, String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(url(path))).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    private HttpResponse<byte[]> upload(HttpClient client, String filename, byte[] content) throws Exception {
        var boundary = "----test" + UUID.randomUUID();
        var out = new ByteArrayOutputStream();
        out.write(("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(content);
        out.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        var request = HttpRequest.newBuilder(URI.create(url("/files/upload")))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray()))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    private static String body(HttpResponse<byte[]> response) {
        return new String(response.body(), StandardCharsets.UTF_8);
    }

    private static String extractId(HttpResponse<byte[]> response) {
        var matcher = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"").matcher(body(response));
        assertThat(matcher.find()).as("response has an id: " + body(response)).isTrue();
        return matcher.group(1);
    }

    // ---------- registration ----------

    @Test
    void registerCreatesUser() throws Exception {
        var response = register(newClient(), uniqueEmail(), PASSWORD);
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(extractId(response)).isNotBlank();
    }

    @Test
    void registerSameEmailTwiceGivesConflict() throws Exception {
        var email = uniqueEmail();
        assertThat(register(newClient(), email, PASSWORD).statusCode()).isEqualTo(201);
        assertThat(register(newClient(), email, PASSWORD).statusCode()).isEqualTo(409);
    }

    @Test
    void registerRejectsShortPassword() throws Exception {
        assertThat(register(newClient(), uniqueEmail(), "123").statusCode()).isEqualTo(400);
    }

    @Test
    void registerRejectsInvalidEmail() throws Exception {
        assertThat(register(newClient(), "not-an-email", PASSWORD).statusCode()).isEqualTo(400);
    }

    // ---------- login and session ----------

    @Test
    void loginWithCorrectPasswordWorks() throws Exception {
        var client = newClient();
        var email = uniqueEmail();
        register(client, email, PASSWORD);

        assertThat(login(client, email, PASSWORD).statusCode()).isEqualTo(200);
    }

    @Test
    void loginWithWrongPasswordOrUnknownEmailIsRejected() throws Exception {
        var client = newClient();
        var email = uniqueEmail();
        register(client, email, PASSWORD);

        var wrongPassword = login(client, email, "wrongpassword");
        var unknownEmail = login(client, uniqueEmail(), PASSWORD);

        assertThat(wrongPassword.statusCode()).isEqualTo(401);
        assertThat(unknownEmail.statusCode()).isEqualTo(401);
        // same message in both cases, so nobody can find out which emails exist
        assertThat(body(wrongPassword)).isEqualTo(body(unknownEmail));
    }

    @Test
    void sessionCookieHasSecurityFlags() throws Exception {
        var client = newClient();
        var email = uniqueEmail();
        register(client, email, PASSWORD);

        var response = login(client, email, PASSWORD);
        var cookie = response.headers().allValues("Set-Cookie").stream()
                .filter(c -> c.startsWith("JSESSIONID"))
                .findFirst().orElseThrow();

        assertThat(cookie).containsIgnoringCase("HttpOnly");
        assertThat(cookie).containsIgnoringCase("SameSite=Strict");
        // Secure is switched off in this test because it uses plain http
    }

    @Test
    void meRequiresLoginAndLogoutEndsTheSession() throws Exception {
        var client = newClient();
        var email = uniqueEmail();
        register(client, email, PASSWORD);

        assertThat(get(client, "/auth/me").statusCode()).isEqualTo(401);   // not logged in yet

        login(client, email, PASSWORD);
        assertThat(get(client, "/auth/me").statusCode()).isEqualTo(200);   // logged in

        postJson(client, "/auth/logout", "{}");
        assertThat(get(client, "/auth/me").statusCode()).isEqualTo(401);   // session destroyed
    }

    // ---------- upload and download ----------

    @Test
    void uploadAndDownloadRequireLogin() throws Exception {
        var anonymous = newClient();

        assertThat(upload(anonymous, "a.txt", "data".getBytes()).statusCode()).isEqualTo(401);
        assertThat(get(anonymous, "/files/" + UUID.randomUUID()).statusCode()).isEqualTo(401);
    }

    @Test
    void ownerCanUploadAndDownloadTheSameContent() throws Exception {
        var client = newClient();
        var email = uniqueEmail();
        register(client, email, PASSWORD);
        login(client, email, PASSWORD);

        var original = ("round trip " + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8);

        var uploaded = upload(client, "hello.txt", original);
        assertThat(uploaded.statusCode()).isEqualTo(201);

        var downloaded = get(client, "/files/" + extractId(uploaded));
        assertThat(downloaded.statusCode()).isEqualTo(200);
        assertThat(downloaded.body()).isEqualTo(original);
    }

    @Test
    void fileOnDiskIsEncrypted() throws Exception {
        var client = newClient();
        var email = uniqueEmail();
        register(client, email, PASSWORD);
        login(client, email, PASSWORD);

        var secret = "TOP-SECRET-" + UUID.randomUUID();
        assertThat(upload(client, "secret.txt", secret.getBytes(StandardCharsets.UTF_8)).statusCode())
                .isEqualTo(201);

        try (var files = Files.list(Path.of("target/test-uploads"))) {
            var stored = files.toList();
            assertThat(stored).isNotEmpty();
            for (var file : stored) {
                var content = new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1);
                assertThat(content).doesNotContain(secret);   // no readable text on disk
            }
        }
    }

    @Test
    void anotherUserCannotDownloadMyFile() throws Exception {
        var ana = newClient();
        var anaEmail = uniqueEmail();
        register(ana, anaEmail, PASSWORD);
        login(ana, anaEmail, PASSWORD);
        var fileId = extractId(upload(ana, "private.txt", "ana's data".getBytes()));

        var bob = newClient();
        var bobEmail = uniqueEmail();
        register(bob, bobEmail, PASSWORD);
        login(bob, bobEmail, PASSWORD);

        assertThat(get(bob, "/files/" + fileId).statusCode()).isEqualTo(404);
        assertThat(get(ana, "/files/" + fileId).statusCode()).isEqualTo(200);   // the owner still can
    }

    @Test
    void unknownOrInvalidFileIdGivesNotFound() throws Exception {
        var client = newClient();
        var email = uniqueEmail();
        register(client, email, PASSWORD);
        login(client, email, PASSWORD);

        assertThat(get(client, "/files/" + UUID.randomUUID()).statusCode()).isEqualTo(404);
        assertThat(get(client, "/files/not-a-uuid").statusCode()).isEqualTo(404);
    }

    //  validation

    private HttpClient loggedInClient() throws Exception {
        var client = newClient();
        var email = uniqueEmail();
        register(client, email, PASSWORD);
        login(client, email, PASSWORD);
        return client;
    }

    @Test
    void textDisguisedAsImageIsRejected() throws Exception {
        var response = upload(loggedInClient(), "evil.png", "just text, not a png".getBytes());
        assertThat(response.statusCode()).isEqualTo(415);
    }

    @Test
        void executableDisguisedAsTextFileIsRejected() throws Exception {
        var fakeExe = new byte[512];
        fakeExe[0] = 'M';
        fakeExe[1] = 'Z';
        var response = upload(loggedInClient(), "notes.txt", fakeExe);
        assertThat(response.statusCode()).isEqualTo(415);
        }

    @Test
        void realPngIsAccepted() throws Exception {
        var png = new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A,
                0, 0, 0, 0x0D, 'I', 'H', 'D', 'R', 0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0};
        var response = upload(loggedInClient(), "pixel.png", png);
        assertThat(response.statusCode()).isEqualTo(201);
        }

    @Test
        void tooLargeFileIsRejected() throws Exception {
            var response = upload(loggedInClient(), "big.txt", new byte[2 * 1024 * 1024]);
            assertThat(response.statusCode()).isEqualTo(413);
        }

    }