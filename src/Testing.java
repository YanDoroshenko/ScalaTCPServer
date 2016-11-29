import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 23.11.16.
 */
public class Testing {

    private static Thread server;

    @BeforeClass
    public static void startServer() throws Exception {
        String[] args = {"3001"};
        server = new Thread(() -> Robot.main(args));
        server.start();
        Thread.sleep(6000);
    }

    @AfterClass
    public static void stopServer() throws Exception {
        server.interrupt();
    }

    @Test
    public void testSyntaxError() throws Exception {
        Socket socket = new Socket("127.0.0.1", 3001);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals("200 LOGIN", in.readLine());
        out.write("2\r\n".getBytes());
        out.flush();
        assertEquals("201 PASSWORD", in.readLine());
        out.write("50\r\n".getBytes());
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("2\r\n".getBytes());
        out.flush();
        assertEquals("501 SYNTAX ERROR", in.readLine());
        try {
            assertEquals(-1, in.read());
        } catch (SocketException e) {
            assertEquals("Connection reset", e.getMessage());
        }
        socket.close();
    }

    @Test
    public void testInfo() throws Exception {
        Socket socket = new Socket("127.0.0.1", 3001);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals("200 LOGIN", in.readLine());
        out.write("2\r\n".getBytes());
        out.flush();
        assertEquals("201 PASSWORD", in.readLine());
        out.write("50\r\n".getBytes());
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("INFO ABCDE\r\n".getBytes());
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("INFOABCDE\r\n".getBytes());
        out.flush();
        assertEquals("501 SYNTAX ERROR", in.readLine());
        try {
            assertEquals(-1, in.read());
        } catch (SocketException e) {
            assertEquals("Connection reset", e.getMessage());
        }
        socket.close();
    }

    @Test
    public void testLoginFailed() throws Exception {
        Socket socket = new Socket("127.0.0.1", 3001);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals("200 LOGIN", in.readLine());
        out.write("2\r\n".getBytes());
        out.flush();
        assertEquals("201 PASSWORD", in.readLine());
        out.write("51\r\n".getBytes());
        out.flush();
        assertEquals("500 LOGIN FAILED", in.readLine());
        try {
            assertEquals(-1, in.read());
        } catch (SocketException e) {
            assertEquals("Connection reset", e.getMessage());
        }
        socket.close();
    }

    @Test
    public void testEmptyLogin() throws Exception {
        Socket socket = new Socket("127.0.0.1", 3001);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals("200 LOGIN", in.readLine());
        out.write("\0\r\n".getBytes());
        out.flush();
        assertEquals("201 PASSWORD", in.readLine());
        out.write("0\r\n".getBytes());
        out.flush();
        assertEquals("500 LOGIN FAILED", in.readLine());
        try {
            assertEquals(-1, in.read());
        } catch (SocketException e) {
            assertEquals("Connection reset", e.getMessage());
        }
        socket.close();
    }

    @Test
    public void testLongLogin() throws Exception {
        Socket socket = new Socket("127.0.0.1", 3001);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals("200 LOGIN", in.readLine());
        byte[] login = new byte[8192];
        for (int i = 0; i < login.length; i++)
            login[i] = 50;
        for (int i = 0; i < 1024; i++) {
            out.write(login);
            out.flush();
        }
        out.write("\r\n".getBytes());
        out.flush();
        assertEquals("201 PASSWORD", in.readLine());
        out.write(String.valueOf(50 * 8192 * 1024).getBytes());
        out.write("\r\n".getBytes());
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("INFOABCDE\r\n".getBytes());
        out.flush();
        assertEquals("501 SYNTAX ERROR", in.readLine());
        try {
            assertEquals(-1, in.read());
        } catch (SocketException e) {
            assertEquals("Connection reset", e.getMessage());
        }
        socket.close();
    }

    @Test
    public void testFoto() throws Exception {
        Socket socket = new Socket("127.0.0.1", 3001);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals("200 LOGIN", in.readLine());
        out.write("2\r\n".getBytes());
        out.flush();
        assertEquals("201 PASSWORD", in.readLine());
        out.write("50\r\n".getBytes());
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("FOTO 8 ABCDEFGH".getBytes());
        byte[] hashsum = {0x0, 0x0, 0x2, 0x24};
        out.write(hashsum);
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("50\r\n".getBytes());
        out.flush();
        assertEquals("501 SYNTAX ERROR", in.readLine());
        try {
            assertEquals(-1, in.read());
        } catch (SocketException e) {
            assertEquals("Connection reset", e.getMessage());
        }
        socket.close();
    }

    @Test
    public void testFotoBadLength() throws Exception {
        Socket socket = new Socket("127.0.0.1", 3001);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals("200 LOGIN", in.readLine());
        out.write("2\r\n".getBytes());
        out.flush();
        assertEquals("201 PASSWORD", in.readLine());
        out.write("50\r\n".getBytes());
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("FOTO -13 A\r\nB\0CD\r\nEFGH".getBytes());
        byte[] hashsum = {0x0, 0x0, 0x2, 0x52};
        out.write(hashsum);
        out.flush();
        assertEquals("501 SYNTAX ERROR", in.readLine());
        try {
            assertEquals(-1, in.read());
        } catch (SocketException e) {
            assertEquals("Connection reset", e.getMessage());
        }
        socket.close();
    }

    @Test
    public void testFotoBadLengthAgain() throws Exception {
        Socket socket = new Socket("127.0.0.1", 3001);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals("200 LOGIN", in.readLine());
        out.write("2\r\n".getBytes());
        out.flush();
        assertEquals("201 PASSWORD", in.readLine());
        out.write("50\r\n".getBytes());
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("FOTO 1a3 A\r\nB\0CD\r\nEFGH".getBytes());
        byte[] hashsum = {0x0, 0x0, 0x2, 0x52};
        out.write(hashsum);
        out.flush();
        assertEquals("501 SYNTAX ERROR", in.readLine());
        try {
            assertEquals(-1, in.read());
        } catch (SocketException e) {
            assertEquals("Connection reset", e.getMessage());
        }
        socket.close();
    }

    @Test
    public void testMalformedFoto() throws Exception {
        Socket socket = new Socket("127.0.0.1", 3001);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals("200 LOGIN", in.readLine());
        out.write("2\r\n".getBytes());
        out.flush();
        assertEquals("201 PASSWORD", in.readLine());
        out.write("50\r\n".getBytes());
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("FOTO 13 A\r\nB\0CD\r\nEFGH".getBytes());
        byte[] hashsum = {0x0, 0x0, 0x2, 0x52};
        out.write(hashsum);
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("50\r\n".getBytes());
        out.flush();
        assertEquals("501 SYNTAX ERROR", in.readLine());
        try {
            assertEquals(-1, in.read());
        } catch (SocketException e) {
            assertEquals("Connection reset", e.getMessage());
        }
        socket.close();
    }

    @Test
    public void testBadChecksum() throws Exception {
        Socket socket = new Socket("127.0.0.1", 3001);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals("200 LOGIN", in.readLine());
        out.write("2\r\n".getBytes());
        out.flush();
        assertEquals("201 PASSWORD", in.readLine());
        out.write("50\r\n".getBytes());
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("FOTO 8 ABCDEFGH".getBytes());
        byte[] hashsum = {0x0, 0x0, 0x2, 0x23};
        out.write(hashsum);
        out.flush();
        assertEquals("300 BAD CHECKSUM", in.readLine());
        out.write("50\r\n".getBytes());
        out.flush();
        assertEquals("501 SYNTAX ERROR", in.readLine());
        try {
            assertEquals(-1, in.read());
        } catch (SocketException e) {
            assertEquals("Connection reset", e.getMessage());
        }
        socket.close();
    }

    @Test
    public void testComplexFoto() throws Exception {
        Socket socket = new Socket("127.0.0.1", 3001);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals("200 LOGIN", in.readLine());
        out.write("2\r\n".getBytes());
        out.flush();
        assertEquals("201 PASSWORD", in.readLine());
        out.write("50\r\n".getBytes());
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("FOTO 8 ABCDEFGH".getBytes());
        byte[] hashsum = {0x0, 0x0, 0x2, 0x24};
        out.write(hashsum);
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("FOTO 8 ABCDEFGH".getBytes());
        byte[] hashsum1 = {0x0, 0x0, 0x2, 0x23};
        out.write(hashsum1);
        out.flush();
        assertEquals("300 BAD CHECKSUM", in.readLine());
        out.write("FOTO 8 ABCDEFGH".getBytes());
        byte[] hashsum2 = {0x0, 0x0, 0x2, 0x24};
        out.write(hashsum2);
        out.flush();
        assertEquals("202 OK", in.readLine());
        out.write("50\r\n".getBytes());
        out.flush();
        assertEquals("501 SYNTAX ERROR", in.readLine());
        try {
            assertEquals(-1, in.read());
        } catch (SocketException e) {
            assertEquals("Connection reset", e.getMessage());
        }
        socket.close();
    }

    @Test
    public void timeoutTest() throws Exception {
        Socket socket = new Socket("127.0.0.1", 3001);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals("200 LOGIN", in.readLine());
        Thread.sleep(45000);
        assertEquals("502 TIMEOUT", in.readLine());
        try {
            assertEquals(-1, in.read());
        } catch (SocketException e) {
            assertEquals("Connection reset", e.getMessage());
        }
        socket.close();
    }

}
