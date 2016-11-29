/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 26.11.16.
  */
object ExpressionParser {


  def parse(s: String): Double = {
    def parse(expr: String): Double = {
      if (expr contains "(") {
        val par_expr = expr.dropWhile(_ != '(')
        var par_counter = 0

        var end: Int = -1
        for (i <- 0 until par_expr.length) {
          if (par_expr(i) == '(')
            par_counter += 1
          else if (par_expr(i) == ')')
            par_counter -= 1
          if (par_counter == 0 && end == -1)
            end = i
        }
        val e = par_expr.substring(0, end + 1)
        parse(expr.replace(e, parse(e.tail.dropRight(1)).toString))
      }
      else if (expr contains "+")
        parse(expr takeWhile (_ != '+')) + parse((expr dropWhile (_ != '+')).tail)
      /*else if (expr contains "-")
        parse(expr takeWhile (_ != '-')) - parse((expr dropWhile (_ != '-')).tail)*/
      else if (expr contains "/")
        parse(expr takeWhile (_ != '/')) / parse((expr dropWhile (_ != '/')).tail)
      else if (expr contains "*")
        parse(expr takeWhile (_ != '*')) * parse((expr dropWhile (_ != '*')).tail)
      else if (expr contains "^")
        Math.pow(parse(expr takeWhile (_ != '^')), parse((expr dropWhile (_ != '^')).tail))
      else
        expr.toDouble
    }

    parse(s.replace(" ", "").replace("-", "+-"))
  }

  def main(args: Array[String]): Unit = {
    println(parse("3^3"))
  }


}
