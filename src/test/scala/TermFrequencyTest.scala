import java.io.File
import java.nio.file.{Paths, Files}

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{BeforeAndAfterEach, ShouldMatchers}

class TermFrequencyTest extends org.scalatest.FunSuite with ShouldMatchers with BeforeAndAfterEach {

  private val configuration = new SparkConf().setAppName("test").setMaster("local")
  protected var sparkContext: SparkContext = null

  override protected def beforeEach(): Unit = {
    sparkContext = new SparkContext(configuration)
  }

  override protected def afterEach(): Unit = {
    sparkContext.stop()
  }

  test("returns empty map for empty input") {

    // Given
    val input = textFileFrom("")

    // When
    val wordFrequencies: Map[String, Int] = termFrequencies(input)

    // Then
    wordFrequencies should have size 0
  }

  test("returns word as key of the map") {

    // Given
    val input = textFileFrom("Lorem")

    // When
    val wordFrequencies: Map[String, Int] = termFrequencies(input)

    // Then
    wordFrequencies should contain key "Lorem"
  }

  test("single word in whole document has frequency 1") {

    // Given
    val input = textFileFrom("Lorem")

    // When
    val wordFrequencies: Map[String, Int] = termFrequencies(input)

    // Then
    wordFrequencies should ((contain key "Lorem") and (contain value 1))
  }

  test("splits text line by spaces") {

    // Given
    val input = textFileFrom("Lorem ipsum")

    // When
    val wordFrequencies: Map[String, Int] = termFrequencies(input)

    // Then
    wordFrequencies should contain key "Lorem"
    wordFrequencies should contain key "ipsum"
  }

  private def textFileFrom(inputText: String) = {
    val temporaryFile = File.createTempFile("tmp", ".tmp")
    temporaryFile.deleteOnExit()
    Files.write(Paths.get(temporaryFile.getAbsolutePath), inputText.getBytes)

    textFile(temporaryFile.getAbsolutePath)
  }

  private def textFile(inputFilePath: String) = {
    sparkContext.textFile(inputFilePath)
  }

  private def termFrequencies(input: RDD[String]): Map[String, Int] = {
    new TermFrequency(input).asMap()
  }

}