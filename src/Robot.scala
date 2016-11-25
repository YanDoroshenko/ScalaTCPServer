import java.io.FileOutputStream
import java.net.{ServerSocket, Socket}
import java.util.concurrent.Executors

import scala.annotation.tailrec
import scala.util.Random

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 23.11.16.
  */
object Robot {

  def main(args: Array[String]): Unit = {
    if (args.length < 1)
      println("No port specified!")
    else {
      print("Creating server on port " + args(0))
      val socket = new ServerSocket(args(0).toInt)
      val executor = Executors.newCachedThreadPool()
      var i = 1
      while (!socket.isClosed) {
        executor submit new Communicator(socket.accept(), i)
        i += 1
      }
    }
  }

  class Communicator(socket: Socket, connectionId: Int) extends Thread {
    final val FOTO = "FOTO "
    final val INFO = "INFO "

    val reader = socket.getInputStream()
    val writer = socket.getOutputStream()
    val logger = new FileOutputStream("Robot" + connectionId + ".log")

    override def run(): Unit = {
      new Thread() {
        override def run(): Unit = {
          Thread.sleep(45000)
          timeout
        }
      }.start()

      login
      val username = read
      passwd
      val password = try {
        read.toLong
      }
      catch {
        case _: NumberFormatException =>
          -1
      }
      if (username.toCharArray.map(_.toLong).sum == password && password != 0)
        ok
      else
        loginFailed

      while (!socket.isClosed)
        readSyntax
    }

    def login = {
      writer write "200 LOGIN\r\n".getBytes
      logger write "Server: Login?\r\n".getBytes
    }

    def passwd = {
      writer write "201 PASSWORD\r\n".getBytes
      logger write "Server: Password?\r\n".getBytes
    }

    def ok = {
      writer write "202 OK\r\n".getBytes
      logger write "Server: OK\r\n".getBytes
    }

    def loginFailed = {
      logger write "Server: Login failed\r\n".getBytes
      if (!socket.isClosed) {
        writer write "500 LOGIN FAILED\r\n".getBytes
        socket.close()
      }
    }

    def timeout = {
      logger write "Server: Timeout\r\n".getBytes
      if (!socket.isClosed) {
        writer write "502 TIMEOUT\r\n".getBytes
        socket.close()
      }
    }

    def read = {

      @tailrec
      def readRec(last: Char, buffer: StringBuilder): String = {
        val c = reader.read().toChar
        if (last == '\r' && c == '\n')
          buffer.toString()
        else
          readRec(c,
            if (last == '\u0000')
              buffer
            else
              buffer + last
          )
      }

      val res = readRec('\u0000', new StringBuilder(8192 * 1024))
      logger write ("Client: " + res + "\r\n").getBytes()
      res
    }

    def readSyntax = {

      @tailrec
      def readRec(last: Char, buffer: StringBuilder): String = {
        val c = reader.read().toChar
        buffer += c
        val str = buffer.toString
        str.length match {
          case l if l < 6 && str != (FOTO take l) && str != (INFO take l) =>
            syntaxError
            buffer.toString()
          case 7 if (str take 5) == (FOTO take 5) && (str(5) < '0' || str(5) > '9') =>
            syntaxError
            buffer.toString()
          case _ if (str take 5) == FOTO && (str split " ").length == 2 && str.last == ' ' =>
            val length = (str split " ") (1).toInt
            val foto = readFoto(new StringBuilder(32768), 0, length)
            val hashsum = (reader.read() << 24) |
              (reader.read() << 16) |
              (reader.read() << 8) |
              reader.read()
            if (foto.map((e: Byte) => (e & 0xff).toLong).sum == hashsum) {
              new FileOutputStream("foto" + new Random().nextInt(999) + ".png").write(foto)
              ok
            }
            else
              badChecksum
            str + foto.mkString("[", " ", "]") + " with actual hashsum: " + foto.map((e: Byte) => (e & 0xff).toLong).sum + "\r\nHashsum entered: " + hashsum
          case _ if last == '\r' && c == '\n' =>
            ok
            buffer.toString()
          case _ =>
            readRec(c, buffer)
        }
      }

      @tailrec
      def readFoto(buffer: StringBuilder, read: Long, length: Long): Array[Byte] = {
        if (read == length)
          buffer.toArray.map(_.toByte)
        else {
          readFoto(buffer + reader.read().toChar, read + 1, length)
        }
      }

      val res = readRec('\u0000', new StringBuilder(32768))
      logger write ("Client: " + res + "\r\n").getBytes()
    }

    def badChecksum = {
      writer write "300 BAD CHECKSUM\r\n".getBytes
      logger write "Server: Bad checksum\r\n".getBytes
    }

    def syntaxError = {
      logger write "Server: Syntax error\r\n".getBytes
      if (!socket.isClosed) {
        writer write "501 SYNTAX ERROR\r\n".getBytes
        socket.close()
      }
    }
  }
}