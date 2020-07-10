package ugeneric.circe.benchmark.model.adt

object ADTBenchmarkModel {
  import ugeneric.circe.benchmark.model.adt.ADTs.ADT0
  import ugeneric.circe.benchmark.model.adt.Defaults._
  import ugeneric.circe.benchmark.model.adt.Generic.ADT
  import ugeneric.circe.benchmark.model.adt.Hierarchy._
  import ugeneric.circe.benchmark.model.adt.Recursive._
  type Data = ADT[Seq[(Int, Int)], String, A, LL, ADTc, ADT0]
  val benchmarkSampleData: Data = ADT(
    Vector((1, 2), (3, 4), (4, 5), (6, 7), (8, 9), (10, 11), (12, 13)),
    """
        |I am cow, hear me moo
        |I weigh twice as much as you
        |And I look good on the barbecueeeee
      """.stripMargin,
    C("lol i am a noob", "haha you are a noob"): A,
    Node(-11, Node(-22, Node(-33, Node(-44, End)))): LL,
    ADTc(i = 1234567890, s = "i am a strange loop"),
    ADT0("1234")
  )
}
