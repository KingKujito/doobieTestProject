package controllers

import java.net.InetAddress

import org.openqa.selenium.{By, WebDriver, WebElement}
import org.openqa.selenium.safari.{SafariDriver, SafariOptions}
import scalaj.http.Http
import org.json4s._
import org.json4s.jackson.JsonMethods._
/**
  * Scrapes the web for random names. Only works for amounts dividable by 10 for now. Required the Safari browser to be installed
  * and to have 'allow for remote automation' turned on.
  */
object DataScraper {
  val safariOptions   : SafariOptions       = new SafariOptions()
  val driver          : WebDriver           = new SafariDriver()
  val namepath                              = "https://www.name-generator.org.uk/quick/"
  //makes sure we get a fresh session (urls expire)
  lazy val streetpath : String              = {
    driver.get("https://www.name-generator.org.uk/street/")
    driver.findElement(By.xpath("//*[@id=\"main\"]/div/form/input[4]")).sendKeys("100")
    driver.findElement(By.xpath("//*[@id=\"main\"]/div/form/input[15]")).click()
    Thread.sleep(2000)
    driver.getCurrentUrl
  }


  def init(path : String): Unit = driver.get(path)

  def scrape(amount: Int, path : String = namepath): List[String] = {
    println("Scraping data..")
    path match {
      case `namepath`   =>
        if(amount > 1000)
          (for(i <- 0 until amount) yield s"person $i").toList
        else
          (for(_ <- 0 until(amount/10)) yield get10Names).toList.flatten //this took 52 minutes to run for amount = 50000
      case `streetpath` =>
        if(amount > 1000)
          (for(i <- 0 until amount) yield s"facility $i").toList
        else
          (for(_ <- 0 until math.ceil(amount/100).toInt) yield get10Streets).toList.flatten //this took 30 minutes to run for amount = 50000 and was stopped due to url expiration
      case _            => List.empty[String]
    }
  }

  def get10Names: List[String] = {
    init(namepath)
    val nameElems = driver.findElements(By.className("name_heading"))
    nameElems.toArray.toList.map {
      case e: WebElement =>
        e.getAttribute("innerHTML")
    }
  }

  def get10Streets: List[String] = {
    init(streetpath)
    val nameElems = driver.findElements(By.className("name"))
    nameElems.toArray.toList.map {
      case e: WebElement =>
        e.getAttribute("innerHTML").split("\\. ").reverse.head
    }
  }

  def getIp: String = {
    import java.net.DatagramSocket

    val socket: DatagramSocket = new DatagramSocket()
    socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
    socket.getLocalAddress.getHostAddress
  }

  def getMyLocation: List[Float] = {
    jsonStrToMap(
      Http("https://ipinfo.io/json").param("token","63884450bf0425").asString.body
    ).get("loc").map {
      case s: String => s.split(",").map(_.toFloat).toList
    }.get
  }

  def jsonStrToMap(jsonStr: String): Map[String, Any] = {
    implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
    parse(jsonStr).extract[Map[String, Any]]
  }
}
